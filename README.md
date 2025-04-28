# SoftDeath - 柔性死亡插件  
**Minecraft Version 1.21.4**  
**Spigot API 1.21**  


> <p align = "center"><em>“...and death was a temporary inconvenience.”</em></p>
> <p align = "right">—— Julian Gough, <em>End Poem</em></p>

## 简介
- 为游戏新手（甚至是电脑新手）友好服务器设计的柔性死亡惩罚系统。尽量保留生存挑战性、缓解游戏寿命缩短的同时降低死亡带来的挫败感。
- 玩家可使用死亡时的经验值赎回部分物品。基于物品价值表、玩家平均生存时长等多角度计算赎回物品的成本（下称"价格"）。
- 因为开发本插件的目的主要在于自用，绝大多数功能并未实现客制化。可能会在后续版本中优化，请见谅。
- 开发者是完全的新手。代码有不规范、待完善的地方请多多指正，谢谢各位大佬！
- 代码中有人工智能编写成分，请注意辨别。
- 同样因为开发本插件的目的主要在于自用，GUI内容以全中文显示，请见谅。

## 使用说明

### 安装步骤
1. 构建 jar 文件。
2. 建议直接新建 `plugins/SoftDeath/config.yml` 文件，然后以 Releases 页面中的 `src\main\resources\config.yml` 文件覆盖，再按需修改内容。（所有项都未设置默认值；因此请不要擅自删除任何未使用（更改）的项）。
3. 在 `Server.properties` 中启用 `KeepInventory`。
4. 启动服务器。

### 配置文件格式
见 `src\main\resources\config.yml` 及其中的注释。建议在增添物品时也加入物品的中文名注释。

### 命令
```/testprice <true|false> <Player>```  
为玩家启用（或禁用）测试价格。启用时，玩家的价格调整系数恒为1（不调整）。

## 功能简介

### 部分保护策略
- 当玩家上次存活时间少于七分钟时，判定玩家死于跑尸，从而大幅降低价格。
- 玩家物品栏中的部分物品可以免费（注释称"特价"）赎回几组，如铁剑和便宜的垫脚方块，鼓励玩家通过跑尸来拿回剩下的物品（跑尸行为更接近原版生存）。

### GUI内容
- **GUI标题栏**：以百分比形式通过`|`符号显示玩家剩余可用的经验。当玩家点击物品（不通过按住shift移动物品）时，会以绿色的`|`预测玩家将要使用的经验值。
- **GUI底部**：
   - i. 竹制告示牌：显示特价可用内容。
   - ii. 书：显示玩家目前的具体价格调整系数。
   - iii. 火箭：在聊天栏广播掉落物的坐标。
   - iv. 指南针：玩家死亡的坐标与掉落物的坐标。
   - v. 下界之星：显示警告。玩家的掉落物是否（有可能）被损毁（注释称"苦痛"）。
   - vi. 经验瓶：显示确切的剩余经验。
   - vii. 屏障：退出GUI，返还未使用的经验的80%，并将未被赎回的掉落物重现在掉落物坐标。

### GUI展示
- 当玩家重生时，显示GUI：  
![When Respawn.png](images%2FWhen%20Respawn.png)
- 通过移动光标查看钻石锄的价格：  
![Show Price.png](images%2FShow%20Price.png)
- 特价的铁剑：  
![Discount Item.png](images%2FDiscount%20Item.png)
- 当启用测试价格时，玩家系数被强制设置为1：  
![Test Price On.png](images%2FTest%20Price%20On.png)
- 点击一个物品，预测它将要花费的价格所占总经验百分比：  
![Predict Price.png](images%2FPredict%20Price.png)  
- 当买下一个物品时：  
![Buy Item.png](images%2FBuy%20Item.png)

## 注意事项
- 本插件未添加任何其他插件的适配性内容。换言之，任何插件的同时启用都可能导致本插件崩溃（尤其是会启动自定义GUI的插件）。请谨慎在多插件服务器中使用。
- `config.yml` 中的所有项都未设置默认值，因此请不要擅自删除任何未使用（更改）的项。

## 作者
**Yuyukon** <carlswitch1099@gmail.com>  

## 测试人员
**_NOz** <3326832181@qq.com>
