package com.tuservidor.personaldragon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PDragonListener implements Listener {
    private final DragonManager manager;

    public PDragonListener(DragonManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Entity clicked = e.getRightClicked();
        if (!manager.isOurEntity(clicked)) return;

        UUID owner = manager.getOwner(clicked);
        Player p = e.getPlayer();

        if (owner == null || !owner.equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.RED + "Ese dragón no es tuyo.");
            e.setCancelled(true);
            return;
        }

        manager.mount(p);
        p.sendMessage(ChatColor.AQUA + "Montado. Sprint para BOOST (consume stamina).");
        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.despawn(e.getPlayer());
    }
}
