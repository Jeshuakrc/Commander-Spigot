package com.jkantrell.commander.provider.builtIn;

import com.jkantrell.commander.command.Argument;
import com.jkantrell.commander.exception.CommandArgumentException;
import com.jkantrell.commander.exception.CommandException;
import com.jkantrell.commander.provider.CommandProvider;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LocationProvider extends CommandProvider<Location> {

    private double x_, y_, z_;
    private World world_ = null;

    @Override
    public List<String> suggest() {
        List<String> r = new ArrayList<>();
        Block block = null;
        if (this.getCommandSender() instanceof Player player) {
            StringBuilder builder = new StringBuilder();
            builder.append("~ ~ ~");
            builder.setLength((builder.length() - (this.getSupplyConsecutive() * 2)));
            r.add(builder.toString());

            List<Block> lineOfSight = player.getLineOfSight(null,10);
            if (lineOfSight.isEmpty()) {
                block = player.getLocation().getBlock();
            } else {
                block = lineOfSight.get(0);
            }
        } else if (this.getCommandSender() instanceof BlockCommandSender blockCommandSender) {
            block = blockCommandSender.getBlock();
        }
        if (block != null) {
            String[] pos = {
                Integer.toString(block.getX()),
                Integer.toString(block.getY()),
                Integer.toString(block.getZ())
            };
            StringBuilder builder = new StringBuilder();
            for (int i = this.getSupplyConsecutive(); i < pos.length; i++) {
                builder.setLength(0);
                for (int j = this.getSupplyConsecutive(); j <= i; j++) {
                    builder.append(pos[j]).append(" ");
                }
                builder.setLength(builder.length() - 1);
                r.add(builder.toString());
            }
        }

        return r;
    }

    @Override
    protected boolean handleArgument(Argument argument) throws CommandException {
        if (!(argument.isDouble() || argument.isInt())) {
            throw new CommandArgumentException(argument,"Value must be numeric!");
        }

        int consecutive = this.getSupplyConsecutive();

        double relative = 0;
        if (argument.isRelative()) {
            Location loc;
            if (this.getCommandSender() instanceof Entity entity) {
                loc = entity.getLocation();
            } else if (this.getCommandSender() instanceof BlockCommandSender blockCommandSender) {
                loc = blockCommandSender.getBlock().getLocation().add(.5,.5,.5);
            } else {
                throw new CommandArgumentException(argument,"Relative coordinates are not supported here.");
            }
            relative = switch (consecutive) {
                case 1 -> loc.getX();
                case 2 -> loc.getY();
                case 3 -> loc.getZ();
                default -> 0;
            };
        }
        
        switch (consecutive) {
            case 1 -> this.x_ = argument.getDouble() + relative;
            case 2 -> this.y_ = argument.getDouble() + relative;
            case 3 -> this.z_ = argument.getDouble() + relative;
        }
        
        boolean canInferWorld = true;
        if (this.world_ == null) {
            if (this.getCommandSender() instanceof Entity entity) {
                this.world_ = entity.getWorld();
            } else if (this.getCommandSender() instanceof BlockCommandSender blockCommandSender) {
                this.world_ = blockCommandSender.getBlock().getWorld();
            } else {
                canInferWorld = false;
                if (consecutive > 3) {
                    WorldProvider worldProvider = new WorldProvider();
                    worldProvider.initialize(this.getCommander(),this.getCommandSender(),this.getAnnotations());
                    worldProvider.supply(argument);
                    this.world_ = worldProvider.provide();
                }
            }
        }

        return canInferWorld ? consecutive > 2 : consecutive > 3;
    }

    @Override
    public Location provide() throws CommandException {
        return new Location(this.world_,this.x_,this.y_,this.z_);
    }
}
