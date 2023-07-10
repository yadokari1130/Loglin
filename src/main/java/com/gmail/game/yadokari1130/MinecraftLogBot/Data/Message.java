package com.gmail.game.yadokari1130.MinecraftLogBot.Data;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class Message {
	Map<String, Object> datas = new HashMap<>();
	private final String json;
	private MinecraftColor playerColor;
	public int count = 0;
	public static String systemName = "";
	public static String systemAvatarUrl = "";


	public Message(String name, String avatarUrl, String text) {
		datas.put("username", name);
		datas.put("avatar_url", avatarUrl);
		datas.put("content", text);
		Map<String, Object> temp = new HashMap<>();
		temp.put("parse", new String[0]);
		datas.put("allowed_mentions", temp);

		json = new Gson().toJson(datas);
	}

	public Message(Player player, String text) {
		this(player.hasNickName() ? player.getNickName() : player.getName(), player.getAvatarUrl(), text);
	}

	public Message(String text) {
		this(systemName, systemAvatarUrl, text);
	}

	public String getJson() {
		return json;
	}

	public String getName() {
		return datas.get("username").toString();
	}

	public String getText() {
		return datas.get("content").toString();
	}

	public MinecraftColor getPlayerColor() {
		return playerColor;
	}

	public void setPlayerColor(MinecraftColor playerColor) {
		this.playerColor = playerColor;
	}
}
