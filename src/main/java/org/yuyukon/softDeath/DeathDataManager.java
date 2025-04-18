package org.yuyukon.softDeath;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DeathDataManager {
    private static DeathDataManager instance;
    private final Map<UUID, DeathData> deathDataMap= new ConcurrentHashMap<>();

    public static DeathDataManager getInstance() {
        if (instance == null){
            instance = new DeathDataManager();
        }
        return instance;
    }

    public void storeData(UUID uuid, DeathData data){
        deathDataMap.put(uuid, data);
    }

    public DeathData getData(Player player){
        return deathDataMap.get(player.getUniqueId());
    }
}
