package com.gmail.game.yadokari1130.MinecraftLogBot;

import net.kronos.rkon.core.Rcon;
import net.kronos.rkon.core.ex.AuthenticationException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

	public static String sendCommand(String command) {
		String result = null;

		try {
			result = rcon.command(command);
		}
		catch (IOException | NullPointerException | NegativeArraySizeException e) {
			build();
		}

		return result;
	}
}
