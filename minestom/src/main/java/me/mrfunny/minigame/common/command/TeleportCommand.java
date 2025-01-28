package me.mrfunny.minigame.common.command;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity;
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.entity.EntityFinder;
import net.minestom.server.utils.location.RelativeVec;
import org.jetbrains.annotations.NotNull;

public class TeleportCommand extends Command {
    public TeleportCommand() {
        super("tp");
        setDefaultExecutor((sender, context) -> {
            String commandName = context.getCommandName();
            sender.sendMessage(Component.text("Usage: /" + commandName + " <position> [targets]", NamedTextColor.RED));
        });
        ArgumentEntity player1 = ArgumentType.Entity("player1")
            .onlyPlayers(true);
        ArgumentEntity player2 = ArgumentType.Entity("player1")
            .onlyPlayers(true);
        addSyntax((sender, context) -> {
            EntityFinder finder = context.get(player1);
            Entity teleportTo = context.get(player2).findFirstEntity(sender);
            if(teleportTo == null) {
                sender.sendMessage(Component.text("Target not found", NamedTextColor.RED));
                return;
            }
            for (@NotNull Entity entity : finder.find(sender)) {
                entity.teleport(teleportTo.getPosition());
            }
        }, player1, player2);
        ArgumentRelativeVec3 location = ArgumentType.RelativeVec3("location");
        addSyntax((sender, context) -> {
            if(!(sender instanceof Player player)) {
                sender.sendMessage("Only players can execute this command");
                return;
            }
            player.teleport(context.get(location).from(player).asPosition());
        }, location);
        addSyntax((sender, context) -> {
            EntityFinder finder = context.get(player1);
            RelativeVec relativeVec = context.get(location);
            for (@NotNull Entity entity : finder.find(sender)) {
                entity.teleport(relativeVec.from(entity).asPosition());
            }
        }, player1, location);
    }
}
