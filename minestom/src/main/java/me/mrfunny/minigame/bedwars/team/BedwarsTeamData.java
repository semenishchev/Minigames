package me.mrfunny.minigame.bedwars.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.mrfunny.minigame.bedwars.data.BedwarsGeneratorData;
import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class BedwarsTeamData {
    @JsonIgnore
    public TeamColor color;
    public Pos spawnPos;
    public Pos itemShopPos;
    public Pos teamUpgradesPos;
    public Pos bedPos;
    public List<BedwarsGeneratorData> generators;
    public Pos teamChestPos;
    public Pos protectedCornerMin;
    public Pos protectedCornerMax;

    public BedwarsTeamData() {}

    public BedwarsTeamData(TeamColor color) {
        this.color = color;
    }
}
