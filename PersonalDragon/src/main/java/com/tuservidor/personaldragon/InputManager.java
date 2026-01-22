package com.tuservidor.personaldragon;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InputManager {

    private final Map<UUID, Vector> inputs = new HashMap<>();

    public void update(Player player, float forward, float sideways) {
        inputs.put(player.getUniqueId(), new Vector(sideways, 0, forward));
    }

    public Vector get(Player player) {
        return inputs.getOrDefault(player.getUniqueId(), new Vector(0, 0, 0));
    }

    public void clear(Player player) {
        inputs.remove(player.getUniqueId());
    }
}
