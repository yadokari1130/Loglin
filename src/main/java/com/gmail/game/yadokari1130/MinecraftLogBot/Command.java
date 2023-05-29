package com.gmail.game.yadokari1130.MinecraftLogBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {

	public static Properties prop = new Properties();
	public static Map<String, String> commands = new HashMap<>();
	public static Map<String, List<Option>> options = new HashMap<>();
	public static List<String> ignoreCommands = new ArrayList<>();

	public static void register() {
		for (Object k : prop.keySet()) {
			String name = k.toString();
			String command = prop.getProperty(name);
			Matcher matcher = Pattern.compile("(.*)_(([oefi]){1,3})$").matcher(name);
			List<Option> option = new ArrayList<>();

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

		StringBuilder sb = new StringBuilder();
		args.add(0, commands.get(command));
		ProcessBuilder pb = new ProcessBuilder(args);
		try {
			Process p = pb.start();
			p.waitFor();
			if (options.get(command).contains(Option.OUTPUT)) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
					while (true) {
						String line = reader.readLine();
						if (line == null) break;
						sb.append(line);
						sb.append("\n");
					}
				}
			}

			if (options.get(command).contains(Option.ERROR)) {
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream(), Charset.defaultCharset()))) {
					while (true) {
						String line = reader.readLine();
						if (line == null) break;
						sb.append(line);
						sb.append("\n");
					}
				}
			}
			p.destroy();
		}
		catch (IOException | InterruptedException e) {
			sb.append("エラーが発生しました");
			e.printStackTrace();
		}

		return sb.toString();
	}

	public static boolean isFree(String command) {
		return options.containsKey(command) && options.get(command).contains(Option.FREE);
	}
}

enum Option {
	OUTPUT,
	ERROR,
	FREE
}