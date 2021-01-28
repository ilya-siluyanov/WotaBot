package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.BotConfig;
import org.innopolis.wotabot.database.UserRepository;
import org.innopolis.wotabot.models.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;

import static org.innopolis.wotabot.Constants.SEND_MESSAGE;


@Controller
@Slf4j
public class MainController {
    final TelegramWebhookBot bot;

    final UserRepository repository;

    public MainController(TelegramWebhookBot bot, UserRepository repository) {
        this.bot = bot;
        this.repository = repository;
    }

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody Update update) throws IOException {
        long chatId = update.getMessage().getChatId();
        log.info(update.getMessage().toString());
        Message receivedMessage = update.getMessage();
        String userName = receivedMessage.getChat().getUserName();
        Optional<User> optUser = repository.findById(userName);
        if(!optUser.isPresent()){
            throw new IOException("user was not found.");
        }
        User user = optUser.get();

        String urlString = String.format(SEND_MESSAGE, BotConfig.BOT_TOKEN, chatId,"Hello, "+user.getRealName());
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        log.info(response);
        return "home";
    }

}
