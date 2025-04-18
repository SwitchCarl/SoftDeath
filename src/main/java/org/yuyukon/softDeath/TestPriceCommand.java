package org.yuyukon.softDeath;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestPriceCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, @org.jetbrains.annotations.NotNull String[] args) {
        Player target = Bukkit.getPlayer(args[1]);

        if (args.length != 2) {
            return false;
        }
        if (!args[0].equalsIgnoreCase("true") && !args[0].equalsIgnoreCase("false")) {
            return false;
        }
        if (target == null) {
            return false;
        }
        boolean value = Boolean.parseBoolean(args[0]);
        DeathDataManager.getInstance().getData(target).setIsTestPriceOn(value);

        // 执行你的逻辑（这里只是示例）
        sender.sendMessage(ChatColor.GREEN + "已为玩家 " + target.getName() +
                " 设置测试价格为 " + value);

        // 返回 true 表示指令执行成功
        return true;
    }
}
