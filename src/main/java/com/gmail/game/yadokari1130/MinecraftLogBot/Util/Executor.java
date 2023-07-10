package com.gmail.game.yadokari1130.MinecraftLogBot.Util;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Option;
import com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLogBot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

public class Executor {
    public static String exec(List<String> command, Set<Option> options) {
        StringBuilder sb = new StringBuilder();
        if (MinecraftLogBot.osName.startsWith("windows")) {
            command.add(0, "/c");
            command.add(0, "cmd");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        try {
            Process p = pb.start();
            p.waitFor();
            if (options.contains(Option.OUTPUT)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), Charset.defaultCharset()))) {
                    while (true) {
                        String line = reader.readLine();
                        if (line == null) break;
                        sb.append(line);
                        sb.append("\n");
                    }
                }
            }

            if (options.contains(Option.ERROR)) {
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
}
