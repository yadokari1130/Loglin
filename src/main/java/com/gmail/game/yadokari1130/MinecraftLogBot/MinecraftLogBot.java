package com.gmail.game.yadokari1130.MinecraftLogBot;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

public class MinecraftLogBot {
	public static Properties prop = new Properties();
	static String token = "";
	public static String version = "b1.0";
	public static String sqlitePath = "";

	public static void main(String[] args) {
		System.out.println("Minecraft Log Bot ver " + version);

		if (loadFiles()) System.out.println("設定をロードしました");
		else {
			System.out.println("設定のロードに失敗しました");
			System.exit(0);
		}

		if (!(new File(LogReader.logFile + "/latest.log").exists())) {
			System.out.println("ログファイル(latest.log)が見つかりません");
			System.out.println("一度サーバーを起動し、設定(settings.properties)を見直してください");
			System.exit(0);
		}

		if (!token.isEmpty()) DiscordBot.login(token);
		try {
			MinecraftRcon.build();
		}
		catch (Exception e) {
			System.out.println("rconに関する設定が不正です");
			e.printStackTrace();
		}

		new Thread(new LogReader()).start();

		new Thread(() -> {
			Scanner scan = new Scanner(System.in);
			while (true) {
				String line = scan.nextLine();
				int num = line.indexOf(" ");
				num = num > 0 ? num : line.length();
				String command = line.substring(0, num);
				line = line.substring(Math.min(num + 1, line.length()));
				Message message = new Message(line);

				if (command.equals("mc")) MinecraftRcon.sendCommand(line);
				else if (command.equals("dc")) Sender.sendMessage(message);
				else if (command.equals("reload")) {
					if (loadFiles()) System.out.println("設定をリロードしました");
					else System.out.println("設定のリロードに失敗しました");
				}
			}
		}).start();
	}

	public static boolean loadFiles() {
		List<FileType> types = new ArrayList<>();
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("settings.properties"), StandardCharsets.UTF_8))) {
			prop.load(reader);
			MinecraftRcon.hostName = prop.getProperty("hostName");
			MinecraftRcon.port = Integer.parseInt(prop.getProperty("port"));
			MinecraftRcon.password = prop.getProperty("password");
			Sender.url = prop.getProperty("url");
			token = prop.getProperty("token");
			LogReader.logFile = prop.getProperty("logFile");
			LogReader.before = Long.parseLong(prop.getProperty("before"));
			DiscordBot.settingChannel = prop.getProperty("settingChannel");
			DiscordBot.textChannel = prop.getProperty("textChannel");
			DiscordBot.commandChannel = prop.getProperty("commandChannel");
			DiscordBot.settingRoleId = prop.getProperty("settingRoleId");
			DiscordBot.textRoleId = prop.getProperty("textRoleId");
			DiscordBot.commandRoleId = prop.getProperty("commandRoleId");
			DiscordBot.commandChar = prop.getProperty("commandChar");
			Message.systemName = prop.getProperty("systemName");
			Message.systemAvatarUrl = prop.getProperty("systemAvatarUrl");
			MinecraftLogBot.sqlitePath = prop.getProperty("sqlitePath");
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

		return true;
	}

	public static void initFiles(List<FileType> types) {
		if (types.contains(FileType.SETTING)) {
			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("settings.properties"), StandardCharsets.UTF_8))) {
				prop.setProperty("hostName", "localhost");
				prop.setProperty("port", "25575");
				prop.setProperty("password", "");
				prop.setProperty("url", "");
				prop.setProperty("token", "");
				prop.setProperty("logFile", "");
				prop.setProperty("before", "0");
				prop.setProperty("settingChannel", "");
				prop.setProperty("textChannel", "");
				prop.setProperty("commandChannel", "");
				prop.setProperty("settingRoleId", "");
				prop.setProperty("textRoleId", "");
				prop.setProperty("commandRoleId", "");
				prop.setProperty("commandChar", "!");
				prop.setProperty("systemName", "System");
				prop.setProperty("systemAvatarUrl", "");
				prop.setProperty("sqlitePath", "");
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
}

enum FileType {
	SETTING,
	AVATAR,
	COMMAND,
	ID,
	NAME,
}
