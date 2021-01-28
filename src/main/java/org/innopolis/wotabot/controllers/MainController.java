package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.lang.invoke.MethodHandleProxies;


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
    public String post(@RequestBody Update update) {
        log.info(update.toString());
        long chatId = update.getMessage().getChatId();
        Message message = new Message();
        message.setChat(new Chat(chatId,"private"));
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
