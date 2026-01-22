package com.tuservidor.personaldragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DragonManager {

    private final PersonalDragonPlugin plugin;
    private final StaminaManager stamina;

    private final NamespacedKey keyOwner;
    private final Map<UUID, DragonBundle> dragons = new HashMap<>();

    public DragonManager(PersonalDragonPlugin plugin, StaminaManager stamina) {
        this.plugin = plugin;
        this.stamina = stamina;
        this.keyOwner = new NamespacedKey(plugin, "pdragon_owner");
    }

    public boolean hasDragon(Player p) {
        return dragons.containsKey(p.getUniqueId());
    }

    public DragonBundle get(Player p) {
        return dragons.get(p.getUniqueId());
    }

    public DragonBundle spawn(Player p) {
        if (hasDragon(p)) return get(p);

        World w = p.getWorld();
        Location base = p.getLocation().add(0, 1, 0);
        UUID owner = p.getUniqueId();

        ArmorStand vehicle = w.spawn(base, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setInvulnerable(true);
            as.setGravity(false);
            as.setCollidable(false);
            as.setSilent(true);
            as.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, owner.toString());
        });

        Interaction hitbox = w.spawn(base, Interaction.class, i -> {
            i.setInteractionWidth(3.2f);
            i.setInteractionHeight(2.6f);
        });

        ConfigurationSection palette = plugin.getConfig().getConfigurationSection("dragon.palette");
        if (palette == null) palette = plugin.getConfig().createSection("dragon.palette");

        List<BlockDisplay> parts = new ArrayList<>();
        List<?> raw = plugin.getConfig().getMapList("dragon.blocks");

        for (Object obj : raw) {
            if (!(obj instanceof List<?> entry) || entry.size() < 4) continue;

            double x = ((Number) entry.get(0)).doubleValue();
            double y = ((Number) entry.get(1)).doubleValue();
            double z = ((Number) entry.get(2)).doubleValue();
            String key = entry.get(3).toString();

            String matName = palette.getString(key, "BLACK_CONCRETE");
            Material mat;
            try {
                mat = Material.valueOf(matName);
            } catch (Exception ex) {
                mat = Material.BLACK_CONCRETE;
            }

            BlockData data = Bukkit.createBlockData(mat);

            Location spawnLoc = base.clone().add(x, y, z);
            BlockDisplay bd = w.spawn(spawnLoc, BlockDisplay.class, d -> {
                d.setBlock(data);
                d.setPersistent(false);
                d.setInvulnerable(true);
                d.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, owner.toString());
            });

            parts.add(bd);
        }

        DragonBundle bundle = new DragonBundle(owner, vehicle, hitbox, parts);
        dragons.put(owner, bundle);
        stamina.reset(p);
        return bundle;
    }

    public void despawn(Player p) {
        DragonBundle b = dragons.remove(p.getUniqueId());
        if (b != null) b.remove();
    }

    public void despawnAll() {
        for (DragonBundle b : dragons.values()) {
            try { b.remove(); } catch (Exception ignored) {}
        }
        dragons.clear();
    }

    public void mount(Player p) {
        DragonBundle b = get(p);
        if (b == null || !b.isValid()) return;
        if (!b.vehicle().getPassengers().contains(p)) b.vehicle().addPassenger(p);
    }

    public void sync(Player p) {
        DragonBundle b = get(p);
        if (b == null || !b.isValid()) return;

        Location base = b.vehicle().getLocation();

        List<?> raw = plugin.getConfig().getMapList("dragon.blocks");
        int i = 0;

        for (Object obj : raw) {
            if (i >= b.blocks().size()) break;
            if (!(obj instanceof List<?> entry) || entry.size() < 4) continue;

            double x = ((Number) entry.get(0)).doubleValue();
            double y = ((Number) entry.get(1)).doubleValue();
            double z = ((Number) entry.get(2)).doubleValue();

            Vector offset = new Vector(x, y, z).rotateAroundY(Math.toRadians(base.getYaw()));
            b.blocks().get(i).teleport(base.clone().add(offset));
            i++;
        }

        b.hitbox().teleport(base);
    }
}
