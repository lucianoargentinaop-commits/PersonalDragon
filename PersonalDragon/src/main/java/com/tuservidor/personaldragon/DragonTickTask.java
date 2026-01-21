package com.tuservidor.personaldragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DragonTickTask implements Runnable {

    private final PersonalDragonPlugin plugin;
    private final DragonManager manager;
    private final StaminaManager stamina;

    public DragonTickTask(PersonalDragonPlugin plugin, DragonManager manager, StaminaManager stamina) {
        this.plugin = plugin;
        this.manager = manager;
        this.stamina = stamina;
    }

    @Override
    public void run() {
        double speed = plugin.getConfig().getDouble("flight.speed", 0.9);
        double boostSpeed = plugin.getConfig().getDouble("flight.boost_speed", 1.6);

        double ascendPitch = plugin.getConfig().getDouble("flight.ascend_pitch", -20);
        double descendPitch = plugin.getConfig().getDouble("flight.descend_pitch", 25);
        double ascendSpeed = plugin.getConfig().getDouble("flight.ascend_speed", 0.55);
        double descendSpeed = plugin.getConfig().getDouble("flight.descend_speed", 0.40);

        int maxY = plugin.getConfig().getInt("flight.max_y", 320);

        double drainBoost = plugin.getConfig().getDouble("stamina.drain_per_tick_boost", 0.45);
        double regenNormal = plugin.getConfig().getDouble("stamina.regen_per_tick_normal", 0.20);
        double regenOff = plugin.getConfig().getDouble("stamina.regen_per_tick_offmount", 0.45);

        boolean slowOnLow = plugin.getConfig().getBoolean("stamina.low_stamina_slowdown", true);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!manager.hasDragon(p)) continue;

            DragonBundle b = manager.getBundle(p);
            if (b == null || !b.isValid()) continue;

            manager.syncVisual(p);

            boolean mounted = b.vehicle().getPassengers().contains(p);

            if (!mounted) {
                stamina.regen(p, regenOff);
                stamina.showBar(p, false, false);
                continue;
            }

            boolean wantsBoost = p.isSprinting();
            boolean canBoost = wantsBoost && stamina.has(p, drainBoost);

            if (canBoost) stamina.drain(p, drainBoost);
            else stamina.regen(p, regenNormal);

            double usedSpeed = canBoost ? boostSpeed : speed;
            if (slowOnLow && stamina.get(p) <= 1.0) usedSpeed *= 0.55;

            Location look = p.getLocation();
            Vector dir = look.getDirection().normalize().multiply(usedSpeed);

            float pitch = look.getPitch();
            if (pitch < ascendPitch) dir.setY(ascendSpeed);
            else if (pitch > descendPitch) dir.setY(-descendSpeed);
            else dir.setY(0.0);

            if (b.vehicle().getLocation().getY() >= maxY && dir.getY() > 0) dir.setY(0.0);

            b.vehicle().setVelocity(dir);

            stamina.showBar(p, canBoost, true);
        }
    }
}
