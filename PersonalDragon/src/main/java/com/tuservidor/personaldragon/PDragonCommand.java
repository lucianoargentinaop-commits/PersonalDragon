package com.tuservidor.personaldragon;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class PDragonCommand implements CommandExecutor {
    private final DragonManager manager;
    private final StaminaManager stamina;

    public PDragonCommand(PersonalDragonPlugin plugin, DragonManager manager, StaminaManager stamina) {
        this.manager = manager;
        this.stamina = stamina;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("Solo jugadores.");
            return true;
        }
        if (!p.hasPermission("personaldragon.use")) {
            p.sendMessage(ChatColor.RED + "Sin permiso.");
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Uso: /pdragon summon | /pdragon despawn | /pdragon staminareset");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "summon" -> {
                manager.spawn(p);
                p.sendMessage(ChatColor.GREEN + "Tu dragón fue invocado.");
            }
            case "despawn" -> {
                manager.despawn(p);
                p.sendMessage(ChatColor.YELLOW + "Tu dragón fue removido.");
            }
            case "staminareset" -> {
                stamina.reset(p);
                p.sendMessage(ChatColor.AQUA + "Stamina al máximo.");
            }
            default -> p.sendMessage(ChatColor.RED + "Subcomando desconocido.");
        }
        return true;
    }
}
