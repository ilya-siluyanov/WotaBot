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
import java.util.*;
import java.util.stream.Stream;

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
        Message receivedMessage = update.getMessage();
        Chat currentChat = update.getMessage().getChat();
        log.info(currentChat.getUserName() + " : " + receivedMessage.getText());
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
            case POLL_NO:
                handlePollNoRequest(update);
                break;
            case WATER_IS_EMPTY:
                handleWaterIsEmptyRequest(update);
                break;
            case TRASH_IS_FULL:
                handleTrashIsFullRequest(update);
                break;
            default:
                sendMessage(currentChat, "Не по масти шелестишь, петушок.");
        }
        return homePage();
    }

    private void handlePollNoRequest(Update update) throws IOException {
        PriorityQueue<NewPoint> newPoints = getAllPoints();
        if (newPoints.isEmpty()) {
            sendMessage(update.getMessage().getChat(), "There are no points requests");
        } else {
            NewPoint declinedPoint = newPoints.peek();
            Roommate loser = declinedPoint.getRoommate();
            Roommate declinedRoommate = roommateRepository.findById(update.getMessage().getChatId()).get();
            newPointRepository.delete(declinedPoint);
            sendBroadcastMessage(roommateRepository.findAll(),declinedRoommate.getRealName()+" declined "+loser.getRealName()+" new point request." );
        }
    }


    private void handleStartRequest(Update update) {
        long chatId = update.getMessage().getChat().getId();
        boolean saved = false;
        if (!roommateRepository.existsById(chatId)) {
            saved = registerNewRoommate(update.getMessage().getChat());
        }
        if (saved) {
            //noinspection OptionalGetWithoutIsPresent
            log.info("New roommate was registered: " + roommateRepository.findById(chatId).get());
        }
    }

    private void handleStatsRequest(Update update) throws IOException {
        sendMessage(update.getMessage().getChat(), generateStatisticsMessage());
    }

    private void handleNewPointRequest(Update update) throws IOException {
        Chat currentChat = update.getMessage().getChat();
        Optional<Roommate> potentialRoommate = roommateRepository.findById(currentChat.getId());
        if (!potentialRoommate.isPresent()) {
            log.error("The user does not belong to any room." + currentChat.getUserName());
            sendMessage(currentChat, "You do not belong to any room.");
        } else {
            Roommate currentRoommate = potentialRoommate.get();
            List<Roommate> otherRoommates = new ArrayList<>();
            roommateRepository.findAll().forEach(x -> {
                if (!x.equals(currentRoommate)) {
                    otherRoommates.add(x);
                }
            });
            saveNewPoint(currentChat);
            sendBroadcastMessage(otherRoommates, generatePollMessage(currentRoommate));
        }
    }

    private void handlePollYesRequest(Update update) throws IOException {
        PriorityQueue<NewPoint> newPoints = getAllPoints();
        Roommate sentRoommate = roommateRepository.findById(update.getMessage().getChat().getId()).get();
        Stream<NewPoint> temp = newPoints.stream().filter(x -> !x.getRoommate().equals(sentRoommate));
        newPoints.clear();
        temp.forEach(newPoints::offer);

        if (newPoints.isEmpty()) {
            sendMessage(update.getMessage().getChat(), "There are no polls.");
        } else {
            NewPoint checkedPoint = newPoints.peek();
            Roommate provedRoommate = checkedPoint.getRoommate();
            if (!provedRoommate.equals(sentRoommate))
                newPointRepository.delete(checkedPoint);
            provedRoommate.incrementPoints();
            roommateRepository.save(provedRoommate);
            String sb = sentRoommate.getRealName() + " has approved that " +
                    provedRoommate.getRealName() + " has done his job.";
            sendBroadcastMessage(roommateRepository.findAll(), sb);
        }
    }

    private void handleWaterIsEmptyRequest(Update update) throws IOException {
        sendWaterTrashMessage("bring a water!");
    }

    private void handleTrashIsFullRequest(Update update) throws IOException {
        sendWaterTrashMessage("take out the trash!");
    }

    private void sendWaterTrashMessage(String particularPart) {
        List<Roommate> candidates = getListOfWorkers();
        for (Roommate candidate : candidates) {
            sendMessage(candidate.getChatId(), candidate.getRealName() + ", it is your turn to " + particularPart);
        }
    }

    /**
     * @return list of roommates with the lowest number of points
     */
    private List<Roommate> getListOfWorkers() {
        List<Roommate> roommates = getListOfRoommates();
        int lowest = roommates.get(roommates.size() - 1).getPoints();
        List<Roommate> result = new ArrayList<>();
        for (int i = roommates.size() - 1; i >= 0; i--) {
            if (roommates.get(i).getPoints() == lowest) {
                result.add(roommates.get(i));
            }
        }
        return result;
    }

    private String generateStatisticsMessage() {
        StringBuilder sb = new StringBuilder();
        List<Roommate> roommates = getListOfRoommates();
        for (Roommate roommate : roommates) {
            sb.append(roommate.getRealName()).append(" : ").append(roommate.getPoints()).append("\n");
        }
        return sb.toString();
    }

    /**
     * @return list of roommates sorted in descending order by points
     */
    private List<Roommate> getListOfRoommates() {
        List<Roommate> roommates = new ArrayList<>();
        roommateRepository.findAll().forEach(roommates::add);
        roommates.sort((a, b) -> b.getPoints() - a.getPoints());
        return roommates;
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

    private String sendMessage(long chatId, String message) {
        if (message.isEmpty()) {
            message = "Почему-то пустое сообщение";
        }
        log.info("Attempt to send a message : " + message);
        String urlString = "";
        String response;
        try {
            urlString = String.format(SEND_MESSAGE, BotConfig.BOT_TOKEN, chatId, URLEncoder.encode(message, StandardCharsets.UTF_8.toString()));
            log.info("Attempt to send response with URL (encoded): " + urlString);
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            response = reader.readLine();
            log.info("Message successfully was sent: " + response);
        } catch (IOException e) {
            log.error("Cannot send a message: " + urlString);
            log.error(e.getMessage());
            response = "Some problems with message sending. Please, try again";
        }
        return response;
    }

    private boolean registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.getUserName());
        newRoommate.setRealName(chat.getFirstName());
        newRoommate.setChatId(chat.getId());
        newRoommate.setNewPointList(new Stack<>());

        log.info("Attempt to add a new roommate to the room: " + newRoommate.toString());
        try {
            roommateRepository.save(newRoommate);
            return true;
        } catch (IllegalArgumentException e) { //in case of saving was unsuccessful
            log.error("There is a problem with adding a new roommate to the room: " + newRoommate.toString());
            log.error(e.getMessage());
            return false;
        }
    }

    private void saveNewPoint(Chat chat) {
        NewPoint newPoint = new NewPoint();
        Optional<Roommate> optRoommate = roommateRepository.findById(chat.getId());
        if (!optRoommate.isPresent()) {
            log.error("There is no such a roommate. Cannot save a new point request");
        } else {
            Roommate sentRoommate = optRoommate.get();
            newPoint.setRoommate(sentRoommate);
            newPoint.setCreatedAt(new Date());
            newPointRepository.save(newPoint);
        }
    }

    private PriorityQueue<NewPoint> getAllPoints() {
        PriorityQueue<NewPoint> newPoints = new PriorityQueue<>((a, b) -> {
            long x = b.getCreatedAt().getTime() - a.getCreatedAt().getTime();
            if (x == 0) {
                return 0;
            }
            return (int) (x / abs(x));
        });
        newPointRepository.findAll().forEach(newPoints::add);
        return newPoints;
    }
}
