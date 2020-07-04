package com.jaoafa.jaoSuperAchievement2.Command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.jaoafa.jaoSuperAchievement2.Main;
import com.jaoafa.jaoSuperAchievement2.API.AchievementAPI;
import com.jaoafa.jaoSuperAchievement2.Task.Task_OpenPage;

public class Cmd_JSA implements CommandExecutor {
	JavaPlugin plugin;

	public Cmd_JSA(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(AchievementAPI.getPrefix() + "このコマンドはプレイヤーからのみ実行できます。");
			return true;
		}
		if (args.length == 1) {
			Player player = (Player) sender;
			OfflinePlayer offplayer = Bukkit.getOfflinePlayer(args[0]);
			if (offplayer == null) {
				sender.sendMessage(AchievementAPI.getPrefix() + "指定されたプレイヤー「" + args[0] + "」は見つかりませんでした。");
				return true;
			}
			new Task_OpenPage(player, offplayer, 1).runTaskAsynchronously(Main.getJavaPlugin());
		} else {
			Player player = (Player) sender;
			new Task_OpenPage(player, player, 1).runTaskAsynchronously(Main.getJavaPlugin());
		}
		return true;
	}
}