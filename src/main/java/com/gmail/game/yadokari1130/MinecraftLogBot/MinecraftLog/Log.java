package com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLog;

import com.gmail.game.yadokari1130.MinecraftLogBot.Util.Executor;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Option;
import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Player;
import com.gmail.game.yadokari1130.MinecraftLogBot.Util.LoginUtil;

import java.util.*;

public class Log {
    private final String text;
    private final List<List<String>> commands;
    private final List<Set<Option>> options;
    private final List<String> args;
    private final Player player;

    public Log(String text, List<List<String>> commands, List<Set<Option>> options, List<String> args, Player player) {
        this.text = text;
        this.commands = commands;
        this.options = options;
        this.args = args;
        this.player = player;
    }

    public String getText() {
        return text;
    }

    public List<String> execCommand() {
        List<String> results = new ArrayList<>();
        for (int i = 0; i < commands.size(); i++) {
            List<String> command = commands.get(i);

            if (command.isEmpty()) {
                results.add("");
                continue;
            }
            if (command.get(0).equals("LoginUtil.insertLoginRecord")) {
                LoginUtil.insertLoginRecord(new Player(this.args.get(0)));
                results.add("");
                continue;
            }
            if (command.get(0).equals("LoginUtil.sendLoginInfoCommand")) {
                LoginUtil.sendLoginInfoCommand(new Player(this.args.get(0)));
                results.add("");
                continue;
            }
            if (command.get(0).equals("LoginUtil.setId")) {
                LoginUtil.setId(this.player, this.args.get(0));
                results.add("");
                continue;
            }

            for (int k = 0; k < command.size(); k++) {
                String c = command.get(k);
                if (c.equals("$playerName")) command.set(k, this.player.getName());
                if (c.equals("$playerUuid")) command.set(k, this.player.getUuid());
            }

            results.add(Executor.exec(command, options.get(i)));
        }

        return results;
    }

    public Player getPlayer() {
        return player;
    }
}
