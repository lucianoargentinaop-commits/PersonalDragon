package com.tuservidor.personaldragon;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;

public class InputManager {

    // Intentamos usar Player#getCurrentInput() si existe.
    // Si no existe (según tu Paper API), devolvemos (0,0,0) y el plugin puede fallback a modo cámara.
    private Method getCurrentInputMethod;

    public InputManager() {
        try {
            getCurrentInputMethod = Player.class.getMethod("getCurrentInput");
        } catch (NoSuchMethodException ignored) {
            getCurrentInputMethod = null;
        }
    }

    public boolean supportsNativeInput() {
        return getCurrentInputMethod != null;
    }

    public Vector read(Player p) {
        if (getCurrentInputMethod == null) return new Vector(0, 0, 0);

        try {
            Object inputObj = getCurrentInputMethod.invoke(p);
            if (inputObj == null) return new Vector(0, 0, 0);

            // Paper input suele tener forward() y sideways()
            Method forward = inputObj.getClass().getMethod("forward");
            Method sideways = inputObj.getClass().getMethod("sideways");

            float f = ((Number) forward.invoke(inputObj)).floatValue();   // W/S
            float s = ((Number) sideways.invoke(inputObj)).floatValue();  // A/D

            return new Vector(s, 0, f);
        } catch (Throwable t) {
            return new Vector(0, 0, 0);
        }
    }
}
