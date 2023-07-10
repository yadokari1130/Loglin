package com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLog;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Message;
import com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLogBot;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Parser;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Sender;
import com.sun.nio.file.ExtendedWatchEventModifier;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LogWatcher implements Runnable {

	public static String logFile = "";
	public static long before = 0;

	@Override
	public void run() {
		File file = new File(LogWatcher.logFile, "latest.log");
		while (true) {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (file.length() == before) continue;
			else if (file.length() < before) before = 0;

			try (RandomAccessFile reader = new RandomAccessFile(file, "r")) {
				reader.seek(before);
				while (true) {
					String line = RandomAccessReader.readLine(reader, Charset.defaultCharset());
					before = reader.getFilePointer();
					if (line == null) break;

					Log log = Parser.parse(line);

					if (log == null) continue;

					if (!log.getText().isEmpty()) {
						Message message;
						if (log.getPlayer() != null) message = new Message(log.getPlayer(), log.getText());
						else message = new Message(log.getText());
						Sender.sendMessageToDiscord(message);
					}

					List<String> commandOutputs = log.execCommand();
					for (String commandOutput : commandOutputs) {
						if (!commandOutput.isEmpty()) Sender.sendMessageToDiscord(commandOutput);
					}
				}

				new Thread(() -> {
					try (FileOutputStream writer = new FileOutputStream("settings.properties")) {
						MinecraftLogBot.prop.setProperty("before", before + "");
						MinecraftLogBot.prop.store(writer, null);
						writer.flush();
					}
					catch (IOException e1) {
						e1.printStackTrace();
					}
				}).start();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}