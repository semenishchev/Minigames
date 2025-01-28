package me.mrfunny.minigame.common.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.block.Block;

public class AnchorCommand extends Command {
    public AnchorCommand() {
        super("anchor");
        setDefaultExecutor((sender, context) -> {
            if(!(sender instanceof Player player)) return;
            Pos position = player.getPosition();
            player.getInstance().setBlock(position.withY(position.y() - 1), Block.BEDROCK);
        });

        var posArg = ArgumentType.RelativeBlockPosition("pos");
        addSyntax((sender, context) -> {
            if(!(sender instanceof Player player)) return;
            player.getInstance().setBlock(context.get(posArg).from(player), Block.BEDROCK);
        }, posArg);
    }
}
