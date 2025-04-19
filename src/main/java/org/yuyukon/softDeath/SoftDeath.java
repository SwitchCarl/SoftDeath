package org.yuyukon.softDeath;

import org.bukkit.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class SoftDeath extends JavaPlugin implements Listener{

    public static NamespacedKey MARKER_OWNER;
    private PriceCalculate priceCalculate;


    @Override
    public void onEnable() {
        // Plugin startup logic

        // 注册监听
        getServer().getPluginManager().registerEvents(new EventListeners(this, new EventTrackers(this), new GUIContent()), this);
        getLogger().info("Plugin SoftDeath has been enabled!");

        saveDefaultConfig();
        super.reloadConfig();
        priceCalculate = new PriceCalculate(this);
        priceCalculate.loadConfig();

        // 注册指令
        Objects.requireNonNull(getCommand("testprice")).setExecutor(new TestPriceCommand());

        MARKER_OWNER = new NamespacedKey(this, "marker_owner");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        if (priceCalculate != null) {
            priceCalculate.loadConfig();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

}
