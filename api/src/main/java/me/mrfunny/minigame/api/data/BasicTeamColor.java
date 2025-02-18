package me.mrfunny.minigame.api.data;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public interface BasicTeamColor {
    String getBlockName();
    Component getNameComponent();
    String getId();
    TextColor getColor();
}
