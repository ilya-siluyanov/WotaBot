package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.config.BotConfig;
import org.innopolis.wotabot.database.NewPointRepository;
import org.innopolis.wotabot.database.RoommateRepository;
import org.innopolis.wotabot.models.NewPoint;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static java.lang.Math.abs;
import static org.innopolis.wotabot.config.Constants.Commands.*;
import static org.innopolis.wotabot.config.Constants.SEND_MESSAGE;

@Controller
@Slf4j
public class MainController {
    final TelegramWebhookBot bot;
    final RoommateRepository roommateRepository;
    final NewPointRepository newPointRepository;

    public MainController(TelegramWebhookBot bot, RoommateRepository roommateRepository, NewPointRepository newPointRepository) {
        this.bot = bot;
        this.roommateRepository = roommateRepository;
        this.newPointRepository = newPointRepository;
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
                handleStartRequest(update);
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


    private void handleStartRequest(Update update) {
        String userName = update.getMessage().getChat().getUserName();
        if (!roommateRepository.existsById(userName)) {
            registerNewRoommate(update.getMessage().getChat());
        }
        log.info("New roommate was registered: " + roommateRepository.findById(userName).get());
    }

    //TODO: add new functionality
    private void handlePollYesRequest(Update update) throws IOException {
        List<NewPoint> newPoints = new ArrayList<>();
        newPointRepository.findAll().forEach(newPoints::add);
        newPoints.sort((a, b) -> {
            long x = b.getCreatedAt().getTime() - a.getCreatedAt().getTime();
            if (x == 0) {
                return 0;
            }
            return (int) (x / abs(x));
        });
        Roommate sentRoommate = roommateRepository.findById(update.getMessage().getChat().getUserName()).get();
        NewPoint checkedPoint = newPoints.get(0);
        Roommate provedRoommate = checkedPoint.getRoommate();
        newPointRepository.delete(checkedPoint);
        String sb = sentRoommate.getRealName() + " has approved that " +
                provedRoommate.getRealName() + "has done his job.";
        sendBroadcastMessage(roommateRepository.findAll(), sb);
    }

    private void handleNewPointRequest(Update update) throws IOException {
        Chat currentChat = update.getMessage().getChat();
        @SuppressWarnings("OptionalGetWithoutIsPresent") Roommate currentRoommate = roommateRepository.findById(currentChat.getUserName()).get();
        List<Roommate> otherRoommates = new ArrayList<>();
        roommateRepository.findAll().forEach(x -> {
            if (!x.equals(currentRoommate)) {
                otherRoommates.add(x);
            }
        });
        String generatedBroadcastMessage = generatePollMessage(currentRoommate);
        sendBroadcastMessage(otherRoommates, generatedBroadcastMessage);
    }

    private void handleStatsRequest(Update update) throws IOException {
        log.info(sendMessage(update.getMessage().getChat(), generateStatisticsMessage()));
    }

    private String generateStatisticsMessage() {
        StringBuilder sb = new StringBuilder();
        for (Roommate roommate : roommateRepository.findAll()) {
            sb.append(roommate.getRealName()).append(" : ").append(roommate.getPoints()).append("\n");
        }
        return sb.toString();
    }

    private String generatePollMessage(Roommate broughtWater) {
        return "Your roommate" + " " + broughtWater.getRealName() +
                " " + "brought water" +
                " " + "or took out trash." +
                " " + "Is it true?";
    }

    private String sendMessage(Chat chat, String message) throws IOException {
        return sendMessage(chat.getId(), message);
    }

    private void sendBroadcastMessage(Iterable<Roommate> roommates, String message) throws IOException {
        for (Roommate roommate : roommates) {
            sendMessage(roommate.getChatId(), message);
        }
    }

    private String sendMessage(long chatId, String message) throws IOException {
        if (message.isEmpty()) {
            message = "Почему-то пустое сообщение";
        }
        log.info("Sent message : " + message);
        String urlString = String.format(SEND_MESSAGE, BotConfig.BOT_TOKEN, chatId, URLEncoder.encode(message, StandardCharsets.UTF_8.toString()));
        log.info("Send response with URL (encoded): " + urlString);
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        return reader.readLine();
    }

    private void registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.getUserName());
        newRoommate.setRealName(chat.getFirstName());
        newRoommate.setChatId(chat.getId());
        newRoommate.setNewPointList(new Stack<>());
        roommateRepository.save(newRoommate);
    }
}
