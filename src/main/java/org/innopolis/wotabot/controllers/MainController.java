package org.innopolis.wotabot.controllers;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.database.NewPointRepository;
import org.innopolis.wotabot.database.RoommateRepository;
import org.innopolis.wotabot.models.NewPoint;
import org.innopolis.wotabot.models.Roommate;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static org.innopolis.wotabot.MessageManager.sendBroadcastMessage;
import static org.innopolis.wotabot.MessageManager.sendMessage;
import static org.innopolis.wotabot.config.Constants.Commands.*;
import static org.innopolis.wotabot.config.Constants.POLL_MESSAGE;

@Controller
@Slf4j
public class MainController {
    final TelegramBot bot;
    final RoommateRepository roommateRepository;
    final NewPointRepository newPointRepository;

    final HttpClient client;

    public MainController(TelegramBot bot, RoommateRepository roommateRepository, NewPointRepository newPointRepository) {
        this.bot = bot;
        this.roommateRepository = roommateRepository;
        this.newPointRepository = newPointRepository;
        this.client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    }

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody String textUpdate) {
        log.info(new JSONObject(textUpdate).toString(4));

        Update update = BotUtils.parseUpdate(textUpdate);
        Message receivedMessage = update.message();

        if (receivedMessage == null) {
            log.info("Received message does not have body. ");
            return homePage();
        }

        Chat currentChat = receivedMessage.chat();
        String receivedMessageText = receivedMessage.text();

        if (update.callbackQuery() != null) {
            log.info("Callback query was received.");
            CallbackQuery callbackQuery = update.callbackQuery();
            AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id());
            answer.text("Handled");

            if (update.callbackQuery().data().equals("1")) {
                handlePollYesRequest(update);
            } else {
                handlePollNoRequest(update);
            }

            if (bot.execute(answer).isOk()) {
                log.info("Button was handled.");
            } else {
                log.info("Button was NOT handled!");

            }
            return homePage();
        }

        switch (receivedMessageText) {
            case START:
                handleStartRequest(update);
                break;
            case STATS:
                handleStatsRequest(update);
                break;
            case NEW_POINT:
                handleNewPointRequest(update);
                break;
            case WATER_IS_EMPTY:
                handleWaterIsEmptyRequest();
                break;
            case TRASH_IS_FULL:
                handleTrashIsFullRequest();
                break;
            default: {
                sendMessage(currentChat, "Не по масти шелестишь, петушок.");
            }

        }
        return homePage();
    }

    public boolean isPollAnswer(String messageText) {
        String[] parts = POLL_MESSAGE.split(" %s ");
        return messageText.startsWith(parts[0]) && messageText.endsWith(parts[1]);
    }


    public void handleStartRequest(Update update) {
        long chatId = update.message().chat().id();
        boolean saved = false;
        if (!roommateRepository.existsById(chatId)) {
            saved = registerNewRoommate(update.message().chat());
        }
        Optional<Roommate> optRoommate = roommateRepository.findById(chatId);
        if (optRoommate.isPresent()) {
            Roommate roommate = optRoommate.get();
            sendMessage(roommate.getChatId(), "Ас-саламу алейкум, братик");
        }
        if (saved) {
            log.info("New roommate was registered: " + roommateRepository.findById(chatId).get());
        }
    }

    public void handleStatsRequest(Update update) {
        sendMessage(update.message().chat(), generateStatisticsMessage());
    }

    public void handleNewPointRequest(Update update) {
        Chat currentChat = update.message().chat();
        Optional<Roommate> potentialRoommate = roommateRepository.findById(currentChat.id());
        if (potentialRoommate.isEmpty()) {
            log.error("The user does not belong to any room." + currentChat.username());
            sendMessage(currentChat, "You do not belong to any room.");
        } else {
            Roommate currentRoommate = potentialRoommate.get();
            List<Roommate> otherRoommates = getListOfRoommates().stream().filter(x -> !x.equals(currentRoommate)).collect(Collectors.toList());
            saveNewPoint(currentChat);
            String pollMessageText = generatePollMessage(currentRoommate);
            for (Roommate roommate : otherRoommates) {
                SendMessage message = new SendMessage(roommate.getChatId(), pollMessageText);
                InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton("yes").callbackData("1"), new InlineKeyboardButton("no").callbackData("2"));
                message.replyMarkup(replyKeyboardMarkup);
                sendMessage(roommate.getChatId(), message);
            }
        }
    }

    public void handlePollYesRequest(Update update) {
        log.info(update.message().from().username() + " voted for yes. ");
        List<NewPoint> newPoints = getAllPoints();
        //noinspection OptionalGetWithoutIsPresent
        Roommate sentRoommate = roommateRepository.findById(update.message().chat().id()).get();
        newPoints.forEach(x -> log.info(x.toString()));
        newPoints = newPoints.stream().filter(x -> !x.getRoommate().equals(sentRoommate)).collect(Collectors.toList());
        newPoints.forEach(x -> log.info(x.toString()));


        if (newPoints.isEmpty()) {
            sendMessage(update.message().chat(), "There are no polls.");
        } else {
            NewPoint checkedPoint = newPoints.get(newPoints.size() - 1);
            Roommate provedRoommate = checkedPoint.getRoommate();

            newPointRepository.delete(checkedPoint);
            provedRoommate.incrementPoints();
            roommateRepository.save(provedRoommate);

            String sb = sentRoommate.getRealName() + " has approved that " +
                    provedRoommate.getRealName() + " has done his job.";

            sendBroadcastMessage(roommateRepository.findAll(), sb);
        }
    }


    public void handlePollNoRequest(Update update) {
        log.info(update.message().from().username() + " voted for no. ");
        List<NewPoint> newPoints = getAllPoints();
        if (newPoints.isEmpty()) {
            sendMessage(update.message().chat(), "There are no points requests");
        } else {
            NewPoint declinedPoint = newPoints.get(newPoints.size() - 1);
            Roommate loser = declinedPoint.getRoommate();
            //noinspection OptionalGetWithoutIsPresent
            Roommate declinedRoommate = roommateRepository.findById(update.message().chat().id()).get();
            newPointRepository.delete(declinedPoint);
            sendBroadcastMessage(roommateRepository.findAll(), declinedRoommate.getRealName() + " declined " + loser.getRealName() + "'s new point request.");
        }
    }

    public void handleWaterIsEmptyRequest() {
        sendWaterTrashMessage("bring a water!");
    }

    public void handleTrashIsFullRequest() {
        sendWaterTrashMessage("take out the trash!");
    }


    public void sendWaterTrashMessage(String particularPart) {
        List<Roommate> candidates = getListOfSlaves();
        for (Roommate candidate : candidates) {
            sendMessage(candidate.getChatId(), candidate.getRealName() + ", it is your turn to " + particularPart);
        }
    }


    public String generateStatisticsMessage() {
        StringBuilder sb = new StringBuilder();
        List<Roommate> roommates = getListOfRoommates();
        for (int i = 0, opposite = roommates.size() - 1 - i; i < opposite; i++, opposite--) {
            Roommate tmp = roommates.get(i);
            roommates.set(i, roommates.get(opposite));
            roommates.set(opposite, tmp);
        }
        for (Roommate roommate : roommates) {
            sb.append(roommate.getRealName()).append(" : ").append(roommate.getPoints())
                    .append("(").append(getListOfRequestedPoints(roommate).size()).append(" requests").append(")")
                    .append("\n");
        }
        return sb.toString();
    }

    private List<NewPoint> getListOfRequestedPoints(Roommate roommate) {
        return getAllPoints().stream().filter(x -> x.getRoommate().equals(roommate)).collect(Collectors.toList());
    }

    /**
     * @return list of roommates with the lowest number of points
     */
    public List<Roommate> getListOfSlaves() {
        List<Roommate> roommates = getListOfRoommates();
        int lowest = roommates.get(0).getPoints();
        List<Roommate> result = new ArrayList<>();
        for (int i = 0; i < roommates.size() && roommates.get(i).getPoints() <= lowest; i++) {
            result.add(roommates.get(i));
        }
        return result;
    }

    /**
     * @return list of roommates sorted in ascending order by points
     */
    public List<Roommate> getListOfRoommates() {
        List<Roommate> roommates = new ArrayList<>();
        roommateRepository.findAll().forEach(roommates::add);
        roommates.sort(Comparator.comparingInt(Roommate::getPoints));
        return roommates;
    }

    public String generatePollMessage(Roommate broughtWater) {
        return String.format(POLL_MESSAGE, broughtWater.getRealName());
    }

    public boolean registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.username());
        newRoommate.setRealName(chat.firstName());
        newRoommate.setChatId(chat.id());
        newRoommate.setNewPointList(new ArrayList<>());

        log.info("Attempt to add a new roommate to the room: " + newRoommate.toString());
        try {
            roommateRepository.save(newRoommate);
            log.info("New roommate was successfully registered. " + newRoommate.toString());
            return true;
        } catch (IllegalArgumentException e) { //in case of saving was unsuccessful
            log.info("There is a problem with adding a new roommate to the room: " + newRoommate.toString());
            log.info(e.getMessage());
            return false;
        }
    }

    public void saveNewPoint(Chat chat) {
        NewPoint newPoint = new NewPoint();
        Optional<Roommate> optRoommate = roommateRepository.findById(chat.id());
        if (optRoommate.isEmpty()) {
            log.error("There is no such a roommate. Cannot save a new point request");
        } else {
            Roommate sentRoommate = optRoommate.get();
            newPoint.setRoommate(sentRoommate);
            sentRoommate.getNewPointList().add(newPoint);
            newPoint.setCreatedAt(new Date());
            newPointRepository.save(newPoint);
            roommateRepository.save(sentRoommate);
        }
    }

    public List<NewPoint> getAllPoints() {
        List<NewPoint> newPoints = new ArrayList<>();
        newPointRepository.findAll().forEach(newPoints::add);
        newPoints.sort((a, b) -> {
            long x = a.getCreatedAt().getTime() - b.getCreatedAt().getTime();
            if (x == 0) {
                return 0;
            }
            return (int) (x / abs(x));
        });
        return newPoints;
    }
}
