package com.gmail.game.yadokari1130.MinecraftLogBot;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

public class Player {

	public static Properties avatarProp = new Properties();
	public static Properties nameProp = new Properties();

	private final String name;
	private final String uuid;
	private final String avatarUrl;
	private final String nickName;

	public Player(String name) {
		this.name = name;
		this.uuid = getUuid(name);
		if (avatarProp.containsKey(uuid)) avatarUrl = avatarProp.getProperty(uuid);
		else avatarUrl = "";
		if (nameProp.containsKey(uuid)) nickName = nameProp.getProperty(uuid);
		else nickName = "";
	}

	public String getName() {
		return name;
	}

	public String getUuid() {
		return uuid;
	}

	public String getAvatarUrl() {
		return avatarUrl;
	}

	public String getNickName() {
		return nickName;
	}

	public boolean hasNickName() {
		return !this.nickName.isEmpty();
	}

	public static String getUuid(String name) {
		String data = null;
		try {
			data = IOUtils.toString(new URL("https://api.mojang.com/users/profiles/minecraft/" + name), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		Map map = gson.fromJson(data, Map.class);

		if (map == null || !map.containsKey("id")) throw new IllegalArgumentException();

		return map.get("id").toString();
	}

	public static String getName(String uuid) {
		String data = null;
		try {
			data = IOUtils.toString(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		Gson gson = new Gson();
		Map map = gson.fromJson(data, Map.class);

		if (map == null || !map.containsKey("name")) throw new IllegalArgumentException();

		return map.get("name").toString();
	}
}
