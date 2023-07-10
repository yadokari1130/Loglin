package com.gmail.game.yadokari1130.MinecraftLogBot.Util;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Message;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.MinecraftColor;
import com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftRcon;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Sender {
	
//	public static Queue<Message> queue = new ArrayDeque<>();
	public static String discordWebhookUrl = "";

	public static void sendMessageToMinecraft(Message message, String selector) {
		if (message.count > 5) {
			System.out.println("メッセージの送信に失敗しました。\nRconの設定が間違っている可能性があります。");
			return;
		}

		Map<String, String> map = new HashMap<>();
		String code = (message.getPlayerColor() == null || message.getPlayerColor() == MinecraftColor.BLACK) ? MinecraftColor.WHITE.getCode() : message.getPlayerColor().getCode();
		map.put("text", String.format("<%s%s%s> %s", code, message.getName(), MinecraftColor.getResetCode(), message.getText()));

		if (MinecraftRcon.sendCommand("/tellraw " + selector + " "  + new Gson().toJson(map)) == null) {
			message.count++;
			sendMessageToMinecraft(message, selector);
		}
	}

	public static void sendMessageToMinecraft(Message message) {
		sendMessageToMinecraft(message, "@a");
	}

	public static String sendCommandToMinecraft(String command, int count) {
		if (count > 5) {
			System.out.println("Rconの設定が間違っているか、重大なエラーが発生した可能性があります。");
			return "";
		}

		String result = MinecraftRcon.sendCommand(command);
		if (result == null) result = sendCommandToMinecraft(command, ++count);

		return result;
	}

	public static String sendCommandToMinecraft(String command) {
		return sendCommandToMinecraft(command, 0);
	}

	public static void sendMessageToDiscord(Message message) {
		if (message.count > 5) {
			System.out.println("メッセージの送信に失敗しました。\n");
			return;
		}

		try {
			URL sendUrl = new URL(discordWebhookUrl);
			HttpsURLConnection con = (HttpsURLConnection) sendUrl.openConnection();

			con.addRequestProperty("Content-Type", "application/JSON; charset=utf-8");
			con.addRequestProperty("User-Agent", "DiscordBot");
			con.setDoOutput(true);
			con.setRequestMethod("POST");

			con.setRequestProperty("Content-Length", String.valueOf(message.getJson().length()));
			OutputStream stream = con.getOutputStream();
			stream.write(message.getJson().getBytes(StandardCharsets.UTF_8));
			stream.flush();
			stream.close();

			final int status = con.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_NO_CONTENT) {
				System.out.println("error:" + status);
				message.count++;
				sendMessageToDiscord(message);
			}

			con.disconnect();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void sendMessageToDiscord(String message) {
		Message m = new Message(message);
		sendMessageToDiscord(m);
	}
}
