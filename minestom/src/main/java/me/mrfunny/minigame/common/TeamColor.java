package me.mrfunny.minigame.common;

import me.mrfunny.minigame.Translations;
import me.mrfunny.minigame.api.data.BasicTeamColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.color.DyeColor;
import net.minestom.server.item.Material;

public enum TeamColor implements BasicTeamColor {
    RED(NamedTextColor.RED, DyeColor.RED, "RED", Translations.COMMON_COLOR_RED),
    GREEN(NamedTextColor.GREEN, DyeColor.GREEN, "LIME", Translations.COMMON_COLOR_GREEN),
    BLUE(NamedTextColor.BLUE, DyeColor.BLUE, "BLUE", Translations.COMMON_COLOR_BLUE),
    YELLOW(NamedTextColor.YELLOW, DyeColor.YELLOW, "YELLOW", Translations.COMMON_COLOR_YELLOW),
    AQUA(NamedTextColor.AQUA, DyeColor.LIGHT_BLUE, "LIGHT_BLUE", Translations.COMMON_COLOR_AQUA),
    PINK(NamedTextColor.LIGHT_PURPLE, DyeColor.PINK, "PINK", Translations.COMMON_COLOR_PINK),
    GRAY(NamedTextColor.DARK_GRAY, DyeColor.GRAY, "GRAY", Translations.COMMON_COLOR_GRAY),
    WHITE(NamedTextColor.WHITE, DyeColor.WHITE, "WHITE", Translations.COMMON_COLOR_WHITE),;
    public final NamedTextColor namedColor;
    public final TextColor chatColor;
    public final DyeColor dyeColor;
    public final String blockColorName;
    public final Material buildingBlock;
    public final Material glass;
    public final Material wool;
    public final Material bed;
    public final Component userDisplay;

    TeamColor(TextColor teamColor, DyeColor dyeColor, String colorName, Component userDisplay) {
        this.namedColor = NamedTextColor.nearestTo(teamColor);
        this.chatColor = teamColor;
        this.dyeColor = dyeColor;
        this.blockColorName = colorName;
        this.buildingBlock = getColoredMaterial("CONCRETE");
        this.glass = getColoredMaterial("STAINED_GLASS");
        this.wool = getColoredMaterial("WOOL");
        this.bed = getColoredMaterial("BED");
        this.userDisplay = userDisplay;
    }

    private Material getColoredMaterial(String name) {
        return Material.fromNamespaceId(this.blockColorName + "_" + name);
    }

    @Override
    public String getBlockName() {
        return blockColorName;
    }

    @Override
    public Component getNameComponent() {
        return userDisplay;
    }

    @Override
    public String getId() {
        return name().toLowerCase();
    }

    @Override
    public TextColor getColor() {
        return chatColor;
    }
}
