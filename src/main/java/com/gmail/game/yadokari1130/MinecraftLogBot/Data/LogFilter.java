package com.gmail.game.yadokari1130.MinecraftLogBot.Data;

import com.gmail.game.yadokari1130.MinecraftLogBot.Data.Option;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogFilter {
    private final Pattern pattern;
    private final List<List<String>> commands;
    private final List<Set<Option>> options;
    private final boolean isWhite;
    private final boolean isPlayerLog;
    private final String separator;
    private final int argCount;

    public LogFilter(String regex, String separator, List<List<String>> commands, List<Set<Option>> options, int argCount, boolean isWhite, boolean isPlayerLog) {
        this.pattern = Pattern.compile(regex);
        this.separator = separator;
        this.commands = commands;
        this.options = options;
        this.isWhite = isWhite;
        this.isPlayerLog = isPlayerLog;
        this.argCount = argCount;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getSeparator() {
        return separator;
    }

    public List<List<String>> getCommands() {
        return commands;
    }

    public List<Set<Option>> getOptions() {
        return options;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isPlayerLog() {
        return isPlayerLog;
    }

    public Matcher getMatcher(String line) {
        return pattern.matcher(line);
    }

    public int getArgCount() {
        return argCount;
    }
}
