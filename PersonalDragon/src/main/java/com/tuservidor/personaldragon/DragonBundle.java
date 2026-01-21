package com.tuservidor.personaldragon;

import org.bukkit.entity.*;

import java.util.UUID;

public record DragonBundle(
        UUID owner,
        ArmorStand vehicle,
        Interaction hitbox,
        ItemDisplay head,
        ItemDisplay body,
        ItemDisplay wingL,
        ItemDisplay wingR,
        ItemDisplay tail
) {
    public boolean isValid() {
        return vehicle != null && !vehicle.isDead()
                && hitbox != null && !hitbox.isDead()
                && head != null && !head.isDead()
                && body != null && !body.isDead()
                && wingL != null && !wingL.isDead()
                && wingR != null && !wingR.isDead()
                && tail != null && !tail.isDead();
    }

    public void remove() {
        if (tail != null) tail.remove();
        if (wingR != null) wingR.remove();
        if (wingL != null) wingL.remove();
        if (body != null) body.remove();
        if (head != null) head.remove();
        if (hitbox != null) hitbox.remove();
        if (vehicle != null) vehicle.remove();
    }
}
