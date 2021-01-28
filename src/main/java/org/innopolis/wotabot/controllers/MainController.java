package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.BotConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


@Controller
@Slf4j
public class MainController {
    @Autowired
    TelegramWebhookBot bot;

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody Update update) throws IOException {
        long chatId = update.getMessage().getChatId();
        Message message = new Message();
        log.info(message.toString());

        message.setChat(new Chat(chatId, "private"));
        String rawURL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
        String urlString = String.format(rawURL, BotConfig.BOT_TOKEN, chatId, message.getText());

        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        log.info(response);

        return "home";
    }


    private String beautifyJSON(String json) {
        int level = 0;
        for (int i = 0; i < json.length(); i++) {
            if (json.charAt(i) == '{') {
                StringBuilder sb = new StringBuilder();
                sb.append(json.substring(0, i + 1)).append("\n");
                level++;
                for (int j = 0; j < level; j++) {
                    sb.append("\t");
                }
                sb.append(json.substring(i + 1));
                json = sb.toString();
            } else if (json.charAt(i) == ',') {
                StringBuilder sb = new StringBuilder();
                sb.append(json.substring(0, i + 1)).append("\n");
                for (int j = 0; j < level; j++) {
                    sb.append("\t");
                }
                sb.append(json.substring(i + 1));
                json = sb.toString();
            } else if (json.charAt(i) == '}') {
                level--;
                StringBuilder sb = new StringBuilder();
                sb.append(json.substring(0, i + 1)).append("\n");
                for (int j = 0; j < level; j++) {
                    sb.append("\t");
                }
                sb.append(json.substring(i + 1));
            }
        }
        return json;
    }

}
