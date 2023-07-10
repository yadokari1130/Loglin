package com.gmail.game.yadokari1130.MinecraftLogBot.MinecraftLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RandomAccessReader {

    public static String readLine(RandomAccessFile reader, Charset charset) throws IOException {
        long pos = reader.getFilePointer();
        String str = reader.readLine();
        String result = str;
        long headPos = reader.getFilePointer();

        if (str != null) {
            byte[] buffer = new byte[2];
            reader.seek(headPos - 2);
            reader.read(buffer);

            long rt = 0;
            if (buffer[1] == 0x0A) {
                if (buffer[0] == 0x0D) rt = 2;
                else rt = 1;
            }

            if (headPos > pos) {
                reader.seek(pos);
                buffer = new byte[(int)(headPos - rt - pos)];
                int r = reader.read(buffer);
                if (r != -1) result = new String(buffer, charset);
            }

            reader.seek(headPos);
        }

        return result;
    }
}
