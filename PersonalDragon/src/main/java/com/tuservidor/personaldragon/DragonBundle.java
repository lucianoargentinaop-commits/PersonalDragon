package com.tuservidor.personaldragon;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;

import java.util.List;
import java.util.UUID;

public record DragonBundle(
        UUID owner,
        ArmorStand vehicle,
        Interaction hitbox,
        List<BlockDisplay> blocks
) {
    public boolean isValid() {
        return vehicle != null && !vehicle.isDead()
                && hitbox != null && !hitbox.isDead()
                && blocks != null && !blocks.isEmpty();
    }

    public void remove() {
        for (BlockDisplay bd : blocks) bd.remove();
        hitbox.remove();
        vehicle.remove();
    }
}
