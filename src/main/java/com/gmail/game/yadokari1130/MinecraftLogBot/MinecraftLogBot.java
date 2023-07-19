package com.gmail.game.yadokari1130.MinecraftLogBot;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Command;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.LogFilterJson;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Message;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Player;
import com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLog.LogWatcher;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Parser;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Sender;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class MinecraftLogBot {
	public static Properties prop = new Properties();
	static String token = "";
	public static String version = "1.0";
	public static String dbPath = "";
	public static final String osName = System.getProperty("os.name").toLowerCase();

	public static void main(String[] args) {
		System.out.println("Minecraft Log Bot ver " + version);

		checkUpdate();

		if (loadFiles()) System.out.println("設定をロードしました");
		else {
			System.out.println("設定のロードに失敗しました");
			System.exit(0);
		}

		if (!new File(LogWatcher.logFile, "latest.log").exists()) {
			System.out.println(LogWatcher.logFile + "内にlatest.logが見つかりません");
			System.out.println("一度サーバーを起動し、設定(settings.properties)を見直してください");
			System.exit(0);
		}
		if (MinecraftLogBot.dbPath != null && !MinecraftLogBot.dbPath.isEmpty()) {
			try {
				new File(MinecraftLogBot.dbPath).createNewFile();
			}
			catch (IOException e) {
				e.printStackTrace();
			}

			try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + MinecraftLogBot.dbPath))  {
				conn.setAutoCommit(false);

				try (Statement statement = conn.createStatement()) {
					statement.execute("CREATE TABLE IF NOT EXISTS login_data(UUID TEXT, login_date DATE, PRIMARY KEY(UUID, login_date));");
					conn.commit();
				}
				catch (Exception e) {
					conn.rollback();
					e.printStackTrace();
				}
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}

		if (!token.isEmpty()) DiscordBot.login(token);
		try {
			MinecraftRcon.build();
		}
		catch (Exception e) {
			System.out.println("rconに関する設定が不正です");
			e.printStackTrace();
		}

		new Thread(new LogWatcher()).start();

		new Thread(() -> {
			Scanner scan = new Scanner(System.in);
			while (scan.hasNext()) {
				String line = scan.nextLine();
				int num = line.indexOf(" ");
				num = num > 0 ? num : line.length();
				String command = line.substring(0, num);
				line = line.substring(Math.min(num + 1, line.length()));
				Message message = new Message(line);

				if (command.equals("mc")) MinecraftRcon.sendCommand(line);
				else if (command.equals("dc")) Sender.sendMessageToDiscord(message);
			}
		}).start();
	}

	public static boolean loadFiles() {
		List<FileType> types = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("settings.properties"), StandardCharsets.UTF_8))) {
			prop.load(reader);
			MinecraftRcon.hostName = prop.getProperty("hostIp");
			MinecraftRcon.port = Integer.parseInt(prop.getProperty("port"));
			MinecraftRcon.password = prop.getProperty("password");
			Sender.discordWebhookUrl = prop.getProperty("url");
			token = prop.getProperty("token");
			LogWatcher.logFile = prop.getProperty("logFile");
			LogWatcher.before = Long.parseLong(prop.getProperty("before"));
			DiscordBot.settingChannelId = prop.getProperty("settingChannelId");
			DiscordBot.textChannelId = prop.getProperty("textChannelId");
			DiscordBot.commandChannelId = prop.getProperty("commandChannelId");
			DiscordBot.settingRoleId = prop.getProperty("settingRoleId");
			DiscordBot.textRoleId = prop.getProperty("textRoleId");
			DiscordBot.commandRoleId = prop.getProperty("commandRoleId");
			DiscordBot.commandChar = prop.getProperty("commandChar");
			DiscordBot.isChangeUserColor = Boolean.parseBoolean(prop.getProperty("isChangeUserColor"));
			Message.systemName = prop.getProperty("systemName");
			Message.systemAvatarUrl = prop.getProperty("systemAvatarUrl");
			MinecraftLogBot.dbPath = prop.getProperty("dbPath");
		}
		catch (FileNotFoundException e) {
			types.add(FileType.SETTING);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("avatarUrls.properties"), StandardCharsets.UTF_8))) {
			Player.avatarProp.load(reader);
		}
		catch (FileNotFoundException e) {
			types.add(FileType.AVATAR);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("commands.properties"), StandardCharsets.UTF_8))) {
			Command.prop.load(reader);
			Command.register();
		}
		catch (FileNotFoundException e) {
			types.add(FileType.COMMAND);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("ids.properties"), StandardCharsets.UTF_8))) {
			DiscordBot.prop.load(reader);
		}
		catch (FileNotFoundException e) {
			types.add(FileType.ID);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("names.properties"), StandardCharsets.UTF_8))) {
			Player.nameProp.load(reader);
		}
		catch (FileNotFoundException e) {
			types.add(FileType.NAME);
		}
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if (!types.isEmpty()) initFiles(types);

		StringBuilder sb = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("log_filter.json"), StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		catch (FileNotFoundException e) {
			try {
				new File("log_filter.json").createNewFile();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		List<LogFilterJson> jsons = new Gson().fromJson(sb.toString(), new TypeToken<List<LogFilterJson>>(){}.getType());
		Parser.register(jsons);

		return true;
	}

	public static void initFiles(List<FileType> types) {
		if (types.contains(FileType.SETTING)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("settings.properties"), StandardCharsets.UTF_8))) {
				prop.setProperty("hostIp", "localhost");
				prop.setProperty("port", "25575");
				prop.setProperty("password", "");
				prop.setProperty("url", "");
				prop.setProperty("token", "");
				prop.setProperty("logFile", "");
				prop.setProperty("before", "0");
				prop.setProperty("settingChannelId", "");
				prop.setProperty("textChannelId", "");
				prop.setProperty("commandChannelId", "");
				prop.setProperty("settingRoleId", "");
				prop.setProperty("textRoleId", "");
				prop.setProperty("commandRoleId", "");
				prop.setProperty("commandChar", "!");
				prop.setProperty("isChangeUserColor", "true");
				prop.setProperty("systemName", "Loglin");
				prop.setProperty("systemAvatarUrl", "https://github.com/yadokari1130/Loglin/blob/master/images/Loglin_icon_white.png?raw=true");
				prop.setProperty("dbPath", "login_data.db");
				prop.store(writer, null);
				writer.flush();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (types.contains(FileType.AVATAR)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("avatarUrls.properties"), StandardCharsets.UTF_8))) {
				Player.avatarProp.store(writer, null);
				writer.flush();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (types.contains(FileType.COMMAND)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("commands.properties"), StandardCharsets.UTF_8))) {
				Command.prop.store(writer, null);
				writer.flush();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (types.contains(FileType.ID)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ids.properties"), StandardCharsets.UTF_8))) {
				DiscordBot.prop.store(writer, null);
				writer.flush();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		if (types.contains(FileType.NAME)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("names.properties"), StandardCharsets.UTF_8))) {
				Player.nameProp.store(writer, null);
				writer.flush();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		System.out.println("設定を初期化しました");
		System.exit(0);
	}

	public static void checkUpdate() {
		String update = null;
		try {
			update = IOUtils.toString(new URL("https://raw.githubusercontent.com/yadokari1130/Loglin/master/changelog.json"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		Map map = gson.fromJson(update, Map.class);

		String latest = map.get("latest").toString();

		if (!latest.equals(MinecraftLogBot.version)) {
			System.out.println("アップデートがあります！");
			System.out.println(((Map<String, String>)map.get(latest)).get("content").toString());
			System.out.print("ダウンロードはこちら：");
			System.out.println(((Map<String, String>)map.get(latest)).get("url").toString());
		}
	}
}

enum FileType {
	SETTING,
	AVATAR,
	COMMAND,
	ID,
	NAME,
}
