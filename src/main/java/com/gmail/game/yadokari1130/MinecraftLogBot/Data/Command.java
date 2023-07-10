package com.gmail.game.yadokari1130.MinecraftLogBot.Data;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Option;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Executor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {

	public static Properties prop = new Properties();
	private static final Map<String, String> commands = new HashMap<>();
	private static final Map<String, Set<Option>> options = new HashMap<>();
	private static final Set<String> ignoreCommands = new HashSet<>();

	public static void register() {
		for (Object k : prop.keySet()) {
			String name = k.toString();
			String command = prop.getProperty(name);
			Matcher matcher = Pattern.compile("(.*)_([oefi]{1,3})$").matcher(name);
			Set<Option> option = new HashSet<>();

			if (matcher.find()) {
				name = matcher.group(1);
				String opChars = matcher.group(2);

				if (opChars.contains("i")) {
					ignoreCommands.add(name);
					continue;
				}
				if (opChars.contains("o")) option.add(Option.OUTPUT);
				if (opChars.contains("e")) option.add(Option.ERROR);
				if (opChars.contains("f")) option.add(Option.FREE);
			}

			commands.put(name, command);
			options.put(name, option);
		}
	}

	public static String exec(String command, List<String> args) {
		if (ignoreCommands.contains(command)) return "";
		if (!commands.containsKey(command)) return "コマンドが見つかりませんでした";

		args.add(0, commands.get(command));

		return Executor.exec(args, options.get(command));
	}

	public static boolean isFree(String command) {
		return options.containsKey(command) && options.get(command).contains(Option.FREE);
	}
}