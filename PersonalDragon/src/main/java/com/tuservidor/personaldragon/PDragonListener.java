package com.tuservidor.personaldragon;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PDragonListener implements Listener {

    private final DragonManager manager;

    public PDragonListener(DragonManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent e) {
        Player p = e.getPlayer();
        Entity clicked = e.getRightClicked();

        // Solo montamos si el jugador tiene dragón y el click fue cerca del vehículo/hitbox
        DragonBundle b = manager.get(p);
        if (b == null || !b.isValid()) return;

        if (clicked.getUniqueId().equals(b.vehicle().getUniqueId())
                || clicked.getUniqueId().equals(b.hitbox().getUniqueId())
                || clicked.getLocation().distanceSquared(b.vehicle().getLocation()) <= 9.0) {

            manager.mount(p);
            p.sendMessage(ChatColor.AQUA + "Montado. Sprint para BOOST (consume stamina).");
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.despawn(e.getPlayer());
    }
}
