package com.gmail.game.yadokari1130.MinecraftLogBot;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Queue;

public class Sender {
	
	public static Queue<Message> messages = new ArrayDeque<>();
	public static String url = "";
	
	public static void send() {
		while (!messages.isEmpty()) {
			Message message = messages.poll();
			
			try{
				URL sendUrl = new URL(url);    
				HttpsURLConnection con = (HttpsURLConnection)sendUrl.openConnection();

				con.addRequestProperty("Content-Type", "application/JSON; charset=utf-8");
				con.addRequestProperty("User-Agent", "DiscordBot");
				con.setDoOutput(true);
				con.setRequestMethod("POST");
				
				con.setRequestProperty("Content-Length", String.valueOf(message.getJson().length()));
				OutputStream stream = con.getOutputStream();
				stream.write(message.getJson().getBytes(StandardCharsets.UTF_8));
				stream.flush();
				stream.close();

				final int status = con.getResponseCode();
				if (status != HttpURLConnection.HTTP_OK && status != HttpURLConnection.HTTP_NO_CONTENT) {
					System.out.println("error:" + status);
					if (message.count++ < 4) messages.add(message);
				}

				con.disconnect();

			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	
	public static void sendMessage(Message message) {
		messages.add(message);
		send();
	}
}
