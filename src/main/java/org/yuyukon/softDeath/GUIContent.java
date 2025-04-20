package org.yuyukon.softDeath;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GUIContent {

    //method-title                                                                                                      |====界面显示====>
    public void guiInitialize(Player player) {

        Inventory displayInventory = Bukkit.createInventory(null, 54, guiTitle(DeathDataManager.getInstance().getData(player).getMoney(), player, false));
        ItemStack[] allInventoryContentDisplay = DeepClone.deepCloneInventory(DeathDataManager.getInstance().getData(player).getInventory());

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
                            Objects.requireNonNull(meta.getLore()).add(ChatColor.GREEN + "价格：" + PriceCalculate.getPrice(allInventoryContentDisplay[i], player));
                            meta.getLore().add(ChatColor.GREEN + "[" + guiTitle(PriceCalculate.getPrice(allInventoryContentDisplay[i], player), player, false) + ChatColor.GREEN + "]");
                        }
                        else {
                            List<String> lore = new ArrayList<>();
                            lore.add(ChatColor.GREEN + "价格：" + PriceCalculate.getPrice(allInventoryContentDisplay[i], player) );
                            lore.add(ChatColor.GREEN + "[" + guiTitle(PriceCalculate.getPrice(allInventoryContentDisplay[i], player), player, false) + ChatColor.GREEN + "]");
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
        metaFirework.setDisplayName(ChatColor.GOLD + "广播坐标！");
        List<String> loreFirework = new ArrayList<>();
        loreFirework.add(ChatColor.GRAY + "点击以更新并广播当前掉落物的坐标！");
        metaFirework.setLore(null);
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
        metaBarrier.setDisplayName(ChatColor.RED + "退出！");
        List<String> loreBarrier = new ArrayList<>();
        loreBarrier.add(ChatColor.GRAY + "点击以退出界面。");
        metaBarrier.setLore(loreBarrier);
        itemBarrier.setItemMeta(metaBarrier);
        displayInventory.setItem(53, itemBarrier);
        //endregion

        player.openInventory(displayInventory);
    }

    //method-title                                                                                                      |====经验瓶子====>
    public static ItemStack expShow(int exp){

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
        lore.add(ChatColor.DARK_GREEN + "等级：" + level);
        assert meta != null;
        meta.setDisplayName(ChatColor.GOLD + "经验：" + exp);
        meta.setLore(lore);
        expShowcase.addUnsafeEnchantment(Enchantment.MENDING, 1);
        expShowcase.setItemMeta(meta);
        //endregion

        return expShowcase;
    }

    //method-title                                                                                                      |====坐标指南====>
    public static ItemStack dropLocationShow(Player player){

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

        switch (Objects.requireNonNull(dropLocation.getWorld()).getName()){
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

        switch (Objects.requireNonNull(deathLocation.getWorld()).getName()){
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
    public static ItemStack dropEventShow(Player player){

        ItemStack dropEventShowcase = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = dropEventShowcase.getItemMeta();
        List<String> lore = new ArrayList<>();
        assert meta != null;
        meta.setDisplayName(ChatColor.DARK_RED + "警告！");

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
            meta.setDisplayName(ChatColor.GOLD + "您的掉落物暂时安全。");
            lore.add(ChatColor.DARK_GREEN + "跑尸快乐！");
        }
        //endregion

        meta.setLore(lore);
        dropEventShowcase.setItemMeta(meta);
        return dropEventShowcase;
    }

    //method-title                                                                                                      |====特价告示====>
    public static ItemStack discountShow(Player player){
        ItemStack itemSign = new ItemStack(Material.BAMBOO_SIGN);
        ItemMeta metaSign = itemSign.getItemMeta();
        List<String> loreSign = new ArrayList<>();
        assert metaSign != null;

        metaSign.setDisplayName(ChatColor.GOLD + "可用特价：");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(0))
            loreSign.add(ChatColor.DARK_GREEN + "水桶特价可用！");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(1))
            loreSign.add(ChatColor.DARK_GREEN + "战逃反应特价可用！");
        if (DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(2))
            loreSign.add(ChatColor.DARK_GREEN + "垫脚方块特价可用！");
        if (loreSign.isEmpty())
            loreSign.add(ChatColor.DARK_RED + "无可用特价！");

        metaSign.setLore(loreSign);
        itemSign.setItemMeta(metaSign);
        return itemSign;
    }

    //method-title                                                                                                      |====可视价格====>
    public static String guiTitle(int money, Player player, boolean isUsedInForecast) {

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
            }
        }else
            title = new StringBuilder("*");
        return title.toString();
    }
}