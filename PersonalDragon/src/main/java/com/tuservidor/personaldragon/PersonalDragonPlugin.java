package com.tuservidor.personaldragon;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PersonalDragonPlugin extends JavaPlugin {

    private DragonManager dragonManager;
    private StaminaManager staminaManager;

    private InputManager inputManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.staminaManager = new StaminaManager(this);
        this.dragonManager = new DragonManager(this, staminaManager);

        getCommand("pdragon").setExecutor(new PDragonCommand(this, dragonManager, staminaManager));
        Bukkit.getPluginManager().registerEvents(new PDragonListener(dragonManager), this);

        Bukkit.getScheduler().runTaskTimer(this, new DragonTickTask(this, dragonManager, staminaManager, inputManager), 1L, 1L);
    }

    @Override
    public void onDisable() {
        dragonManager.despawnAll();
    }
}
