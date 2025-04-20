package org.yuyukon.softDeath;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getLogger;
import static org.bukkit.Bukkit.getServer;
import static org.yuyukon.softDeath.SoftDeath.MARKER_OWNER;

public class EventListeners implements Listener {

    private final JavaPlugin plugin;
    private final EventTrackers eventTrackers;
    private final GUIContent guiContent;

    private static List<Material> cheapBlockItem;

    //method-title                                                                                                      |====监听注册====>
    public EventListeners(JavaPlugin plugin, EventTrackers eventTrackers, GUIContent guiContent){
        this.plugin = plugin;
        this.eventTrackers = eventTrackers;
        this.guiContent = guiContent;

        // 导入部分特价信息
        FileConfiguration config = plugin.getConfig();
        cheapBlockItem = DeepClone.convertStringListToMaterials(config.getStringList("cheap-block-item"));
    }

    //method-title                                                                                                      |====玩家注册：进服监听====>
    @EventHandler//                                                                                                     Register DeathData and Start Tracking
    public void onPlayerJoin(PlayerJoinEvent event){
        // 注册信息
        if (DeathDataManager.getInstance().getData(event.getPlayer()) == null){
            DeathDataManager.getInstance().storeData(event.getPlayer().getUniqueId(), new DeathData());
        }
        // 若仍处于未重生状态，20tick后启动gui
        if (!DeathDataManager.getInstance().getData(event.getPlayer()).getRespawnDone())
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                guiContent.guiInitialize(event.getPlayer());
            }, 20L);
        // 开始追踪
        eventTrackers.trackPlayer(event.getPlayer());
    }

    //method-title                                                                                                      |====死亡快照：死亡监听====>
    @EventHandler//                                                                                                     Store Series of Info when Die
    public void onPlayerDeath(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        assert player != null;
        player.setGameMode(GameMode.SPECTATOR);
        DeathDataManager.getInstance().getData(player).clearDropEvent();

        //region-title                                                                                                  |==全物品栏==>
                                                                                                                        //region get All Inventory Contents as ItemStack[]
        // 获取全物品栏
        PlayerInventory realInventory = player.getInventory();
        ItemStack[] allInventoryContents = new ItemStack[41];
        // 主物品栏（36 格）
        System.arraycopy(realInventory.getContents(), 0, allInventoryContents, 0, 36);
        // 盔甲槽（4 格）
        System.arraycopy(realInventory.getArmorContents(), 0, allInventoryContents, 36, 4);
        // 副手（1 格）
        allInventoryContents[40] = realInventory.getItemInOffHand();
        //endregion

        //region-title                                                                                                  |==死亡快照==>
                                                                                                                        //region Death Snapshot
        // 按下快门，死亡快照已建立！可以随便改动的全局数据！好耶！

        DeathDataManager.getInstance().getData(player).submitDeathData(
                // 获取死亡地点
                player.getLocation(),
                // 深拷贝一份玩家的物品栏
                DeepClone.deepCloneInventory(allInventoryContents),
                // 获取玩家的总经验（待改进）
                player.getTotalExperience(),
                // 获取平均存活时间
                (double) player.getStatistic(Statistic.PLAY_ONE_MINUTE) /player.getStatistic(Statistic.DEATHS)
        );
        //endregion

        //region-title                                                                                                  |==丢出标记==>
                                                                                                                        //region Create Marker
        // 标记掉落物的物品叠
        ItemStack markerStack = new ItemStack(Material.COMMAND_BLOCK);
        // 标记掉落物
        Item marker = Objects.requireNonNull(player.getLocation().getWorld()).dropItem(player.getLocation(), markerStack);
        // 给标记掉落物上一个 MARKER_OWNER 键，内容为玩家的名字
        marker.getPersistentDataContainer().set(MARKER_OWNER, PersistentDataType.STRING, player.getName());
        marker.setUnlimitedLifetime(true);
        marker.setVisibleByDefault(false);
        eventTrackers.trackMarker(marker, player);
        //endregion
    }

    //method-title                                                                                                      |====界面处理：重生监听====>
    @EventHandler//                                                                                                     Frozen Player and Clear Inventory to Open GUI
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        //region-title                                                                                                  |==冻结玩家==>
                                                                                                                        //region Frozen Player
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
        player.setAllowFlight(true);
        player.setFlying(true);
        // 50级缓慢
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                Integer.MAX_VALUE,
                50,
                false,
                false
        ));
        // 50级失明
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                Integer.MAX_VALUE,
                1,
                false,
                false
        ));
        //endregion

        //region-title                                                                                                  |==孟婆例汤==>
                                                                                                                        //region Clear Inventory and Experience

        // 延迟5tick清除玩家的背包与经验
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PlayerInventory realInventory = player.getInventory();
            // 清空主物品栏（0~35格）
            realInventory.clear();
            // 清空盔甲槽（头盔、胸甲、护腿、靴子）
            realInventory.setHelmet(null);
            realInventory.setChestplate(null);
            realInventory.setLeggings(null);
            realInventory.setBoots(null);
            // 清空副手
            realInventory.setItemInOffHand(null);
            // 清空经验值
            player.setTotalExperience(0);
            player.setLevel(0);
            player.setExp(0);
        }, 5L);
        //endregion

        // 音效：信标切换效果（延迟10tick）
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
        }, 10L);

        // 启动GUI
        guiContent.guiInitialize(player);
    }

    //method-title                                                                                                      |====提交苦痛：标记监听====>
    @EventHandler//                                                                                                     Submit if Anything Occur to Marker
    public void dropEventDealer(EntityDamageEvent event){
        if (event.getEntity() instanceof Item item){
            // 获取匹配的玩家
            if (item.getPersistentDataContainer().get(MARKER_OWNER, PersistentDataType.STRING) != null){ // 如果 这个物品有 MARKER_OWNER 标签
                // 通过名字获取这个玩家
                Player player = Bukkit.getPlayer(Objects.requireNonNull(item.getPersistentDataContainer().get(MARKER_OWNER, PersistentDataType.STRING)));
                // 如果这个玩家已经重生完毕了
                if (DeathDataManager.getInstance().getData(player).getRespawnDone())
                    item.remove();
                else {
                    event.setCancelled(true);
                    DeathData deathData = DeathDataManager.getInstance().getData(player);
                    //region-title                                                                                      |==提交苦难==>
                                                                                                                        //region Submit Drop Event
                    switch (event.getCause()) {
                        // 火
                        case EntityDamageEvent.DamageCause.FIRE:
                            deathData.submitDropEvent(0);
                            break;
                        // 岩浆
                        case EntityDamageEvent.DamageCause.LAVA:
                            deathData.submitDropEvent(1);
                            break;
                        // 爆炸
                        case EntityDamageEvent.DamageCause.BLOCK_EXPLOSION:
                            deathData.submitDropEvent(2);
                            break;
                        // 仙人掌刺伤
                        case EntityDamageEvent.DamageCause.CONTACT:
                            deathData.submitDropEvent(3);
                            break;
                    }
                    //endregion
                }
            }
        }
    }

    //method-title                                                                                                      |====界面保护：关闭监听====>
    @EventHandler//                                                                                                     Reopen GUI when Unexpectedly Closed
    public void avoidGUIClose(InventoryCloseEvent event){
        if (!DeathDataManager.getInstance().getData((Player) event.getPlayer()).getRespawnDone()){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                guiContent.guiInitialize((Player) event.getPlayer());
            }, 2L);
        }
    }

    //method-title                                                                                                      |====点击物品：点击监听====>
    @EventHandler//                                                                                                     Deal if Any Items Clicked
    public void onInventoryClick(InventoryClickEvent event) {
        // 若是本插件创建的GUI被点击
        if (!DeathDataManager.getInstance().getData((Player) event.getWhoClicked()).getRespawnDone() && event.getCurrentItem() != null) {
            Player player = (Player) event.getWhoClicked();
            // 点击的物品叫谷子（
            ItemStack goods = event.getCurrentItem();

            //if-title                                                                                                  |===火箭：广播并更新坐标===>
            if (event.getSlot() == 48) {
                event.getInventory().setItem(49, GUIContent.dropLocationShow(player));

                player.playSound(player, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0f, 1.0f);
                Location dropLocation = DeathDataManager.getInstance().getData(player).getDropLocation();
                String dimensionName = switch (dropLocation.getWorld().getName()) {
                    case "world" -> (ChatColor.DARK_GREEN + "主世界");
                    case "world_nether" -> (ChatColor.DARK_RED + "下界");
                    case "world_the_end" -> (ChatColor.DARK_PURPLE + "末地");
                    default -> null;
                };
                getServer().broadcastMessage("玩家 " + ChatColor.GOLD + player.getName() + ChatColor.WHITE +
                        " 的掉落物正位于" + dimensionName +
                        ChatColor.WHITE + "的 " + ChatColor.GOLD + "(" +
                        BigDecimal.valueOf(dropLocation.getX()).setScale(2, RoundingMode.HALF_UP) + ", " +
                        BigDecimal.valueOf(dropLocation.getY()).setScale(2, RoundingMode.HALF_UP) + ", " +
                        BigDecimal.valueOf(dropLocation.getZ()).setScale(2, RoundingMode.HALF_UP) + ") " +
                        ChatColor.WHITE + "处！");
            }

            //if-title                                                                                                  |===屏障：退出===>
            if (event.getSlot() == 53){
                event.setCancelled(true);

                // 重新赋予剩余的经验
                player.giveExp((int) (0.7f * DeathDataManager.getInstance().getData(player).getMoney()));

                //region-title                                                                                          |===解冻玩家===>
                                                                                                                        //region Unfreeze Player
                player.setGameMode(GameMode.SURVIVAL);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.removePotionEffect(PotionEffectType.SLOWNESS);
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                //endregion

                //region-title                                                                                          |===掉落物品===>
                                                                                                                        //region Drop Inventory Items
                DeathData deathData = DeathDataManager.getInstance().getData(player);
                assert player.getLastDeathLocation() != null;
                for (int i = 0; i <= 40; i++){
                    if (deathData.getInventory()[i] != null){
                        // 若 Marker 遭受虚空
                        if (deathData.getDropDiedFromCactus())
                            // 取消掉落物
                            break;
                        // 若 Marker 遭受爆炸
                        if (deathData.getDropDiedFromExplosion()){
                            // 仅下界之星可以掉落
                            if (deathData.getInventory()[i].getType() == Material.NETHER_STAR)
                                Objects.requireNonNull(player.getLastDeathLocation().getWorld()).dropItem(deathData.getDropLocation(),
                                        deathData.getInventory()[i]);
                        }
                        // 若 Marker 遭受火焰或岩浆
                        if (deathData.getDropInLava() || deathData.getDropOnFire()){
                            // 仅合金装备可以掉落
                            switch (deathData.getInventory()[i].getType()){
                                case NETHERITE_AXE :
                                case NETHERITE_CHESTPLATE:
                                case NETHERITE_BLOCK:
                                case NETHERITE_BOOTS:
                                case NETHERITE_HELMET:
                                case NETHERITE_HOE:
                                case NETHERITE_INGOT:
                                case NETHERITE_LEGGINGS:
                                case NETHERITE_PICKAXE:
                                case NETHERITE_SCRAP:
                                case NETHERITE_SHOVEL:
                                case NETHERITE_SWORD:
                                case NETHERITE_UPGRADE_SMITHING_TEMPLATE:
                                case LODESTONE:
                                case ANCIENT_DEBRIS: {
                                    Objects.requireNonNull(player.getLastDeathLocation().getWorld()).dropItem(deathData.getDropLocation(),
                                            deathData.getInventory()[i]);
                                    break;
                                }
                            }
                        }
                        // 若 Marker 安然无恙
                        if (!deathData.getDropInLava() &&
                                !deathData.getDropOnFire() &&
                                !deathData.getDropDiedFromVoid() &&
                                !deathData.getDropDiedFromExplosion())
                            Objects.requireNonNull(player.getLastDeathLocation().getWorld()).dropItem(deathData.getDropLocation(),
                                    deathData.getInventory()[i]);
                    }
                }
                //endregion

                deathData.doRespawnDone();
                // 2tick后关闭GUI
                Bukkit.getScheduler().runTaskLater(plugin, player::closeInventory, 5L);
            }

            //if-title                                                                                                  |===玩家物品：预测价格===>
            if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getSlot() < 45){
                event.setCancelled(true);
                assert goods != null;
                event.getView().setTitle(GUIContent.guiTitle(PriceCalculate.getPrice(goods, player), player, true));
            }

            //if-title                                                                                                  |===玩家物品+shift：赎回物品===>
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getSlot() < 45) {

                DeathData deathData = DeathDataManager.getInstance().getData(player);

                event.setCancelled(true);
                assert goods != null;
                PlayerInventory realInventory = (PlayerInventory) player.getOpenInventory().getBottomInventory();
                if (PriceCalculate.getPrice(goods, player) <= DeathDataManager.getInstance().getData(player).getMoney()) {
                    int slot = event.getSlot();
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

                    //region-title                                                                                      |===赎回物品===>
                                                                                                                        //region Buy Items Back
                    // 缴费
                    deathData.spendMoney(PriceCalculate.getPrice(goods, player));

                    // 给予玩家物品
                    if (slot <= 40){
                        realInventory.setItem(event.getSlot(), deathData.getInventory()[slot]);
                    } else {
                        switch (slot){
                            case 41:
                                realInventory.setHelmet(deathData.getInventory()[41]);
                                break;
                            case 42:
                                realInventory.setChestplate(deathData.getInventory()[42]);
                                break;
                            case 43:
                                realInventory.setLeggings(deathData.getInventory()[43]);
                                break;
                            case 44:
                                realInventory.setBoots(deathData.getInventory()[44]);
                                break;
                            case 45:
                                realInventory.setItemInOffHand(deathData.getInventory()[45]);
                                break;
                        }
                    }
                    // 扣除归档物品栏中的物品
                    deathData.buyInventory(event.getSlot());
                    // 扣除GUI中的物品
                    event.getView().getTopInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    // 若使用特价
                    if (PriceCalculate.getPrice(goods, player) == 0){
                        switch (goods.getType()){
                            case WATER_BUCKET :
                                if (deathData.getIsDiscountAvailable(0))
                                    deathData.spendDiscount(0);
                                break;
                            case IRON_SWORD:
                            case IRON_AXE:
                            case IRON_PICKAXE:
                            case SHIELD:
                                if (deathData.getIsDiscountAvailable(1))
                                    deathData.spendDiscount(1);
                                break;
                        }
                        if (cheapBlockItem.contains(goods.getType()))
                            if (deathData.getIsDiscountAvailable(2))
                                deathData.spendDiscount(2);
                        event.getView().close();
                    }
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        event.getView().setTitle(GUIContent.guiTitle(deathData.getMoney(), player, false));
                    }, 5L);
                    //endregion
                }
                else {
                    event.setCancelled(true);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                }
                // 更新ui！
                event.getView().getTopInventory().setItem(51, GUIContent.expShow(deathData.getMoney()));
                event.getView().getTopInventory().setItem(46, GUIContent.discountShow(player));
            }
        }
    }
}