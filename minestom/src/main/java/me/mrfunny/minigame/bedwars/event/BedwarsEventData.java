package me.mrfunny.minigame.bedwars.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class BedwarsEventData {
    @JsonIgnore
    public String name;
    public int activatesOn;
    public List<String> commandsOnActivate;
}
