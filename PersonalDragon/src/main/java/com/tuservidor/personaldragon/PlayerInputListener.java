package com.tuservidor.personaldragon;

import io.papermc.paper.event.player.PlayerInputEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInputListener implements Listener {

    private final InputManager inputManager;

    public PlayerInputListener(InputManager inputManager) {
        this.inputManager = inputManager;
    }

    @EventHandler
    public void onInput(PlayerInputEvent event) {
        inputManager.update(
                event.getPlayer(),
                event.getInput().forward(),
                event.getInput().sideways()
        );
    }
}
