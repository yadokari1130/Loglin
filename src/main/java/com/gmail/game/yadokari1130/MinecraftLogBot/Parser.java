package com.gmail.game.yadokari1130.MinecraftLogBot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

	public static Queue<String> logs = new ArrayDeque<>();
	private static boolean isSkip = false;

	public static void parse() {
		while (!logs.isEmpty()) {
			String log = logs.poll();
			if (log.contains("[Server Shutdown Thread/INFO]")) isSkip = false;
			if (!log.contains("[Server thread/INFO]")) continue;
			if (log.contains("logged in with entity id")) continue;
			if (log.contains("lost connection:")) continue;
			if (log.contains("Starting remote control listener")) continue;
			if (log.contains("Disconnect")) continue;
			if (log.contains("For help, type \"help\" or \"?\"")) {
				isSkip = false;
				log += "\n```Server Started!```";
				System.out.println(log);
			}
			if (isSkip) continue;
			if (log.contains("Starting minecraft server")) isSkip = true;
			if (log.contains("Stopping server")) isSkip = true;

			Matcher m1 = Pattern.compile(".*\\[Server thread/INFO]: ([\\s\\S]*)").matcher(log);
			if (m1.find()) {
				String text = m1.group(1);
				Matcher m2 = Pattern.compile("<(.*)> (.*)").matcher(text);
				Matcher m3 = Pattern.compile("(.*) joined the game").matcher(text);
				Message message;
				Player player = null;
				if (m2.find()) {
					player = new Player(m2.group(1));
					message = new Message(player, m2.group(2));
				}
				else if (!MinecraftLogBot.sqlitePath.isEmpty() && m3.find()) {
					try {
						player = new Player(m3.group(1));

						try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + MinecraftLogBot.sqlitePath)) {
							conn.setAutoCommit(false);

							try (Statement statement = conn.createStatement()) {
								ResultSet rs = statement.executeQuery("SELECT * FROM login_data WHERE uuid = \"" + player.getUuid() + "\" and login_date = date(\"now\", \"localtime\");");
								if (!rs.next()) {
									final String playerName = player.getName();
									new Thread(() -> {
										try {
											Thread.sleep(3000);
										}
										catch (InterruptedException e) {
											e.printStackTrace();
										}
										Sender.sendMessage(new Message(DiscordBot.commandChar + "login_info " + playerName));
									}).start();
								}
								statement.execute("INSERT OR IGNORE INTO login_data VALUES(\"" + player.getUuid() + "\", date(\"now\", \"localtime\"));");
								conn.commit();
							}
							catch (Exception e) {
								conn.rollback();
								throw e;
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					catch (IllegalArgumentException e) {
						System.out.println(m3.group(1) + "さんは存在しません");
					}

					message = new Message(text);
				}
				else message = new Message(text);

				if (message.getText().startsWith(DiscordBot.commandChar + "setid")) {
					if (DiscordBot.prop.containsValue(player.getUuid())) {
						String key = "";
						for (Object id : DiscordBot.prop.keySet()) {
							if (DiscordBot.prop.getProperty(id.toString()).equals(player.getUuid())) {
								key = id.toString();
								break;
							}
						}

						DiscordBot.prop.remove(key);
					}
					DiscordBot.prop.setProperty(message.getText().split(" ")[1], player.getUuid());

					new Thread(() -> {
						try(FileOutputStream writer = new FileOutputStream("ids.properties")) {
							DiscordBot.prop.store(writer, null);
							writer.flush();
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
					}).start();

					message = new Message(player.getName() + "さんのアカウントとDiscordのアカウントを紐付けました");
					MinecraftRcon.sendMessage(message, player.getName(), 0);

					return;
				}

				Sender.messages.add(message);
			}
		}

		Sender.send();
	}
}
