package org.yuyukon.softDeath;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DeathData {
    private Location deathLocation;                         // 死亡的坐标，重生时从 getLastDeathLocation() 获取。
    private Location dropLocation;                          // 掉落物的坐标，从Marker的跟踪方法中随时获取。
    private ItemStack[] inventory;                          // 死亡时的物品栏，获取时使用深拷贝方法。
    private int expOnDeath;                                 // 死亡时的总经验值
    private int money;                                      // 可以花费的经验值，可以直接通过 SpendMoney() 直接修改。

    private Long lastLivingTime;
    private double averageLivingTime;
    private float Kl;
    private float Kt;

    private Boolean respawnDone = true;                     // 当此标记为true时玩家已重生。

    private Boolean isDropOnFire;                   // 掉落物是否着火，eventCode 为0
    private Boolean isDropInLava;                   // 掉落物是否坠入虚空， eventCode 为1
    private Boolean isDropDiedFromExplosion;        // 掉落物是否被爆炸摧毁，eventCode 为2
    private Boolean isDropDiedFromCactus;           // 掉落物是否被仙人掌摧毁，eventCode 为3
    private Boolean isDropDiedFromVoid;             // 掉落物是否坠入虚空， eventCode 为4

    private int discountBucket;                 // 免费赎回一个桶（非奶桶）类物品的剩余可用次数，discountCode 为0
    private int discountFightOrFlight;          // 免费赎回两个铁剑或铁镐或盾牌类物品的剩余可用次数，discountCode 为1
    private int discountCheapBlock;             // 免费赎回两组廉价（垫脚）方块物品的剩余可用次数，discountCode 为2

    private boolean isTestPriceOn = false;      // 测试价格是否启用



    // 每两分钟提交上次存活时间
    public void submitLastLivingTime(Long lastLivingTime){
        this.lastLivingTime = lastLivingTime;
    }

    // 死亡时提交死亡数据
    public void submitDeathData(Location deathLocation,
                                ItemStack[] inventory,
                                int expOnDeath,
                                double averageLivingTime) {
        this.deathLocation = deathLocation;
        this.inventory = inventory;
        this.expOnDeath = expOnDeath;
        this.money = expOnDeath;
        this.averageLivingTime = averageLivingTime;
        this.respawnDone = false;
        // 重置特价
        this.discountBucket = 1;
        this.discountFightOrFlight = 2;
        this.discountCheapBlock = 1;

        if (!isTestPriceOn) {
            // 计算玩家水平系数
            if (averageLivingTime <= 7*1200*60)
                this.Kl = (float) (Math.log(averageLivingTime/(1200*60) + 17) + 1 - Math.log(24));
            else
                this.Kl = 1;

            // 计算上条命时长系数
            if (lastLivingTime <= 8400){
                this.Kt = (float) (Math.pow( lastLivingTime /13200.0, 2)+ 0.3);
            } else if (lastLivingTime <= 70700) {
                this.Kt = BigDecimal.valueOf(0.5366455211211094 * Math.exp(-Math.pow( lastLivingTime - 70700.0, 2) / 2.7632178E9) + 0.81097).setScale(5, RoundingMode.HALF_UP).floatValue();
            } else {
                this.Kt = BigDecimal.valueOf(0.2977181197025617 * Math.exp(-Math.pow( lastLivingTime - 70700.0, 2) / 8.978E9) + 1.05).setScale(5, RoundingMode.HALF_UP).floatValue();
            }
        } else {
            this.Kl = 1;
            this.Kt = 1;
        }

        // 重置掉落物苦痛
        this.isDropDiedFromVoid = false;
        this.isDropDiedFromExplosion = false;
        this.isDropDiedFromCactus = false;
        this.isDropOnFire = false;
        this.isDropInLava = false;
    }

    public void setIsTestPriceOn(boolean isTestPriceOn){
        this.isTestPriceOn = isTestPriceOn;
    }

    public void submitDropEvent(int eventCode){
        switch (eventCode){
            case 0:
                this.isDropOnFire = true;
                break;
            case 1:
                this.isDropInLava = true;
                break;
            case 2:
                this.isDropDiedFromExplosion = true;
                break;
            case 3:
                this.isDropDiedFromCactus = true;
                break;
            case 4:
                this.isDropDiedFromVoid = true;
                break;
        }
    }
    public void clearDropEvent(){
        this.isDropOnFire = false;
        this.isDropInLava = false;
        this.isDropDiedFromExplosion = false;
        this.isDropDiedFromCactus = false;
        this.isDropDiedFromVoid = false;
    }

    public void doRespawnDone(){ this.respawnDone = true; }
    public Boolean getRespawnDone(){ return this.respawnDone; }

    //region Drop Marker Event
    public Boolean getDropOnFire() { return isDropOnFire; }

    public Boolean getDropInLava() { return isDropInLava; }

    public Boolean getDropDiedFromExplosion() { return isDropDiedFromExplosion; }

    public Boolean getDropDiedFromCactus() { return isDropDiedFromCactus; }

    public Boolean getDropDiedFromVoid() { return isDropDiedFromVoid; }
    //endregion

    public ItemStack[] getInventory() { return inventory; }
    public void buyInventory(int slot) { this.inventory[slot] = null; }

    public int getExpOnDeath(){ return expOnDeath; }
    public int getMoney() { return money; }
    public void spendMoney(int price){
        this.money = this.money - price;
    }

    public Long getLastLivingTime(){ return lastLivingTime; }
    public double getAverageLivingTime() { return averageLivingTime; }
    public float getKl() { return Kl; }
    public float getKt() { return Kt; }

    public void setDropLocation(Location location) { this.dropLocation = location; }
    public Location getDeathLocation() { return deathLocation; }
    public Location getDropLocation() { return this.dropLocation; }

    public void spendDiscount(int discountCode){
        switch (discountCode){
            case 0:
                this.discountBucket = this.discountBucket - 1;
                break;
            case 1:
                this.discountFightOrFlight = this.discountFightOrFlight - 1;
                break;
            case 2:
                this.discountCheapBlock = this.discountCheapBlock - 1;
                break;

        }
    }

    public Boolean getIsDiscountAvailable(int discountCode){
        return switch (discountCode) {
            case 0 -> this.discountBucket > 0;
            case 1 -> this.discountFightOrFlight > 0;
            case 2 -> this.discountCheapBlock > 0;
            default -> false;
        };
    }
}
