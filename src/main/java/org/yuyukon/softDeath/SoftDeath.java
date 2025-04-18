package org.yuyukon.softDeath;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


public final class SoftDeath extends JavaPlugin implements Listener{

    public static NamespacedKey MARKER_OWNER;

    @Override
    public void onEnable() {
        // Plugin startup logic

        // 注册监听
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Plugin SoftDeath has been enabled!");

        // 注册指令
        getCommand("testprice").setExecutor(new TestPriceCommand());


        MARKER_OWNER = new NamespacedKey(this, "marker_owner");
    }



    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    //method-title                                                                                                      |====深度克隆====>
    public static ItemStack[] deepCloneInventory(ItemStack[] original) {
        if (original == null) {
            return new ItemStack[0];
        }
        ItemStack[] cloned = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            if (original[i] == null) {
                cloned[i] = null;
                continue;
            }
            cloned[i] = original[i].clone();
        }
        return cloned;
    }

    //method-title                                                                                                      |====计算价格====>
    public int getPrice(ItemStack iS, Player player) {

        double Kl, Kt, T, M, E, S;// 玩家水平系数；上次死亡时间系数；物品类型系数；物品材质系数；物品附魔系数；物品堆叠系数；

        Kl = DeathDataManager.getInstance().getData(player).getKl();
        Kt = DeathDataManager.getInstance().getData(player).getKt();
        M = 1;
        T = 1;
        E = 1;
        // 物品堆叠系数
        S = 0.13 * Math.log(iS.getAmount()) + 1 ;

        //region-title                                                                                                  |===类别系数===>
        //region Calculate Type Coefficient
        // 稀有物品类
        switch (iS.getType()){
            case ENCHANTED_GOLDEN_APPLE :   // 附魔金苹果
            case TRIAL_KEY :                // 试炼钥匙
            case OMINOUS_TRIAL_KEY :        // 不详试炼钥匙
            case NETHER_STAR :              // 下界之星
            case ELYTRA :                   // 鞘翅
            case HEAVY_CORE :               // 沉重核心
            case HEART_OF_THE_SEA :         // 海洋之心
            case NETHERITE_INGOT :          // 下界合金锭
            case NETHERITE_BLOCK:           // 下界合金块
            case DRAGON_EGG:                // 龙蛋
            case DRAGON_HEAD:               // 龙首
            case TOTEM_OF_UNDYING:          // 不死图腾
            case BEACON:                    // 信标
                T = 6.0 ;
                break;
        }
        // 盔甲类
        if (iS.getType().name().endsWith("_CHESTPLATE") ||      // 胸甲
                iS.getType().name().endsWith("_BOOTS") ||       // 靴子
                iS.getType().name().endsWith("_LEGGINGS") ||    // 护腿
                iS.getType().name().endsWith("_HELMET")){       // 头甲（误
            T = 3.75 ;
            M = 0;// 标识需要判断材质
        }
        // 高级资源类
        switch (iS.getType()){
            case EXPERIENCE_BOTTLE :        // 附魔之瓶
            case ENDER_EYE :                // 末影之眼
            case DIAMOND_HORSE_ARMOR:       // 钻石马铠
            case GOLDEN_HORSE_ARMOR:        // 金马铠
            case GOAT_HORN:                 // 山羊角
            case RECOVERY_COMPASS:          // 追溯指针
            case WOLF_ARMOR:                // 狼铠
            case LADDER:                    // 鞍
            case DRAGON_BREATH:             // 龙息
            case DIAMOND:                   // 钻石
            case DIAMOND_BLOCK:             // 钻石块
            case DIAMOND_ORE:               // 钻石矿石
            case DEEPSLATE_DIAMOND_ORE:     // 深层钻石矿石
            case ANCIENT_DEBRIS:            // 远古残骸
            case NETHERITE_SCRAP:           // 下界合金碎片
            case DISC_FRAGMENT_5:           // 唱片残片
            case SHULKER_SHELL:             // 潜影壳
            case SHULKER_BOX:               // 潜影盒
            case TURTLE_SCUTE:              // 海龟鳞甲
            case ENCHANTING_TABLE:          // 附魔台
            case OCHRE_FROGLIGHT:           // 赭黄蛙明灯
            case VERDANT_FROGLIGHT:         // 青翠蛙明灯
            case PEARLESCENT_FROGLIGHT:     // 珠光蛙明灯
            case END_CRYSTAL:               // 末影水晶
            case ECHO_SHARD:                // 回响碎片
            case ENCHANTED_BOOK:            // 附魔书
                T = 1.5;
                break;
        }
        if (iS.getType().name().startsWith("MUSIC_DISC")||      // 音乐唱片
                iS.getType().name().endsWith("SMITHING_TEMPLATE"))  // 锻造模板
            T = 1.9;

        // 工具与材料武器类
        if (iS.getType().name().endsWith("_AXE") ||             // 斧
                iS.getType().name().endsWith("_PICKAXE") ||     // 镐
                iS.getType().name().endsWith("_HOE") ||         // 锄
                iS.getType().name().endsWith("_SHOVEL") ||      // 锹
                iS.getType().name().endsWith("_SWORD") ||       // 剑
                iS.getType().name().equals("MACE") ||           // 亚当
                iS.getType().name().equals("TRIDENT") ||        // 三叉戟
                iS.getType().name().equals("SHIELD") ||         // 盾
                iS.getType().name().equals("CROSSBOW") ||       // 弩
                iS.getType().name().equals("BOW")){             // 弓
            T = 3.4 ;
            M = 0;
        }
        //endregion

        //region-title                                                                                                  |===材质系数===>
        //region Material Coefficient
        if (M == 0){
            if (iS.getType().name().startsWith("DIAMOND_"))
                M = 1.3;
            else if (iS.getType().name().startsWith("NETHERITE_"))
                M = 1.4;
            else
                M = 1;
        }
        //endregion

        // 附魔系数
        if (!iS.getItemMeta().getEnchants().isEmpty()){
            E = 1.25;
        }

        // 优惠启用
        switch (iS.getType()){
            case WATER_BUCKET :
                if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(0))
                    return 0;
                break;
            case IRON_SWORD:
            case IRON_AXE:
            case IRON_PICKAXE:
            case SHIELD:
                if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(1))
                    return 0;
                break;
            case DIRT:
            case STONE:
            case COBBLESTONE:
            case NETHERRACK:
                if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(2))
                    return 0;
                break;
        }

        return BigDecimal.valueOf(16 * Kl * Kt * M * E * T * S).setScale(0, RoundingMode.HALF_UP).intValue();
    }


    //method-title                                                                                                      |====可视价格====>
    public String guiTitle(int money, Player player, boolean isUsedInForecast) {

        int expOnDeath = DeathDataManager.getInstance().getData(player).getExpOnDeath();

        StringBuilder title = new StringBuilder();

        if (expOnDeath != 0) {// 死亡经验不为0
            if (!isUsedInForecast) {// 普通标题
                title.append("|".repeat( Math.max ((money * 81) / expOnDeath , 0)));
                if ((money * 81) / expOnDeath > 81)
                    title = new StringBuilder(ChatColor.DARK_RED + "***");
            } else {// 预测标题
                int realMoney = DeathDataManager.getInstance().getData(player).getMoney();
                if (realMoney >= money)
                    title.append("|".repeat(Math.max(0, (( realMoney - money ) * 81) / expOnDeath))).append(ChatColor.GREEN).append("|".repeat(Math.max(0, (money * 81) / expOnDeath)));
                else
                    title.append(ChatColor.DARK_RED).append("|".repeat(Math.max(0, 81)));
                getServer().broadcastMessage(title.toString());
            }
        }else
            title = new StringBuilder("*");
        return title.toString();
    }

    //method-title                                                                                                      |====界面显示====>
    public void guiInitialize(Player player) {

        Inventory displayInventory = Bukkit.createInventory(null, 54, guiTitle(DeathDataManager.getInstance().getData(player).getMoney(), player, false));
        ItemStack[] allInventoryContentDisplay = deepCloneInventory(DeathDataManager.getInstance().getData(player).getInventory());

        //region-title                                                                                                  |==价格显示==>
        //region Showcase Prices

        // 遍历玩家物品栏，添加价格描述后显示于UI
        if ( allInventoryContentDisplay.length != 0) {
            for (int i = 0; i < 41; i++)
                if (allInventoryContentDisplay[i] != null) {
                    ItemMeta meta;
                    meta = allInventoryContentDisplay[i].getItemMeta();
                    // 在物品描述里附上价格
                    if (!allInventoryContentDisplay[i].getType().isAir()) {
                        assert meta != null;
                        if (meta.hasLore()){
                            meta.getLore().add(ChatColor.GREEN + "价格：" + getPrice(allInventoryContentDisplay[i], player));
                            meta.getLore().add(ChatColor.GREEN + "[" + guiTitle(getPrice(allInventoryContentDisplay[i], player), player, false) + ChatColor.GREEN + "]");
                        }
                        else {
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GREEN + "价格：" + getPrice(allInventoryContentDisplay[i], player) );
                            lore.add(ChatColor.GREEN + "[" + guiTitle(getPrice(allInventoryContentDisplay[i], player), player, false) + ChatColor.GREEN + "]");
                            meta.setLore(lore);
                        }
                    }
                    allInventoryContentDisplay[i].setItemMeta(meta);
                    displayInventory.setItem(i, allInventoryContentDisplay[i]);
                }
        }
        //endregion

        //region-title                                                                                                  |==填充装饰==>
        //region Fill Decoration

        // 填充用于显示特价的告示牌
        displayInventory.setItem(46, discountShow(player));

        // 填充用于显示玩家价格系数的书
        ItemStack itemBook = new ItemStack(Material.BOOK);
        ItemMeta metaBook = itemBook.getItemMeta();
        assert metaBook != null;
        List<String> loreBook= new ArrayList<>();
        float Kl = DeathDataManager.getInstance().getData(player).getKl();
        float Kt = DeathDataManager.getInstance().getData(player).getKt();
        metaBook.setDisplayName(ChatColor.GOLD + "当前总调整系数：" + (Kl*Kt) );
        loreBook.add(ChatColor.DARK_GREEN + "平均存活时长：" + BigDecimal.valueOf(DeathDataManager.getInstance().getData(player).getAverageLivingTime()/1200).setScale(2, RoundingMode.HALF_UP) +"分钟。");
        loreBook.add(ChatColor.GREEN + "调整系数：" + Kl);
        loreBook.add(ChatColor.DARK_GREEN + "上次存活时长：" + BigDecimal.valueOf(DeathDataManager.getInstance().getData(player).getLastLivingTime()/1200).setScale(2, RoundingMode.HALF_UP) +"分钟。");
        loreBook.add(ChatColor.GREEN + "调整系数：" + Kt);
        metaBook.setLore(loreBook);
        itemBook.setItemMeta(metaBook);
        displayInventory.setItem(47, itemBook);

        // 填充用于广播的火箭
        ItemStack itemFirework = new ItemStack(Material.FIREWORK_ROCKET);
        ItemMeta metaFirework = itemFirework.getItemMeta();
        assert metaFirework != null;
        metaFirework.setDisplayName(ChatColor.GOLD + "广播坐标");
        List<String> loreFirework = new ArrayList<>();
        loreFirework.add(ChatColor.GRAY + "点击以更新并广播当前掉落物的坐标！");
        metaFirework.setLore(loreFirework);
        itemFirework.setItemMeta(metaFirework);
        displayInventory.setItem(48, itemFirework);

        // 填充显示掉落物坐标的指南针
        displayInventory.setItem(49, dropLocationShow(player));

        // 填充显示掉落物警告的下界之星
        displayInventory.setItem(50, dropEventShow(player));
        // 填充显示可用经验的一叠经验瓶
        displayInventory.setItem(51, expShow(DeathDataManager.getInstance().getData(player).getMoney()));
        // 填充用于退出GUI的屏障
        ItemStack itemBarrier = new ItemStack(Material.BARRIER);
        ItemMeta metaBarrier = itemBarrier.getItemMeta();
        assert metaBarrier != null;
        metaBarrier.setDisplayName("退出");
        List<String> loreBarrier = new ArrayList<>();
        loreBarrier.add(ChatColor.GRAY + "点击以退出界面。");
        metaBarrier.setLore(loreBarrier);
        itemBarrier.setItemMeta(metaBarrier);
        displayInventory.setItem(53, itemBarrier);
        //endregion

        // 延迟1tick后显示
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.openInventory(displayInventory);
        }, 1L);
    }

    //method-title                                                                                                      |====经验瓶子====>
    public ItemStack expShow(int exp){

        //region-title                                                                                                  |==计算等级==>
        //region Calculate Level
        int level;

        if (exp <= 352)
            level = (int)Math.floor(((-6+Math.sqrt(36+(4*exp)))/2));
        else if (exp <= 6382)
            level = (int)Math.floor(((40.5+Math.sqrt(40.5*40.5+(4*(exp-360))))/5));
        else
            level = (int)Math.floor(((162.5+Math.sqrt(162.5*162.5+(4*(exp-2220))))/9));
        //endregion

        //region-title                                                                                                  |==创建瓶子==>
        //region Create Item Stack Shows Level and Experience
        ItemStack expShowcase = new ItemStack(Material.EXPERIENCE_BOTTLE, Math.max(Math.min(level, 64),1));
        ItemMeta meta = expShowcase.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN + "经验：" + exp);
        assert meta != null;
        meta.setDisplayName(ChatColor.DARK_GREEN + "等级：" + level);
        meta.setLore(lore);
        expShowcase.addUnsafeEnchantment(Enchantment.MENDING, 1);
        expShowcase.setItemMeta(meta);
        //endregion

        return expShowcase;
    }

    //method-title                                                                                                      |====坐标指南====>
    public ItemStack dropLocationShow(Player player){

        // 接收玩家信息
        Location dropLocation = DeathDataManager.getInstance().getData(player).getDropLocation();
        Location deathLocation = DeathDataManager.getInstance().getData(player).getDeathLocation();

        List<String> lore = new ArrayList<>();
        ItemStack locationShowcase = new ItemStack(Material.COMPASS);
        ItemMeta meta = locationShowcase.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "位置信息：");

        //region-title                                                                                                  |==掉落坐标==>
        //region Add Drop Location Info
        lore.add(ChatColor.GRAY + "掉落物坐标：" + ChatColor.YELLOW + "(" +
                BigDecimal.valueOf(dropLocation.getX()).setScale(2, RoundingMode.HALF_UP) + ", " +
                BigDecimal.valueOf(dropLocation.getY()).setScale(2, RoundingMode.HALF_UP) + ", " +
                BigDecimal.valueOf(dropLocation.getZ()).setScale(2, RoundingMode.HALF_UP) + ")");

        switch (dropLocation.getWorld().getName()){
            case "world":
                lore.add(ChatColor.GRAY + "掉落物位于：" + ChatColor.DARK_GREEN + "主世界");
                break;
            case "world_nether":
                lore.add(ChatColor.GRAY + "掉落物位于：" + ChatColor.DARK_RED + "下界");
                break;
            case "world_the_end":
                lore.add(ChatColor.GRAY + "掉落物位于：" + ChatColor.DARK_PURPLE + "末地");
                break;
        }

        //endregion

        //region-title                                                                                                  |==死亡坐标==>
        //region Add Death Location Info
        lore.add(ChatColor.GRAY + "死亡坐标：" + ChatColor.YELLOW + "(" +
                BigDecimal.valueOf(deathLocation.getX()).setScale(2, RoundingMode.HALF_UP) + ", " +
                BigDecimal.valueOf(deathLocation.getY()).setScale(2, RoundingMode.HALF_UP) + ", " +
                BigDecimal.valueOf(deathLocation.getZ()).setScale(2, RoundingMode.HALF_UP) + ")");

        switch (deathLocation.getWorld().getName()){
            case "world":
                lore.add(ChatColor.GRAY + "死亡于：" + ChatColor.DARK_GREEN + "主世界");
                break;
            case "world_nether":
                lore.add(ChatColor.GRAY + "死亡于：" + ChatColor.DARK_RED + "下界");
                break;
            case "world_the_end":
                lore.add(ChatColor.GRAY + "死亡于：" + ChatColor.DARK_PURPLE + "末地");
                break;
        }
        //endregion

        meta.setLore(lore);
        locationShowcase.setItemMeta(meta);
        return locationShowcase;
    }

    //method-title                                                                                                      |====苦痛之星====>
    public ItemStack dropEventShow(Player player){

        ItemStack dropEventShowcase = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = dropEventShowcase.getItemMeta();
        List<String> lore = new ArrayList<>();
        meta.setDisplayName(ChatColor.DARK_RED + "WARNING!!!");

        //region-title                                                                                                  |==警示标语==>
        //region Add Warning Sign
        if (DeathDataManager.getInstance().getData(player).getDropOnFire())
            lore.add(ChatColor.DARK_RED + "您的掉落物已炎上：所有可被烧毁的未赎回物品将消失！");
        if (DeathDataManager.getInstance().getData(player).getDropInLava())
            lore.add(ChatColor.DARK_RED + "您的掉落物正在泡岩浆：所有可被烧毁的未赎回物品将消失！");
        if (DeathDataManager.getInstance().getData(player).getDropDiedFromExplosion())
            lore.add(ChatColor.DARK_RED + "您的掉落物已被炸毁：所有未赎回物品将消失！");
        if (DeathDataManager.getInstance().getData(player).getDropDiedFromCactus())
            lore.add(ChatColor.DARK_RED + "您的掉落物已被部分刺毁：未赎回物品将可能消失！");
        if (DeathDataManager.getInstance().getData(player).getDropDiedFromVoid())
            lore.add(ChatColor.DARK_RED + "您的掉落物已遁入虚空：所有未赎回物品将消失！");
        if (lore.isEmpty()) {
            meta.setDisplayName(ChatColor.GREEN + "您的掉落物暂时安全。跑尸快乐！");
        }
        //endregion

        meta.setLore(lore);
        dropEventShowcase.setItemMeta(meta);
        return dropEventShowcase;
    }

    //method-title                                                                                                      |====特价告示====>
    public ItemStack discountShow(Player player){
        ItemStack itemSign = new ItemStack(Material.BAMBOO_SIGN);
        ItemMeta metaSign = itemSign.getItemMeta();
        List<String> loreSign = new ArrayList<>();
        assert metaSign != null;

        metaSign.setDisplayName(ChatColor.DARK_GREEN + "特价显示");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(0))
            loreSign.add(ChatColor.GREEN + "水桶特价可用！");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(1))
            loreSign.add(ChatColor.GREEN + "战逃反应特价可用！");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(2))
            loreSign.add(ChatColor.GREEN + "垫脚方块特价可用！");
        if (loreSign.isEmpty())
            loreSign.add(ChatColor.DARK_RED + "无可用特价！");

        metaSign.setLore(loreSign);
        itemSign.setItemMeta(metaSign);
        return itemSign;
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
            Bukkit.getScheduler().runTaskLater(this, () -> {
                            guiInitialize(event.getPlayer());
                        }, 20L);
        // 开始追踪
        trackPlayer(event.getPlayer());
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
                        deepCloneInventory(allInventoryContents),
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
        Item marker = player.getLocation().getWorld().dropItem(player.getLocation(), markerStack);
        // 给标记掉落物上一个 MARKER_OWNER 键，内容为玩家的名字
        marker.getPersistentDataContainer().set(MARKER_OWNER, PersistentDataType.STRING, player.getName());
        marker.setUnlimitedLifetime(true);
        marker.setVisibleByDefault(false);
        trackMarker(marker, player);
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
                50,
                false,
                false
        ));
        //endregion

        //region-title                                                                                                  |==孟婆例汤==>
                                                                                                                        //region Clear Inventory and Experience

        // 延迟5tick清除玩家的背包与经验
        Bukkit.getScheduler().runTaskLater(this, () -> {
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
        Bukkit.getScheduler().runTaskLater(this, () -> {
            player.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
        }, 10L);

        // 启动GUI
        guiInitialize(player);
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
            Bukkit.getScheduler().runTaskLater(this, () -> {
                guiInitialize((Player) event.getPlayer());
            }, 2L);
        }
    }

    //method-title                                                                                                      |====点击物品：点击监听====>
    @EventHandler//                                                                                                     Deal if Any Items Clicked
    public void onInventoryClick(InventoryClickEvent event) {
        // 若是本插件创建的GUI被点击
        if (!DeathDataManager.getInstance().getData((Player) event.getWhoClicked()).getRespawnDone()) {
            Player player = (Player) event.getWhoClicked();
            // 点击的物品叫谷子（
            ItemStack goods = event.getCurrentItem();

            //if-title                                                                                                  |===火箭：广播并更新坐标===>
            if (event.getSlot() == 48) {
                event.getInventory().setItem(49, dropLocationShow(player));

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
                                player.getLastDeathLocation().getWorld().dropItem(deathData.getDropLocation(),
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
                                    player.getLastDeathLocation().getWorld().dropItem(deathData.getDropLocation(),
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
                            player.getLastDeathLocation().getWorld().dropItem(deathData.getDropLocation(),
                                    deathData.getInventory()[i]);
                    }
                }
                //endregion

                deathData.doRespawnDone();
                // 2tick后关闭GUI
                Bukkit.getScheduler().runTaskLater(this, player::closeInventory, 5L);
            }
            //if-title                                                                                                  |===玩家物品：预测价格===>
            if (event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getSlot() < 45 && !(goods != null && goods.getType().equals(Material.AIR))){
                event.setCancelled(true);
                assert goods != null;
                event.getView().setTitle(guiTitle(getPrice(goods, player), player, true));
            }

            //if-title                                                                                                  |===玩家物品+shift：赎回物品===>
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getSlot() < 45 && !(goods != null && goods.getType().equals(Material.AIR))) {

                DeathData deathData = DeathDataManager.getInstance().getData(player);

                event.setCancelled(true);
                assert goods != null;
                PlayerInventory realInventory = (PlayerInventory) player.getOpenInventory().getBottomInventory();
                if (getPrice(goods, player) <= DeathDataManager.getInstance().getData(player).getMoney()) {
                    int slot = event.getSlot();
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);

                    //region-title                                                                                      |===赎回物品===>
                    //region Buy Items Back

                    // 缴费
                    deathData.spendMoney(getPrice(goods, player));

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
                    deathData.buyInventoryOnDeath(event.getSlot());
                    // 扣除GUI中的物品
                    event.getView().getTopInventory().setItem(event.getSlot(), new ItemStack(Material.AIR));
                    // 若使用特价
                    if (getPrice(goods, player) == 0){
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
                            case DIRT:
                            case STONE:
                            case COBBLESTONE:
                            case NETHERRACK:
                                if (deathData.getIsDiscountAvailable(2))
                                    deathData.spendDiscount(2);
                                break;
                        }
                        event.getView().close();
                    }
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        event.getView().setTitle(guiTitle(deathData.getMoney(), player, false));
                    }, 5L);
                    //endregion
                }
                else {
                    event.setCancelled(true);
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1.0f, 1.0f);
                }
                // 更新ui！
                event.getView().getTopInventory().setItem(51, expShow(deathData.getMoney()));
                event.getView().getTopInventory().setItem(46, discountShow(player));
            }
        }
    }

    //method-title                                                                                                      |====提交时长：玩家追踪====>
    //.                                                                                                                 Submit TIME_SINCE_DEATH Per 60ticks Loop
    public void trackPlayer(Player player){
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()){
                    DeathDataManager.getInstance().getData(player).submitLastLivingTime((long) player.getStatistic(Statistic.TIME_SINCE_DEATH));
                }
                else{
                    cancel();
                }
            }
        };
        task.runTaskTimer(this, 0, 60);
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
                        player.getOpenInventory().getTopInventory().setItem(49, dropLocationShow(player));
                        player.getOpenInventory().getTopInventory().setItem(50, dropEventShow(player));
                    }
                    // 跟踪苦痛更新

                }
            }
        };
        task.runTaskTimer(this, 0, 20);
    }


}
