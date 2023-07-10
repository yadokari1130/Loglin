package com.gmail.game.yadokari1130.MinecraftLogBot.Util;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Message;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Player;
import com.gmail.game.yadokari1130.MinecraftLogBot.DiscordBot;
import com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLogBot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;

public class LoginUtil {
    public static void insertLoginRecord(Player player) {
        if (MinecraftLogBot.dbPath == null || MinecraftLogBot.dbPath.isEmpty()) return;

        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + MinecraftLogBot.dbPath))  {
            conn.setAutoCommit(false);

            try (Statement statement = conn.createStatement()) {
                statement.execute("INSERT OR IGNORE INTO login_data VALUES(\"" + player.getUuid() + "\", date(\"now\", \"localtime\"));");
                conn.commit();
            }
            catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendLoginInfoCommand(Player player) {
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + MinecraftLogBot.dbPath))  {
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
                        Sender.sendMessageToDiscord(DiscordBot.commandChar + "login_info " + playerName);
                    }).start();
                };
            }
            catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setId(Player player, String discordId) {
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
        DiscordBot.prop.setProperty(discordId, player.getUuid());

        new Thread(() -> {
            try(FileOutputStream writer = new FileOutputStream("ids.properties")) {
                DiscordBot.prop.store(writer, null);
                writer.flush();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        }).start();

        Message message = new Message(player.getName() + "さんのアカウントとDiscordのアカウントを紐付けました");
        Sender.sendMessageToMinecraft(message, player.getName());
    }
}
