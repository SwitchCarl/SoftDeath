package org.yuyukon.softDeath;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class EventTrackers {

    private final JavaPlugin plugin;

    public EventTrackers(JavaPlugin plugin){
        this.plugin = plugin;
    }

    //method-title                                                                                                      |====提交时长：玩家追踪====>
    //.                                                                                                                 Submit TIME_SINCE_DEATH Per 60ticks Loop
    public static void trackPlayer(Player player, JavaPlugin plugin){
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline() && DeathDataManager.getInstance().getData(player).getRespawnDone()){
                    DeathDataManager.getInstance().getData(player).submitLastLivingTime((long) player.getStatistic(Statistic.TIME_SINCE_DEATH));
                    plugin.getServer().getLogger().info("" + player.getStatistic(Statistic.TIME_SINCE_DEATH));
                }
                else
                    cancel();
            }
        };
        task.runTaskTimer(plugin, 0, 60);
    }

    //method-title                                                                                                      |====标记管理：标记追踪====>
    //.                                                                                                                 Detect Marker Situation Per 20ticks Loop
    public void trackMarker(Item item, Player player) {

        UUID itemUUID = item.getUniqueId();
        // 这段耗了我一万年的时间。。。。。。他妈的为什么remove()有概率不能直接移除物品
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                Item currentItem = (Item) Bukkit.getEntity(itemUUID); // 通过 UUID 获取最新引用
                //if-title                                                                                              |===物品消失：提交苦痛：虚空===>
                if (currentItem == null || !currentItem.isValid()) {
                    DeathDataManager.getInstance().getData(player).submitDropEvent(4);
                    cancel();
                    return;
                }
                //if-title                                                                                              |===玩家已重生：清除物品===>
                if (DeathDataManager.getInstance().getData(player).getRespawnDone()) {// 若玩家已确定即将重生
                    item.remove();// 清除物品
                    currentItem.remove(); // 确保操作的是最新的实体引用
                    cancel();
                }
                //if-title                                                                                              |===玩家未重生：更新延迟===>
                else {
                    item.setPickupDelay(5000);// 更新物品捡起延迟，保证物品从未被捡起
                    DeathDataManager.getInstance().getData(player).setDropLocation(item.getLocation());// 提交坐标
                    if (player.getOpenInventory().getTopInventory().getSize() == 54) {
                        player.getOpenInventory().getTopInventory().setItem(49,  GUIContent.dropLocationShow(player));
                        player.getOpenInventory().getTopInventory().setItem(50, GUIContent.dropEventShow(player));
                    }
                    // 跟踪苦痛更新

                }
            }
        };
        task.runTaskTimer(plugin, 0, 20);
    }
}