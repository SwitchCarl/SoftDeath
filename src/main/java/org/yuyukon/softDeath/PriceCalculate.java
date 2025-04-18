package org.yuyukon.softDeath;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class PriceCalculate {

    //method-title                                                                                                      |====计算价格====>
    public static int getPrice(ItemStack iS, Player player) {

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
        T = switch (iS.getType()) {   // 附魔金苹果
            // 试炼钥匙
            // 不详试炼钥匙
            // 下界之星
            // 鞘翅
            // 沉重核心
            // 海洋之心
            // 下界合金锭
            // 下界合金块
            // 龙蛋
            // 龙首
            // 不死图腾
            case ENCHANTED_GOLDEN_APPLE, TRIAL_KEY, OMINOUS_TRIAL_KEY, NETHER_STAR, ELYTRA, HEAVY_CORE, HEART_OF_THE_SEA, NETHERITE_INGOT, NETHERITE_BLOCK, DRAGON_EGG, DRAGON_HEAD, TOTEM_OF_UNDYING, BEACON ->                    // 信标
                    6.0;
            default -> T;
        };
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
        if (!Objects.requireNonNull(iS.getItemMeta()).getEnchants().isEmpty()){
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

}