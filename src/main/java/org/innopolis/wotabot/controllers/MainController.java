package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.config.BotConfig;
import org.innopolis.wotabot.database.UserRepository;
import org.innopolis.wotabot.models.Roommate;
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
import java.util.Optional;

import static org.innopolis.wotabot.config.Constants.ADD_ME;
import static org.innopolis.wotabot.config.Constants.SEND_MESSAGE;


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
        Chat currChat = update.getMessage().getChat();
        String userName = currChat.getUserName();
        if (receivedMessage.getText().equals(ADD_ME)) {
            registerNewRoommate(currChat);
        }

        Optional<Roommate> optUser = repository.findById(userName);
        if (!optUser.isPresent()) {
            log.error("Roommate was not found");
            registerNewRoommate(currChat);
            optUser = repository.findById(userName);
        }
        Roommate roommate = optUser.get();
        String urlString = String.format(SEND_MESSAGE, BotConfig.BOT_TOKEN, chatId, "Hello, " + roommate.getRealName());
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String response = reader.readLine();
        log.info(response);
        return "home";
    }

    private void registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.getUserName());
        newRoommate.setRealName(chat.getFirstName());
        repository.save(newRoommate);
    }

}
