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

import static org.innopolis.wotabot.config.Constants.Commands.*;
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

    private enum MessageType {
        REGISTRATION, NEW_POINT, POLL, STATISTICS
    }

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody Update update) throws IOException {
        log.info(update.getMessage().toString());

        Message receivedMessage = update.getMessage();
        Chat currentChat = update.getMessage().getChat();

        switch (receivedMessage.getText()) {
            case START:
                registerNewRoommate(currentChat);
                break;
            case STATS:
                handleStatsRequest(update);
                break;
            case NEW_POINT:
                handleNewPointRequest(update);
                break;
            case POLL_YES:
                handlePollYesRequest(update);
                break;
            default:
                sendMessage(currentChat, "Не по масти шелестишь, петушок.");
        }
        return "home";
    }

    //TODO: add new functionality
    private void handlePollYesRequest(Update update) {

    }

    //TODO: add new functionality
    private void handleNewPointRequest(Update update) {

    }

    //TODO: add new functionality
    private void handleStatsRequest(Update update) throws IOException {
        log.info(sendMessage(update.getMessage().getChat(), generateStatisticsMessage()));
    }


//    //TODO: add new functionality
//    private String generateMessage(MessageType type, Roommate roommate) {
//        StringBuilder sb = new StringBuilder();
//        switch (type) {
//            case REGISTRATION:
//                registerNewRoommate(roommate);
//                sb.append("Hey, ").append(roommate.getRealName()).append(", you was registered.");
//                break;
//            case STATISTICS:
//                sb.append(generateStatisticsMessage());
//                break;
//            case POLL:
//                //TODO: add new functionality
//                break;
//            case NEW_POINT:
//                //TODO: add new functionality
//                break;
//        }
//        return sb.toString();
//    }


    private String generateStatisticsMessage() {
        StringBuilder sb = new StringBuilder();
        for (Roommate roommate : repository.findAll()) {
            sb.append(roommate.getRealName()).append(" : ").append(roommate.getPoints()).append("\n");
        }
        return sb.toString();
    }

    private String sendMessage(Chat chat, String message) throws IOException {
        String urlString = String.format(SEND_MESSAGE, BotConfig.BOT_TOKEN, chat.getId(), message);
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return reader.readLine();
    }

    private void registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.getUserName());
        newRoommate.setRealName(chat.getFirstName());
        repository.save(newRoommate);
    }

    private void registerNewRoommate(Roommate roommate) {
        repository.save(roommate);
    }


}
