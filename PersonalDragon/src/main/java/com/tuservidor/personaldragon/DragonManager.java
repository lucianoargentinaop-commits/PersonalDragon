package com.tuservidor.personaldragon;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;

import java.util.*;

public class DragonManager {
    private final PersonalDragonPlugin plugin;
    private final StaminaManager stamina;

    private final NamespacedKey keyType;
    private final NamespacedKey keyOwner;

    private final Map<UUID, DragonBundle> bundles = new HashMap<>();

    public DragonManager(PersonalDragonPlugin plugin, StaminaManager stamina) {
        this.plugin = plugin;
        this.stamina = stamina;
        this.keyType = new NamespacedKey(plugin, "pdragon_type");
        this.keyOwner = new NamespacedKey(plugin, "pdragon_owner");
    }

    public boolean hasDragon(Player p) {
        DragonBundle b = bundles.get(p.getUniqueId());
        return b != null && b.isValid();
    }

    public DragonBundle getBundle(Player p) {
        return bundles.get(p.getUniqueId());
    }

    public boolean isOurEntity(Entity e) {
        return e.getPersistentDataContainer().has(keyType, PersistentDataType.STRING);
    }

    public UUID getOwner(Entity e) {
        String s = e.getPersistentDataContainer().get(keyOwner, PersistentDataType.STRING);
        if (s == null) return null;
        try { return UUID.fromString(s); } catch (Exception ex) { return null; }
    }

    private void tag(Entity e, String type, UUID owner) {
        e.getPersistentDataContainer().set(keyType, PersistentDataType.STRING, type);
        e.getPersistentDataContainer().set(keyOwner, PersistentDataType.STRING, owner.toString());
    }

    public DragonBundle spawn(Player p) {
        if (hasDragon(p)) return getBundle(p);

        Location loc = p.getLocation().add(p.getLocation().getDirection().normalize().multiply(2));
        World w = loc.getWorld();
        if (w == null) return null;

        UUID owner = p.getUniqueId();

        ArmorStand vehicle = w.spawn(loc, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setInvulnerable(true);
            as.setGravity(false);
            as.setCollidable(false);
            as.setSilent(true);
            as.setMarker(false);
            as.setArms(false);
            as.setBasePlate(false);
            as.setSmall(false);
            tag(as, "vehicle", owner);
        });

        Interaction hitbox = w.spawn(loc, Interaction.class, i -> {
            i.setInteractionWidth(3.0f);
            i.setInteractionHeight(2.2f);
            tag(i, "hitbox", owner);
        });

        Material headMat = Material.matchMaterial(plugin.getConfig().getString("visual.head_item", "DRAGON_HEAD"));
        if (headMat == null) headMat = Material.DRAGON_HEAD;

        Material bodyMat = Material.matchMaterial(plugin.getConfig().getString("visual.body_item", "BLACK_CONCRETE"));
        if (bodyMat == null) bodyMat = Material.BLACK_CONCRETE;

        Material wingMat = Material.matchMaterial(plugin.getConfig().getString("visual.wing_item", "ELYTRA"));
        if (wingMat == null) wingMat = Material.ELYTRA;

        Material tailMat = Material.matchMaterial(plugin.getConfig().getString("visual.tail_item", "BLACKSTONE"));
        if (tailMat == null) tailMat = Material.BLACKSTONE;

        float scale = (float) plugin.getConfig().getDouble("defaults.scale", 1.3);

        ItemDisplay head = spawnDisplay(w, loc, headMat, scale, owner, "head");
        ItemDisplay body = spawnDisplay(w, loc, bodyMat, scale, owner, "body");
        ItemDisplay wingL = spawnDisplay(w, loc, wingMat, scale, owner, "wingL");
        ItemDisplay wingR = spawnDisplay(w, loc, wingMat, scale, owner, "wingR");
        ItemDisplay tail = spawnDisplay(w, loc, tailMat, scale, owner, "tail");

        DragonBundle b = new DragonBundle(owner, vehicle, hitbox, head, body, wingL, wingR, tail);
        bundles.put(owner, b);

        stamina.reset(p);
        return b;
    }

    private ItemDisplay spawnDisplay(World w, Location loc, Material mat, float scale, UUID owner, String type) {
        return w.spawn(loc, ItemDisplay.class, d -> {
            d.setItemStack(new org.bukkit.inventory.ItemStack(mat));
            d.setPersistent(false);
            d.setInvulnerable(true);
            tag(d, type, owner);

            Transformation t = d.getTransformation();
            t.getScale().set(scale, scale, scale);
            d.setTransformation(t);
        });
    }

    public void despawn(Player p) {
        DragonBundle b = bundles.remove(p.getUniqueId());
        if (b != null) b.remove();
    }

    public void despawnAll() {
        for (DragonBundle b : bundles.values()) b.remove();
        bundles.clear();
    }

    public void mount(Player p) {
        DragonBundle b = getBundle(p);
        if (b == null || !b.isValid()) return;
        if (!b.vehicle().getPassengers().contains(p)) {
            b.vehicle().addPassenger(p);
        }
    }

    public void syncVisual(Player p) {
        DragonBundle b = getBundle(p);
        if (b == null || !b.isValid()) return;

        Location base = b.vehicle().getLocation();
        float yaw = base.getYaw();

        if (b.hitbox().getLocation().distanceSquared(base) > 0.25) b.hitbox().teleport(base);

        Location headLoc = base.clone().add(base.getDirection().normalize().multiply(1.2)).add(0, 1.2, 0);
        Location bodyLoc = base.clone().add(0, 1.0, 0);
        Location tailLoc = base.clone().add(base.getDirection().normalize().multiply(-1.1)).add(0, 0.9, 0);

        double rad = Math.toRadians(yaw);
        double rightX = -Math.sin(rad);
        double rightZ =  Math.cos(rad);

        Location wingRLoc = base.clone().add(rightX * 1.4, 1.1, rightZ * 1.4);
        Location wingLLoc = base.clone().add(-rightX * 1.4, 1.1, -rightZ * 1.4);

        b.head().teleport(headLoc);
        b.body().teleport(bodyLoc);
        b.tail().teleport(tailLoc);
        b.wingR().teleport(wingRLoc);
        b.wingL().teleport(wingLLoc);

        b.head().setRotation(yaw, 0);
        b.body().setRotation(yaw, 0);
        b.tail().setRotation(yaw, 0);
        b.wingR().setRotation(yaw + 20, 0);
        b.wingL().setRotation(yaw - 20, 0);
    }
}
