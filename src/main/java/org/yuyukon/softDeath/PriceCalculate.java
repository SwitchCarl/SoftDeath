package org.yuyukon.softDeath;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public class PriceCalculate {

    private static FileConfiguration config;

    private static Double Basic;

    private static Double T_Rare;
    private static Double T_Armor;
    private static Double T_AdvancedResource;
    private static Double T_ToolAndWeapon;

    private static Double M_Diamond;
    private static Double M_Netherite;

    private static Double E_Enchanted;

    private static List<Material> RareItem;
    private static List<Material> AdvancedResourceItem;
    private static List<Material> ArmorItem;
    private static List<Material> ToolAndWeaponItem;

    private static List<Material> CheapBlockItem;


    public PriceCalculate(JavaPlugin plugin) {
        config = plugin.getConfig();
    }
    //method-title                                                                                                      |====加载配置====>
    public void loadConfig(){

        Basic = config.getDouble("Basic");

        T_Rare = config.getDouble("K-type.rare");
        T_Armor = config.getDouble("K-type.armor");
        T_AdvancedResource = config.getDouble("K-type.advanced-resource");
        T_ToolAndWeapon = config.getDouble("K-type.tool-and-weapon");

        M_Diamond = config.getDouble("K-material.diamond");
        M_Netherite = config.getDouble("K-material.netherite");

        E_Enchanted = config.getDouble("K-enchantment.enchanted");

        RareItem = DeepClone.convertStringListToMaterials(config.getStringList("rare-item"));
        AdvancedResourceItem = DeepClone.convertStringListToMaterials(config.getStringList("advanced-resource-item"));
        ArmorItem = DeepClone.convertStringListToMaterials(config.getStringList("Armor-item"));
        ToolAndWeaponItem = DeepClone.convertStringListToMaterials(config.getStringList("tool-and-weapon-item"));

        CheapBlockItem = DeepClone.convertStringListToMaterials(config.getStringList("cheap-block-item"));

    }


    //method-title                                                                                                      |====计算价格====>
    public static int getPrice(ItemStack iS, Player player) {

        double Kl, Kt, T, M, E, S;// 玩家水平系数；上次死亡时间系数；物品类型系数；物品材质系数；物品附魔系数；物品堆叠系数；

        Kl = DeathDataManager.getInstance().getData(player).getKl();
        Kt = DeathDataManager.getInstance().getData(player).getKt();
        M = 1;
        T = 1;
        E = 1;

        //region-title                                                                                                  |===堆叠系数===>
        S = 0.13 * Math.log(iS.getAmount()) + 1 ;

        //region-title                                                                                                  |===类别系数===>
                                                                                                                        //region Calculate Type Coefficient

        // 稀有资源
        if (RareItem.contains(iS.getType()))
            T = T_Rare;

        // 盔甲类
        if (ArmorItem.contains(iS.getType())){
            T = T_Armor ;
            M = 0;  // 标识需要判断材质
        }

        // 高级资源类
        if (AdvancedResourceItem.contains(iS.getType()))
            T = T_AdvancedResource;

        // 工具与武器类
        if (ToolAndWeaponItem.contains(iS.getType())){
            T = T_ToolAndWeapon ;
            M = 0;
        }
        //endregion

        //region-title                                                                                                  |===材质系数===>
                                                                                                                        //region Material Coefficient
        if (M == 0){
            if (iS.getType().name().startsWith("DIAMOND_"))
                M = M_Diamond;
            else if (iS.getType().name().startsWith("NETHERITE_"))
                M = M_Netherite;
            else
                M = 1;
        }
        //endregion

        //region-title                                                                                                  |===附魔系数===>
        if (!Objects.requireNonNull(iS.getItemMeta()).getEnchants().isEmpty()){
            E = E_Enchanted;
        }

        //region-title                                                                                                  |===启用特价===>
                                                                                                                        //region Discount
        // 特价功能特异性较强，并未完全实现文件配置；
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
        }
        if (CheapBlockItem.contains(iS.getType())
                && DeathDataManager.getInstance().getData(player).getIsDiscountAvailable(2))
            return 0;
        //endregion

        return BigDecimal.valueOf(Basic * Kl * Kt * M * E * T * S).setScale(0, RoundingMode.HALF_UP).intValue();
    }

}