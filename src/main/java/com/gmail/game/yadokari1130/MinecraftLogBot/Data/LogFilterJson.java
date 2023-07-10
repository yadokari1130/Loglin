package com.gmail.game.yadokari1130.MinecraftLogBot.Data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogFilterJson {
    private String regex;
    private String separator = " ";
    private List<String> commands = new ArrayList<>();
    private int argCount = 0;
    private boolean isWhite = true;
    private boolean isPlayerLog = false;
    private List<Boolean> isReturnOutputs = new ArrayList<>();
    private List<Boolean> isReturnErrors = new ArrayList<>();
}
