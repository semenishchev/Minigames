package me.mrfunny.minigame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Translations {
    private static List<Component> allTranslations;
    public static final Component BEDWARS_TEAM_SELECTOR = translation("bedwars.team-selector");
    public static final Component BEDWARS_IRON_GENERATOR = translation("bedwars.generator.iron", "Iron Generator");
    public static final Component BEDWARS_GOLD_GENERATOR = translation("bedwars.generator.gold", "Gold Generator");
    public static final Component BEDWARS_DIAMOND_GENERATOR = translation("bedwars.generator.diamond", "Diamond Generator");
    public static final Component BEDWARS_EMERALD_GENERATOR = translation("bedwars.generator.emerald", "Emerald Generator");
    public static final Component BEDWARS_ITEM_SHOP = translation("bedwars.item.shop", "Item Shop");
    public static final Component BEDWARS_TEAM_SHOP = translation("bedwars.team.shop", "Team Shop");
    public static final Component BEDWARS_TEAM_CHEST = translation("bedwars.chest.team", "Team Chest");
    public static final Component BEDWARS_PERSONAL_CHEST = translation("bedwars.chest.personal", "Personal Chest");
    public static final Component BEDWARS_CANT_BREAK_OWN_BED = translation("bedwars.msg.cant-break-own-bed", "You can't break your own bed");
    public static final Component BEDWARS_CANT_BREAK = translation("bedwars.msg.cant-break", "You can't break blocks here");
    public static final Component BEDWARS_CANT_PLACE = translation("bedwars.msg.cant-place", "You can't place blocks here");
    public static final Component BEDWARS_YOUR_BED_DESTROYED = translation("bedwars.msg.bed-destroyed", "Your bed has been destroyed");
    public static final Component BEDWARS_BED_DESTROYED_CHAT  = translation("bedwars.msg.bed-destroyed-chat", "{}");
    public static final Component BEDWARS_KILL = translation("bedwars.msg.kill", "{} has been killed by {}");
    public static final Component BEDWARS_FINAL_KILL  = translation("bedwars.final-kill", "FINAL KILL!");
    public static final Component BEDWARS_PURCHASED_ITEM  = translation("bedwars.msg.purchased", "You have purchased a {}");
    public static final Component BEDWARS_PURCHASED_TEAM_UPGRADE  = translation("bedwars.msg.team-upgrade.purchased", "{} has upgraded {}");
    public static final Component BEDWARS_QUICK_BUY = translation("bedwars.shop.quickbuy", "Quick Buy");
    public static final Component BEDWARS_CAT_BLOCKS = translation("material.shop.blocks", "Blocks");
    public static final Component BEDWARS_CAT_TOOLS = translation("material.shop.tools", "Tools");
    public static final Component BEDWARS_CAT_WEAPONS = translation("material.shop.weapons", "Weapons");
    public static final Component BEDWARS_CAT_ARMOR = translation("material.shop.armor", "Armor");
    public static final Component BEDWARS_CAT_RANGED = translation("material.shop.ranged", "Ranged");
    public static final Component BEDWARS_CAT_UTILS = translation("material.shop.utils", "Utilities");
    public static final Component COMMON_VICTORY = translation("msg.victory", "Victory");
    public static final Component COMMON_RESPAWNING = translation("msg.respawning", "Respawning");
    public static final Component COMMON_RESPAWNING_IN = translation("msg.respawning", "Respawning in {}");
    public static final Component COMMON_TIME_SECOND = translation("msg.time-second", "second(s)");
    public static final Component COMMON_TIME_MINUTE = translation("msg.time-minute", "minute(s)");
    public static final Component COMMON_YOUR = translation("msg.your", "Your");
    public static final Component COMMON_TEAM = translation("msg.team", "Team");
    public static final Component COMMON_COLOR_GREEN = translation("color.green", "Green", NamedTextColor.GREEN);
    public static final Component COMMON_COLOR_RED = translation("color.red", "Red", NamedTextColor.RED);
    public static final Component COMMON_COLOR_YELLOW = translation("color.yellow", "Yellow", NamedTextColor.YELLOW);
    public static final Component COMMON_COLOR_BLUE = translation("color.blue", "Blue", NamedTextColor.BLUE);
    public static final Component COMMON_COLOR_PINK = translation("color.pink", "Pink", NamedTextColor.LIGHT_PURPLE);
    public static final Component COMMON_COLOR_AQUA = translation("color.aqua", "Aqua", NamedTextColor.AQUA);
    public static final Component COMMON_COLOR_GRAY = translation("color.gray", "Gray", NamedTextColor.DARK_GRAY);
    public static final Component COMMON_COLOR_WHITE = translation("color.white", "White", NamedTextColor.WHITE);
    public static final Component COMMON_COLOR_BLACK = translation("color.black", "Black", NamedTextColor.BLACK);
    public static final Component COMMON_TO_LOBBY = translation("msg.back-lobby", "Go back to Lobby");
    private static Component translation(String key) {
        return translation(key, null);
    }
    private static Component translation(String key, String fallback) {
        return translation(key, fallback, null);
    }

    private static Component translation(String key, String fallback, TextColor color) {
        TranslatableComponent.Builder builder = Component.translatable()
            .key(key)
            .fallback(fallback)
            .decoration(TextDecoration.ITALIC, false);
        if(color != null) {
            builder.color(color);
        }
        Component component = builder.build();
        if(allTranslations == null) {
            allTranslations = new LinkedList<>();
        }
        allTranslations.add(component);
        return component;
    }
}
