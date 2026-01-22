package com.tuservidor.personaldragon;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.*;

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
            as.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, owner.toString());
        });

        Interaction hitbox = w.spawn(base, Interaction.class, i -> {
            i.setInteractionWidth(3f);
            i.setInteractionHeight(2.5f);
        });

        List<BlockDisplay> blocks = new ArrayList<>();
        ConfigurationSection palette = plugin.getConfig().getConfigurationSection("dragon.palette");

        for (List<?> entry : plugin.getConfig().getMapList("dragon.blocks")) {
            double x = ((Number) entry.get(0)).doubleValue();
            double y = ((Number) entry.get(1)).doubleValue();
            double z = ((Number) entry.get(2)).doubleValue();
            String key = entry.get(3).toString();

            Material mat = Material.valueOf(palette.getString(key));
            BlockDisplay bd = w.spawn(base.clone().add(x, y, z), BlockDisplay.class);
            bd.setBlock(Bukkit.createBlockData(mat));
            bd.setTransformation(bd.getTransformation());
            blocks.add(bd);
        }

        DragonBundle bundle = new DragonBundle(owner, vehicle, hitbox, blocks);
        dragons.put(owner, bundle);
        stamina.reset(p);
        return bundle;
    }

    public void despawn(Player p) {
        DragonBundle b = dragons.remove(p.getUniqueId());
        if (b != null) b.remove();
    }

    public void sync(Player p) {
        DragonBundle b = get(p);
        if (b == null) return;

        Location base = b.vehicle().getLocation();
        int i = 0;

        for (List<?> entry : plugin.getConfig().getMapList("dragon.blocks")) {
            double x = ((Number) entry.get(0)).doubleValue();
            double y = ((Number) entry.get(1)).doubleValue();
            double z = ((Number) entry.get(2)).doubleValue();

            Vector offset = new Vector(x, y, z).rotateAroundY(Math.toRadians(base.getYaw()));
            b.blocks().get(i++).teleport(base.clone().add(offset));
        }

        b.hitbox().teleport(base);
    }
}
