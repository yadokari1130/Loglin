package com.gmail.game.yadokari1130.MinecraftLogBot;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class LogReader implements Runnable {

	static String logFile = "";
	static long before = 0;

	@Override
	public void run() {
		WatchService watcher;
		try {
			watcher = FileSystems.getDefault().newWatchService();

			Watchable path = Paths.get(logFile);
			path.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}

		while (true) {
			WatchKey watchKey = null;
			try {
				watchKey = watcher.take();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}

			for (WatchEvent<?> event : watchKey.pollEvents()) {
				if (!event.context().toString().equals("latest.log")) continue;

				try (RandomAccessFile reader = new RandomAccessFile(logFile + "/latest.log", "r")) {
					if (reader.length() < before) before = 0;
					reader.seek(before);

					new Thread(() -> {
						try(FileOutputStream writer = new FileOutputStream("settings.properties")) {
							MinecraftLogBot.prop.setProperty("before", before + "");
							MinecraftLogBot.prop.store(writer, null);
							writer.flush();
						}
						catch (IOException e1) {
							e1.printStackTrace();
						}
					}).start();

					while (true) {
						String line = reader.readLine();
						if (line == null) break;
						String log = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
						Parser.logs.add(log);
					}

					before = reader.length();

					Parser.parse();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (!watchKey.reset()) {
				System.out.println("WatchKeyが無効になりました");
				System.out.println("プログラムを再起動してください");
				return;
			}
		}
	}
}
