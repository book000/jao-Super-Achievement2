package com.jaoafa.jaosuperachievement2.command;

import com.jaoafa.jaosuperachievement2.Main;
import com.jaoafa.jaosuperachievement2.api.AchievementAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Cmd_jaoSuperAchievement2 implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		PluginDescriptionFile desc = Main.getJavaPlugin().getDescription();
		String nowVer = desc.getVersion();
		Date nowVerDate = getVersionDate(nowVer);
		String nowVerSha = getVersionSha(nowVer);
        if(nowVerSha == null){
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現バージョンの取得に失敗しました。", NamedTextColor.AQUA)
            ));
            return true;
        }
		String latestVer = getVersion(desc.getName());
		if(latestVer == null){
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現バージョンの取得に失敗しました。", NamedTextColor.AQUA)
            ));
		    return true;
        }
		Date latestVerDate = getVersionDate(latestVer);
		String latestVerSha = getLastCommitSha(desc.getName());

        sender.sendMessage(Component.text().append(
            AchievementAPI.getPrefix(),
            Component.text("----- " + desc.getName() + " infomation -----", NamedTextColor.AQUA)
        ));

		if (nowVer.equals(latestVer)) {
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現在導入されているバージョンは最新です。", NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("導入バージョン: " + nowVer, NamedTextColor.AQUA)
            ));
		} else if (nowVerSha.equals(latestVerSha)) {
			// shaがおなじ
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現在導入されているバージョンは最新です。", NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("導入バージョン: " + nowVer, NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("最新バージョン: " + latestVer + " (" + latestVerSha + ")", NamedTextColor.AQUA)
            ));
		} else if (nowVerDate.before(latestVerDate)) {
			// 新しいバージョンあり
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現在導入されているバージョンよりも新しいバージョンがリリースされています。", NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("導入バージョン: " + nowVer, NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("最新バージョン: " + latestVer + " (" + latestVerSha + ")", NamedTextColor.AQUA)
            ));
		} else if (nowVerDate.after(latestVerDate)) {
			// リリースバージョンよりも導入されている方が新しい
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("現在導入されているバージョンは最新です。(※)", NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("導入バージョン: " + nowVer, NamedTextColor.AQUA)
            ));
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("最新バージョン: " + latestVer + " (" + latestVerSha + ")", NamedTextColor.AQUA)
            ));
		}

        sender.sendMessage(Component.text().append(
            AchievementAPI.getPrefix(),
            Component.text("最近の更新履歴", NamedTextColor.AQUA)
        ));
		List<String> commits = getCommits(desc.getName());
		if (commits == null) {
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("- コミット履歴の取得に失敗しました。", NamedTextColor.AQUA)
            ));
			return true;
		}
		for (String commit : commits) {
            sender.sendMessage(Component.text().append(
                AchievementAPI.getPrefix(),
                Component.text("- " + commit, NamedTextColor.AQUA)
            ));
		}
		return true;
	}

	private List<String> getCommits(String repo) {
		LinkedList<String> ret = new LinkedList<>();
		try {
			String url = "https://api.github.com/repos/jaoafa/" + repo + "/commits";
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).get().build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				return null;
			}
			JSONArray array = new JSONArray(response.body().string());
			response.close();

			for (int i = 0; i < array.length() && i < 5; i++) {
				JSONObject obj = array.getJSONObject(i).getJSONObject("commit");
				Date date = DateFormatUtils.ISO_8601_EXTENDED_DATETIME_TIME_ZONE_FORMAT
						.parse(obj.getJSONObject("committer").getString("date"));
				String sha = array.getJSONObject(i).getString("sha").substring(0, 7);
				ret.add("[" + sdfFormat(date) + "|" + sha + "] " + obj.getString("message"));
			}

			return ret;
		} catch (IOException | JSONException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private String getLastCommitSha(String repo) {
		try {
			String url = "https://api.github.com/repos/jaoafa/" + repo + "/commits";
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).get().build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				return null;
			}
			JSONArray array = new JSONArray(response.body().string());
			response.close();

			return array.getJSONObject(0).getString("sha").substring(0, 7);
		} catch (IOException | JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Date getVersionDate(String version) {
		String[] day_time = version.split("_");
		String[] days = day_time[0].split("\\.");
		String[] times = day_time[1].split("\\.");
		Calendar build_cal = Calendar.getInstance();
		build_cal.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));
		build_cal.set(Integer.parseInt(days[0]),
				Integer.parseInt(days[1]),
				Integer.parseInt(days[2]),
				Integer.parseInt(times[0]),
				Integer.parseInt(times[1]));
		return build_cal.getTime();
	}

	private String getVersionSha(String version) {
		String[] day_time = version.split("_");
		if (day_time.length == 3) {
			return day_time[2];
		}
		return null;
	}

	private String getVersion(String repo) {
		try {
			String url = "https://raw.githubusercontent.com/jaoafa/" + repo + "/master/src/main/resources/plugin.yml";
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder().url(url).get().build();
			Response response = client.newCall(request).execute();
			if (response.code() != 200) {
				return null;
			}
			YamlConfiguration yaml = YamlConfiguration.loadConfiguration(response.body().charStream());
			response.close();
			if (yaml.contains("version")) {
				return yaml.getString("version");
			} else {
				return null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	String sdfFormat(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(date);
	}
}
