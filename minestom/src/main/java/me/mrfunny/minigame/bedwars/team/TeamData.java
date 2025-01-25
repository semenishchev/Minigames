package me.mrfunny.minigame.bedwars.team;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.mrfunny.minigame.bedwars.data.GeneratorData;
import me.mrfunny.minigame.common.TeamColor;
import net.minestom.server.coordinate.Pos;

import java.util.List;

public class TeamData {
    @JsonIgnore
    public TeamColor color;
    public Pos spawnPos;
    public Pos itemShopPos;
    public Pos teamUpgradesPos;
    public Pos bedHalf1;
    public Pos bedHalf2;
    public Pos generatorPos;
    public Pos teamChestPos;
    public Pos personalChestPos;
    public Pos protectedCornerMin;
    public Pos protectedCornerMax;

    public TeamData() {}

    public TeamData(TeamColor color, List<GeneratorData> generators, Pos spawnPos, Pos itemShopPos, Pos teamUpgradesPos, Pos bedHalf1, Pos bedHalf2, Pos teamChestPos, Pos personalChestPos, Pos protectedCornerMin, Pos protectedCornerMax) {
        this.color = color;
        this.spawnPos = spawnPos;
        this.itemShopPos = itemShopPos;
        this.teamUpgradesPos = teamUpgradesPos;
        this.bedHalf1 = bedHalf1;
        this.bedHalf2 = bedHalf2;
        this.generatorPos = generatorPos;
        this.teamChestPos = teamChestPos;
        this.personalChestPos = personalChestPos;
        this.protectedCornerMin = protectedCornerMin;
        this.protectedCornerMax = protectedCornerMax;
    }
}
