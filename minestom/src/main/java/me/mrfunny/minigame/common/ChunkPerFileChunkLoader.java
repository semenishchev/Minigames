package me.mrfunny.minigame.common;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import me.mrfunny.minigame.minestom.Main;
import net.kyori.adventure.nbt.*;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.CoordConversion;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.IChunkLoader;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.Section;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockHandler;
import net.minestom.server.instance.palette.Palettes;
import net.minestom.server.registry.DynamicRegistry;
import net.minestom.server.registry.Registries;
import net.minestom.server.utils.MathUtils;
import net.minestom.server.utils.validate.Check;
import net.minestom.server.world.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class ChunkPerFileChunkLoader implements IChunkLoader {
    private final Logger logger;
    private final File folder;
    private final boolean save;
    private final Long2ObjectMap<byte[]> cachedChunkData = new Long2ObjectOpenHashMap<>();
    private final int biomeId;

    public ChunkPerFileChunkLoader(UUID instanceId, File folder, boolean save, DynamicRegistry.Key<Biome> biome) {
        this.folder = folder;
        this.save = save;
        this.biomeId = MinecraftServer.getBiomeRegistry().getId(biome.namespace());
        this.logger = Main.getLogger("chunk-loader-" + instanceId.toString().substring(0, 8));
    }

    @Override
    public @Nullable Chunk loadChunk(@NotNull Instance instance, int chunkX, int chunkZ) {
        long pos = CoordConversion.chunkIndex(chunkX, chunkZ);
        byte[] chunkDataBytes = cachedChunkData.computeIfAbsent(pos, p -> {
            File chunkFile = new File(folder, chunkX + "_" + chunkZ + ".dat");
            if (!chunkFile.exists()) return null;
            InflaterOutputStream inflaterOutputStream  = null;
            try {
                byte[] compressed = Files.readAllBytes(chunkFile.toPath());
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                inflaterOutputStream = new InflaterOutputStream(bytes);
                inflaterOutputStream.write(compressed);
                return bytes.toByteArray();
            } catch (Exception e) {
                logger.error("Failed loading chunk {} {}", chunkX, chunkZ, e);
            } finally {
                if(inflaterOutputStream != null) {
                    try {
                        inflaterOutputStream.close();
                    } catch (IOException e) {
                        logger.error("Failed to close stream", e);
                    }
                }
            }
           return null;
        });
        if(chunkDataBytes == null) return null;
        try {
            CompoundBinaryTag chunkData = BinaryTagIO.unlimitedReader().read(new ByteArrayInputStream(chunkDataBytes));
            Chunk chunk = instance.getChunkSupplier().createChunk(instance, chunkX, chunkZ);
            final String status = chunkData.getString("status");
            if (status.isEmpty() || "minecraft:full".equals(status)) {
                synchronized (chunk) {
                    loadSections(chunk, chunkData);
                    // Block entities
                    loadBlockEntities(chunk, chunkData);

                    chunk.loadHeightmapsFromNBT(chunkData.getCompound("Heightmaps"));
                }

            } else {
                return null;
            }
            return chunk;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Copied from Minestom
    private void loadSections(@NotNull Chunk chunk, @NotNull CompoundBinaryTag chunkData) {
        for (BinaryTag sectionTag : chunkData.getList("sections", BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag sectionData = (CompoundBinaryTag) sectionTag;

            final int sectionY = sectionData.getInt("Y", Integer.MIN_VALUE);
            Check.stateCondition(sectionY == Integer.MIN_VALUE, "Missing section Y value");
            final int yOffset = Chunk.CHUNK_SECTION_SIZE * sectionY;

            if (sectionY < chunk.getMinSection() || sectionY >= chunk.getMaxSection()) {
                // Vanilla stores a section below and above the world for lighting, throw it out.
                continue;
            }

            final Section section = chunk.getSection(sectionY);

            // Lighting
            if (sectionData.get("SkyLight") instanceof ByteArrayBinaryTag skyLightTag && skyLightTag.size() == 2048) {
                section.setSkyLight(skyLightTag.value());
            }
            if (sectionData.get("BlockLight") instanceof ByteArrayBinaryTag blockLightTag && blockLightTag.size() == 2048) {
                section.setBlockLight(blockLightTag.value());
            }

            {   // Biomes
                section.biomePalette().fill(this.biomeId);
            }

            {   // Blocks
                final CompoundBinaryTag blockStatesTag = sectionData.getCompound("block_states");
                final ListBinaryTag blockPaletteTag = blockStatesTag.getList("palette", BinaryTagTypes.COMPOUND);
                Block[] convertedPalette = loadBlockPalette(blockPaletteTag);
                if (blockPaletteTag.size() == 1) {
                    // One solid block, no need to check the data
                    section.blockPalette().fill(convertedPalette[0].stateId());
                } else if (blockPaletteTag.size() > 1) {
                    final long[] packedStates = blockStatesTag.getLongArray("data");
                    Check.stateCondition(packedStates.length == 0, "Missing packed states data");
                    int[] blockStateIndices = new int[Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE];
                    Palettes.unpack(blockStateIndices, packedStates, packedStates.length * 64 / blockStateIndices.length);

                    for (int y = 0; y < Chunk.CHUNK_SECTION_SIZE; y++) {
                        for (int z = 0; z < Chunk.CHUNK_SECTION_SIZE; z++) {
                            for (int x = 0; x < Chunk.CHUNK_SECTION_SIZE; x++) {
                                try {
                                    final int blockIndex = y * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE + z * Chunk.CHUNK_SECTION_SIZE + x;
                                    final int paletteIndex = blockStateIndices[blockIndex];
                                    final Block block = convertedPalette[paletteIndex];

                                    chunk.setBlock(x, y + yOffset, z, block);
                                } catch (Exception e) {
                                    MinecraftServer.getExceptionManager().handleException(e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private Block[] loadBlockPalette(@NotNull ListBinaryTag paletteTag) {
        Block[] convertedPalette = new Block[paletteTag.size()];
        for (int i = 0; i < convertedPalette.length; i++) {
            CompoundBinaryTag paletteEntry = paletteTag.getCompound(i);
            String blockName = paletteEntry.getString("Name");
            if (blockName.equals("minecraft:air")) {
                convertedPalette[i] = Block.AIR;
            } else {
                Block block = Objects.requireNonNull(Block.fromNamespaceId(blockName), "Unknown block " + blockName);
                // Properties
                final Map<String, String> properties = new HashMap<>();
                CompoundBinaryTag propertiesNBT = paletteEntry.getCompound("Properties");
                for (var property : propertiesNBT) {
                    if (property.getValue() instanceof StringBinaryTag propertyValue) {
                        properties.put(property.getKey(), propertyValue.value());
                    } else {
                        logger.warn("Fail to parse block state properties {}, expected a string for {}, but contents were {}",
                            propertiesNBT, property.getKey(), TagStringIOExt.writeTag(property.getValue()));
                    }
                }
                if (!properties.isEmpty()) block = block.withProperties(properties);

                // Handler
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandler(block.name());
                if (handler != null) block = block.withHandler(handler);

                convertedPalette[i] = block;
            }
        }
        return convertedPalette;
    }

    private void loadBlockEntities(@NotNull Chunk loadedChunk, @NotNull CompoundBinaryTag chunkData) {
        for (BinaryTag blockEntityTag : chunkData.getList("block_entities", BinaryTagTypes.COMPOUND)) {
            final CompoundBinaryTag blockEntity = (CompoundBinaryTag) blockEntityTag;

            final int x = blockEntity.getInt("x");
            final int y = blockEntity.getInt("y");
            final int z = blockEntity.getInt("z");
            Block block = loadedChunk.getBlock(x, y, z);

            // Load the block handler if the id is present
            if (blockEntity.get("id") instanceof StringBinaryTag blockEntityId) {
                final BlockHandler handler = MinecraftServer.getBlockManager().getHandlerOrDummy(blockEntityId.value());
                block = block.withHandler(handler);
            }

            // Remove anvil tags
            CompoundBinaryTag trimmedTag = CompoundBinaryTag.builder().put(blockEntity)
                .remove("id").remove("keepPacked")
                .remove("x").remove("y").remove("z")
                .build();

            // Place block
            final var finalBlock = trimmedTag.size() > 0 ? block.withNbt(trimmedTag) : block;
            loadedChunk.setBlock(x, y, z, finalBlock);
        }
    }

    @Override
    public void saveChunk(@NotNull Chunk chunk) {
        if(!save) return;
        int chunkX = chunk.getChunkX();
        int chunkZ = chunk.getChunkZ();
        File chunkFile = new File(folder, chunkX + "_" + chunkZ + ".dat");
        DeflaterOutputStream out = null;
        try {
            File parent = chunkFile.getParentFile();
            if(!parent.exists()) parent.mkdirs();
            out = new DeflaterOutputStream(new FileOutputStream(chunkFile), new Deflater(9), true);
        } catch (IOException e) {
            logger.error("Failed to create chunk file {} {}", chunkX, chunkZ, e);
        }
        if(out == null) return;
        final CompoundBinaryTag.Builder chunkData = CompoundBinaryTag.builder();

        chunkData.putInt("DataVersion", MinecraftServer.DATA_VERSION);
        chunkData.putString("status", "minecraft:full");

        if(!saveSectionData(chunk, chunkData)) {
            logger.info("Skipping empty chunk {} {}", chunkX, chunkZ);
            return;
        }
        try {
            BinaryTagIO.writer().write(chunkData.build(), out);
        } catch(IOException e) {
            logger.error("Failed to write chunk data {} {}", chunkX, chunkZ, e);
        }
    }

    private boolean saveSectionData(@NotNull Chunk chunk, @NotNull CompoundBinaryTag.Builder chunkData) {
        final ListBinaryTag.Builder<CompoundBinaryTag> sections = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        final ListBinaryTag.Builder<CompoundBinaryTag> blockEntities = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);

        // Block & Biome arrays reused for each chunk
        List<BinaryTag> biomePalette = new ArrayList<>();
        int[] biomeIndices = new int[64];

        List<BinaryTag> blockPaletteEntries = new ArrayList<>();
        IntList blockPaletteIndices = new IntArrayList(); // Map block indices by state id to avoid doing a deep comparison on every block tag
        int[] blockIndices = new int[Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE * Chunk.CHUNK_SECTION_SIZE];
        boolean empty = true;
        synchronized (chunk) {
            for (int sectionY = chunk.getMinSection(); sectionY < chunk.getMaxSection(); sectionY++) {
                final Section section = chunk.getSection(sectionY);

                final CompoundBinaryTag.Builder sectionData = CompoundBinaryTag.builder();
                sectionData.putByte("Y", (byte) sectionY);

                // Lighting
                byte[] skyLight = section.skyLight().array();
                if (skyLight != null && skyLight.length > 0)
                    sectionData.putByteArray("SkyLight", skyLight);
                byte[] blockLight = section.blockLight().array();
                if (blockLight != null && blockLight.length > 0)
                    sectionData.putByteArray("BlockLight", blockLight);

                // Build block, biome palettes & collect block entities
                for (int sectionLocalY = 0; sectionLocalY < Chunk.CHUNK_SECTION_SIZE; sectionLocalY++) {
                    for (int z = 0; z < Chunk.CHUNK_SIZE_Z; z++) {
                        for (int x = 0; x < Chunk.CHUNK_SIZE_X; x++) {
                            final int y = sectionLocalY + (sectionY * Chunk.CHUNK_SECTION_SIZE);

                            final int blockIndex = x + sectionLocalY * 16 * 16 + z * 16;
                            final Block block = chunk.getBlock(x, y, z);

                            // Add block state
                            final int blockStateId = block.stateId();
                            final CompoundBinaryTag blockState = getBlockState(block);
                            if(empty && blockState != null && !blockState.getString("Name", "minecraft:air").equalsIgnoreCase("minecraft:air")) {
                                empty = false;
                            }
                            int blockPaletteIndex = blockPaletteIndices.indexOf(blockStateId);
                            if (blockPaletteIndex == -1) {
                                blockPaletteIndex = blockPaletteEntries.size();
                                blockPaletteEntries.add(blockState);
                                blockPaletteIndices.add(blockStateId);
                            }
                            blockIndices[blockIndex] = blockPaletteIndex;

                            // Add biome (biome are stored for 4x4x4 volumes, avoid unnecessary work)
                            if (x % 4 == 0 && sectionLocalY % 4 == 0 && z % 4 == 0) {
                                int biomeIndex = (x / 4) + (sectionLocalY / 4) * 4 * 4 + (z / 4) * 4;
                                final DynamicRegistry.Key<Biome> biomeKey = chunk.getBiome(x, y, z);
                                final BinaryTag biomeName = StringBinaryTag.stringBinaryTag(biomeKey.name());

                                int biomePaletteIndex = biomePalette.indexOf(biomeName);
                                if (biomePaletteIndex == -1) {
                                    biomePaletteIndex = biomePalette.size();
                                    biomePalette.add(biomeName);
                                }

                                biomeIndices[biomeIndex] = biomePaletteIndex;
                            }

                            // Add block entity if present
                            final BlockHandler handler = block.handler();
                            final CompoundBinaryTag originalNBT = block.nbt();
                            if (originalNBT != null || handler != null) {
                                CompoundBinaryTag.Builder blockEntityTag = CompoundBinaryTag.builder();
                                if (originalNBT != null) {
                                    blockEntityTag.put(originalNBT);
                                }
                                if (handler != null) {
                                    blockEntityTag.putString("id", handler.getNamespaceId().asString());
                                }
                                blockEntityTag.putInt("x", x + Chunk.CHUNK_SIZE_X * chunk.getChunkX());
                                blockEntityTag.putInt("y", y);
                                blockEntityTag.putInt("z", z + Chunk.CHUNK_SIZE_Z * chunk.getChunkZ());
                                blockEntityTag.putByte("keepPacked", (byte) 0);
                                blockEntities.add(blockEntityTag.build());
                            }
                        }
                    }
                }
                // Save the block and biome palettes
                final CompoundBinaryTag.Builder blockStates = CompoundBinaryTag.builder();
                blockStates.put("palette", ListBinaryTag.listBinaryTag(BinaryTagTypes.COMPOUND, blockPaletteEntries));
                if (blockPaletteEntries.size() > 1) {
                    // If there is only one entry we do not need to write the packed indices
                    var bitsPerEntry = (int) Math.max(4, Math.ceil(Math.log(blockPaletteEntries.size()) / Math.log(2)));
                    blockStates.putLongArray("data", Palettes.pack(blockIndices, bitsPerEntry));
                    empty = false;
                }
                sectionData.put("block_states", blockStates.build());

                blockPaletteEntries.clear();
                blockPaletteIndices.clear();

                sections.add(sectionData.build());
            }
        }

        chunkData.put("sections", sections.build());
        chunkData.put("block_entities", blockEntities.build());
        return !empty;
    }

    private final ThreadLocal<Int2ObjectMap<CompoundBinaryTag>> blockStateId2ObjectCacheTLS = ThreadLocal.withInitial(Int2ObjectArrayMap::new);
    private CompoundBinaryTag getBlockState(final Block block) {
        return blockStateId2ObjectCacheTLS.get().computeIfAbsent(block.stateId(), _unused -> {
            final CompoundBinaryTag.Builder tag = CompoundBinaryTag.builder();
            tag.putString("Name", block.name());

            if (!block.properties().isEmpty()) {
                final Map<String, String> defaultProperties = Block.fromBlockId(block.id()).properties(); // Never null
                final CompoundBinaryTag.Builder propertiesTag = CompoundBinaryTag.builder();
                for (var entry : block.properties().entrySet()) {
                    String key = entry.getKey(), value = entry.getValue();
                    if (defaultProperties.get(key).equals(value))
                        continue; // Skip default values

                    propertiesTag.putString(key, value);
                }
                var properties = propertiesTag.build();
                if (properties.size() > 0) {
                    tag.put("Properties", properties);
                }
            }
            return tag.build();
        });
    }
}
