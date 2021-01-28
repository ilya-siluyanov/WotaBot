package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.BotConfig;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


@Controller
@Slf4j
public class MainController {
    final
    TelegramWebhookBot bot;

    public MainController(TelegramWebhookBot bot) {
        this.bot = bot;
    }

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody Update update) throws IOException {
        long chatId = update.getMessage().getChatId();
        log.info(update.getMessage().toString());
        String rawURL = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";
        String urlString = String.format(rawURL, BotConfig.BOT_TOKEN, chatId, "Здарова, отец");

        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        log.info(response);

        return "home";
    }

}
