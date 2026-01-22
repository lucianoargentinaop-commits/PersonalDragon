package com.tuservidor.personaldragon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class DragonTickTask implements Runnable {

    private final PersonalDragonPlugin plugin;
    private final DragonManager manager;
    private final StaminaManager stamina;
    private final InputManager input;

    public DragonTickTask(PersonalDragonPlugin plugin, DragonManager manager, StaminaManager stamina, InputManager input) {
        this.plugin = plugin;
        this.manager = manager;
        this.stamina = stamina;
        this.input = input;
    }

    @Override
    public void run() {
        boolean useWsad = plugin.getConfig().getBoolean("controls.use_wsad", true);

        double forwardSpeed = plugin.getConfig().getDouble("controls.forward_speed", 1.10);
        double backwardSpeed = plugin.getConfig().getDouble("controls.backward_speed", 0.75);
        double yawTurnSpeed = plugin.getConfig().getDouble("controls.yaw_turn_speed", 4.0);

        double boostMult = plugin.getConfig().getDouble("controls.boost_multiplier", 1.65);

        double ascendPitch = plugin.getConfig().getDouble("controls.ascend_pitch", -18);
        double descendPitch = plugin.getConfig().getDouble("controls.descend_pitch", 28);
        double ascendSpeed = plugin.getConfig().getDouble("controls.ascend_speed", 0.55);
        double descendSpeed = plugin.getConfig().getDouble("controls.descend_speed", 0.45);

        int maxY = plugin.getConfig().getInt("limits.max_y", 320);

        double drainBoost = plugin.getConfig().getDouble("stamina.drain_per_tick_boost", 0.55);
        double regenMounted = plugin.getConfig().getDouble("stamina.regen_per_tick_mounted", 0.22);
        double regenOff = plugin.getConfig().getDouble("stamina.regen_per_tick_offmount", 0.50);
        boolean slowOnLow = plugin.getConfig().getBoolean("stamina.low_stamina_slowdown", true);
        double slowMult = plugin.getConfig().getDouble("stamina.slowdown_multiplier", 0.55);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!manager.hasDragon(p)) continue;

            DragonBundle b = manager.get(p);
            if (b == null || !b.isValid()) continue;

            manager.sync(p);

            boolean mounted = b.vehicle().getPassengers().contains(p);
            if (!mounted) {
                stamina.regen(p, regenOff);
                stamina.showBar(p, false, false);
                continue;
            }

            boolean wantsBoost = p.isSprinting();
            boolean canBoost = wantsBoost && stamina.has(p, drainBoost);

            if (canBoost) stamina.drain(p, drainBoost);
            else stamina.regen(p, regenMounted);

            double speedMult = canBoost ? boostMult : 1.0;
            if (slowOnLow && stamina.get(p) <= 1.0) speedMult *= slowMult;

            Location look = p.getLocation();
            Vector vel;

            // Leer input si la API lo soporta; si no, devolvemos 0,0,0 y usamos fallback.
            Vector in = input.read(p);
            boolean nativeInput = input.supportsNativeInput();

            if (useWsad && nativeInput) {
                // A/D = giro del dragón (yaw)
                float yaw = b.vehicle().getLocation().getYaw();
                yaw += (float) (-in.getX() * yawTurnSpeed);

                Location vLoc = b.vehicle().getLocation();
                vLoc.setYaw(yaw);
                b.vehicle().teleport(vLoc);

                // W/S = forward/back relativo al yaw del vehículo
                double strength = Math.min(1.0, Math.abs(in.getZ()));
                double baseSpeed = (in.getZ() >= 0 ? forwardSpeed : backwardSpeed) * strength;

                Vector forward = vLoc.getDirection().normalize().multiply(baseSpeed * speedMult);
                vel = forward;

            } else {
                // Fallback: modo cámara (si no hay soporte input)
                vel = look.getDirection().normalize().multiply(forwardSpeed * speedMult);
            }

            // Subir/bajar mirando arriba/abajo (pitch del jugador)
            float pitch = look.getPitch();
            if (pitch < ascendPitch) vel.setY(ascendSpeed);
            else if (pitch > descendPitch) vel.setY(-descendSpeed);
            else vel.setY(0.0);

            if (b.vehicle().getLocation().getY() >= maxY && vel.getY() > 0) vel.setY(0.0);

            b.vehicle().setVelocity(vel);

            stamina.showBar(p, canBoost, true);
        }
    }
}
