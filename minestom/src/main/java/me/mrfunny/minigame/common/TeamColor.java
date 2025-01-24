package me.mrfunny.minigame.common;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.color.DyeColor;
import net.minestom.server.item.Material;

public enum TeamColor {
    RED(NamedTextColor.RED, DyeColor.RED, "RED"),
    GREEN(NamedTextColor.GREEN, DyeColor.GREEN, "LIME"),
    BLUE(NamedTextColor.BLUE, DyeColor.BLUE, "BLUE"),
    YELLOW(NamedTextColor.YELLOW, DyeColor.YELLOW, "YELLOW"),
    AQUA(NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, "LIGHT_BLUE"),
    PINK(NamedTextColor.LIGHT_PURPLE, DyeColor.PINK, "PINK"),
    GRAY(NamedTextColor.DARK_GRAY, DyeColor.GRAY, "GRAY"),
    WHITE(NamedTextColor.WHITE, DyeColor.WHITE, "WHITE");
    public final NamedTextColor namedColor;
    public final TextColor chatColor;
    public final DyeColor dyeColor;
    public final String blockColorName;
    public final Material buildingBlock;
    public final Material glass;
    public final Material wool;
    public final Material bed;

    TeamColor(TextColor teamColor, DyeColor dyeColor, String colorName) {
        this.namedColor = NamedTextColor.nearestTo(teamColor);
        this.chatColor = teamColor;
        this.dyeColor = dyeColor;
        this.blockColorName = colorName;
        this.buildingBlock = getColoredMaterial("CONCRETE");
        this.glass = getColoredMaterial("STAINED_GLASS");
        this.wool = getColoredMaterial("WOOL");
        this.bed = getColoredMaterial("BED");
    }

    private Material getColoredMaterial(String name) {
        return Material.fromNamespaceId(this.blockColorName + "_" + name);
    }
}
