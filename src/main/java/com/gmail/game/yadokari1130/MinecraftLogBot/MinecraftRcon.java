package com.gmail.game.yadokari1130.MinecraftLogBot;

import com.google.gson.Gson;
import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MinecraftRcon {
	private static Rcon rcon = null;
	public static boolean isConnected = false;
	public static String hostName = "localhost";
	public static int port = 25575;
	public static String password = "";

	public static void build() {
		try {
			rcon = new Rcon(hostName, port, password.getBytes());
		}
		catch (AuthenticationException e) {
			e.printStackTrace();
			System.out.println("Rconへのログインに失敗しました");
			System.out.println("パスワードが間違っている可能性があります");
			return;
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Rconへの接続に失敗しました");
			return;
		}

		isConnected = true;
		System.out.println("Rconに接続しました");
	}

	public static void sendMessage(Message message, String selector, int count) {
		if (count > 5) {
			System.out.println("Rconの設定が間違っている可能性があります。");
			return;
		}

		Map<String, String> map = new HashMap<>();
		map.put("text", String.format("<%s> %s", message.getName(), message.getText()));
		try {
			rcon.command("/tellraw " + selector + " "  + new Gson().toJson(map));
		}
		catch (IOException | NullPointerException | NegativeArraySizeException e) {
			build();
			sendMessage(message, selector, ++count);
		}
	}

	public static void sendMessage(Message message) {
		sendMessage(message, "@a", 0);
	}

	public static String sendCommand(String command, int count) {
		if (count > 5) {
			System.out.println("Rconの設定が間違っている可能性があります。");
			return "";
		}

		try {
			return rcon.command(command);
		}
		catch (IOException | NullPointerException | NegativeArraySizeException e) {
			build();
			return sendCommand(command, ++count);
		}
	}

	public static String sendCommand(String command) {
		return sendCommand(command, 0);
	}
}
