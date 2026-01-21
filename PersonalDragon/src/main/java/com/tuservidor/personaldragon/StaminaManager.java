package com.tuservidor.personaldragon;

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaminaManager {
    private final PersonalDragonPlugin plugin;
    private final Map<UUID, Double> stamina = new HashMap<>();

    public StaminaManager(PersonalDragonPlugin plugin) {
        this.plugin = plugin;
    }

    public double getMax() {
        return plugin.getConfig().getDouble("stamina.max", 100.0);
    }

    public double get(Player p) {
        return stamina.getOrDefault(p.getUniqueId(), getMax());
    }

    public void set(Player p, double value) {
        double max = getMax();
        stamina.put(p.getUniqueId(), Math.max(0.0, Math.min(max, value)));
    }

    public boolean has(Player p, double amount) {
        return get(p) >= amount;
    }

    public void drain(Player p, double amount) {
        set(p, get(p) - amount);
    }

    public void regen(Player p, double amount) {
        set(p, get(p) + amount);
    }

    public void reset(Player p) {
        stamina.put(p.getUniqueId(), getMax());
    }

    public void showBar(Player p, boolean boosting, boolean mounted) {
        double max = getMax();
        double cur = get(p);
        int total = 20;
        int filled = (int) Math.round((cur / max) * total);

        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.DARK_GRAY).append("[");
        for (int i = 0; i < total; i++) {
            if (i < filled) sb.append(ChatColor.LIGHT_PURPLE).append("|");
            else sb.append(ChatColor.GRAY).append("|");
        }
        sb.append(ChatColor.DARK_GRAY).append("] ");

        sb.append(ChatColor.LIGHT_PURPLE).append((int) cur)
          .append(ChatColor.GRAY).append("/")
          .append((int) max);

        if (mounted) {
            sb.append(ChatColor.DARK_GRAY).append(" • ");
            sb.append(boosting ? (ChatColor.AQUA + "BOOST") : (ChatColor.GRAY + "CRUISE"));
        }

        p.sendActionBar(Component.text(sb.toString()));
    }
}
