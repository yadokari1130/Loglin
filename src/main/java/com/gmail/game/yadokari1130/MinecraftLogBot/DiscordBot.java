package com.gmail.game.yadokari1130.MinecraftLogBot;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class DiscordBot {

	public static String settingChannel = "";
	public static String textChannel = "";
	public static String commandChannel = "";
	public static String settingRoleId = "";
	public static String textRoleId = "";
	public static String commandRoleId = "";
	public static String commandChar = "";
	public static Properties prop = new Properties();

	public static void login(String token) {
		DiscordClient client = DiscordClient.create(token);
		GatewayDiscordClient gateway = client.login().block();

		if (!client.getSelf().blockOptional().isPresent()) {
			System.out.println("ログインに失敗しました\nトークンなどが間違っている可能性があります");
			return;
		}

		System.out.println("次のユーザーでログインしました: " + client.getSelf().block().username());

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel().block();
			if (!settingChannel.isEmpty() && !channel.getId().asString().equals(settingChannel)) return;

			if (!message.getContent().startsWith(commandChar + "seticon") && !message.getContent().startsWith(commandChar + "setname") && !message.getContent().startsWith(commandChar + "getid")) return;
			if (message.getAuthorAsMember().block() == null || message.getAuthor().get().isBot())return;
			boolean hasPermission = false;
			for (Snowflake id : message.getAuthorAsMember().block().getRoleIds()) {
				if (!settingRoleId.isEmpty() && settingRoleId.equals(id.asString())) hasPermission = true;
			}
			if (!settingRoleId.isEmpty() && !hasPermission) {
				message.getChannel().block().createMessage("実行権限がありません").withMessageReference(message.getId()).block();
				return;
			}

			List<String> args = new ArrayList<>();
			args.addAll(Arrays.asList(message.getContent().split(" ")));
			if (args.get(0).equals(commandChar + "getid")) {
				Member authour = message.getAuthorAsMember().block();
				String id = authour.getId().asString();
				String name = authour.getNickname().orElse(authour.getDisplayName());
				name = name.replaceAll("`", "\\\\`");
				channel.createMessage(name + "さんのIDは" + id + "です\nMinecraft内で`" + commandChar +"setid " + id + "`と入力してください").withMessageReference(message.getId()).block();
				return;
			}

			if (args.get(0).equals(commandChar + "seticon")) {
				if (!prop.containsKey(message.getAuthorAsMember().block().getId().asString())) {
					channel.createMessage("アカウントがMinecraftアカウントと紐付けられていないか、入力したIDが間違っていた場合があります\n`" + commandChar + "getid`を実行してください").withMessageReference(message.getId()).block();
					return;
				}
				String url = "";
				String uuid = prop.getProperty(message.getAuthorAsMember().block().getId().asString());
				try(FileOutputStream writer = new FileOutputStream("avatarUrls.properties")) {
					if (message.getAttachments().size() > 0) url = message.getAttachments().get(0).getUrl();
					Player.avatarProp.setProperty(uuid, url);
					Player.avatarProp.store(writer, null);
					writer.flush();
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}

				channel.createMessage(Player.getName(uuid) + " さんのアイコンを設定しました\n" + (url.isEmpty() ? "アイコンなし" : url)).withMessageReference(message.getId()).block();
			}

			if (args.get(0).equals(commandChar + "setname")) {
				if (!prop.containsKey(message.getAuthorAsMember().block().getId().asString())) {
					channel.createMessage("アカウントがMinecraftアカウントと紐付けられていないか、入力したIDが間違っていた場合があります`" + commandChar + "getid`を実行してください").withMessageReference(message.getId()).block();
					return;
				}
				String name = message.getContent().length() > (8 + commandChar.length()) ? message.getContent().substring(8 + commandChar.length()) : "";
				String uuid = prop.getProperty(message.getAuthorAsMember().block().getId().asString());
				try(FileOutputStream writer = new FileOutputStream("names.properties")) {
					Player.nameProp.setProperty(uuid, name);
					Player.nameProp.store(writer, null);
					writer.flush();
				}
				catch (IOException e1) {
					e1.printStackTrace();
				}

				channel.createMessage(Player.getName(uuid) + " さんの名前を設定しました\n" + (name.isEmpty() ? Player.getName(uuid) : name)).withMessageReference(message.getId()).block();
			}
		});

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel().block();
			if (!commandChannel.isEmpty() && !channel.getId().asString().equals(commandChannel)) return;

			if (!message.getContent().startsWith(commandChar) || message.getContent().startsWith(commandChar + "seticon") || message.getContent().startsWith(commandChar + "setname") || message.getContent().startsWith(commandChar + "getid")) return;
			if (message.getAuthorAsMember().block() == null || message.getAuthor().get().isBot())return;
			boolean hasPermission = false;
			for (Snowflake id : message.getAuthorAsMember().block().getRoleIds()) {
				if (!commandRoleId.isEmpty() && commandRoleId.equals(id.asString())) hasPermission = true;
			}

			List<String> args = new ArrayList<>();
			args.addAll(Arrays.asList(message.getContent().split(" ")));
			String command = args.get(0).substring(commandChar.length());
			args.remove(0);

			if (!commandRoleId.isEmpty() && !(hasPermission || Command.isFree(command))) {
				message.getChannel().block().createMessage("実行権限がありません").withMessageReference(message.getId()).block();
				return;
			}

			String result = "";
			if (command.equals("mccommand")) {
				StringBuilder sb = new StringBuilder();
				args.forEach(a -> sb.append(a).append(" "));
				result = MinecraftRcon.sendCommand(sb.toString());
			}
			else result = Command.exec(command, args);

			if (!result.isEmpty()) channel.createMessage(result).withMessageReference(message.getId()).block();
		});

		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			Message message = event.getMessage();
			MessageChannel channel = message.getChannel().block();
			if (!textChannel.isEmpty() && !channel.getId().asString().equals(textChannel)) return;

			if (message.getContent().startsWith(commandChar)) return;
			if (message.getAuthorAsMember().block() == null || message.getAuthor().get().isBot())return;
			boolean hasPermission = false;
			for (Snowflake id : message.getAuthorAsMember().block().getRoleIds()) {
				if (!textRoleId.isEmpty() && textRoleId.equals(id.asString())) hasPermission = true;
			}
			if (!textRoleId.isEmpty() && !hasPermission) {
				return;
			}

			if (!MinecraftRcon.isConnected) return;
			com.gmail.game.yadokari1130.MinecraftLogBot.Message sendMessage = new com.gmail.game.yadokari1130.MinecraftLogBot.Message(message.getAuthorAsMember().block().getNickname().orElse(message.getAuthorAsMember().block().getDisplayName()), "", message.getContent());
			MinecraftRcon.sendMessage(sendMessage);
		});
	}
}
