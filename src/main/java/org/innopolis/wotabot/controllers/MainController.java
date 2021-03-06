package org.innopolis.wotabot.controllers;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.database.NewPointMessageRepository;
import org.innopolis.wotabot.database.NewPointRepository;
import org.innopolis.wotabot.database.RoommateRepository;
import org.innopolis.wotabot.models.NewPoint;
import org.innopolis.wotabot.models.NewPointMessage;
import org.innopolis.wotabot.models.Roommate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.http.HttpClient;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static org.innopolis.wotabot.MessageManager.sendMessage;
import static org.innopolis.wotabot.config.Constants.Commands.*;
import static org.innopolis.wotabot.config.Constants.*;
import static org.innopolis.wotabot.config.Constants.State.*;

@Controller
@Slf4j
public class MainController {
    final TelegramBot bot;
    final RoommateRepository roommateRepository;
    final NewPointRepository newPointRepository;
    final NewPointMessageRepository newPointMessageRepository;
    final HttpClient client;

    public MainController(TelegramBot bot, RoommateRepository roommateRepository, NewPointRepository newPointRepository, NewPointMessageRepository newPointMessageRepository) {
        this.bot = bot;
        this.roommateRepository = roommateRepository;
        this.newPointRepository = newPointRepository;
        this.newPointMessageRepository = newPointMessageRepository;
        this.client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    }

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody String textUpdate) {

        Update update = BotUtils.parseUpdate(textUpdate);

        log.info(update.toString());
        //voting handler
        if (update.callbackQuery() != null) {
            CallbackQuery callbackQuery = update.callbackQuery();
            log.info("Callback query was received. " + callbackQuery.toString());

            AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackQuery.id());
            answer.text("Handled");
            if (callbackQuery.data().equals(TRUE.keyword)) {
                handlePollYesRequest(callbackQuery);
            } else {
                handlePollNoRequest(callbackQuery);
            }

            if (bot.execute(answer).isOk()) {
                log.info("Button was handled.");
            } else {
                log.info("Button was NOT handled!");

            }
            return homePage();
        }


        Message receivedMessage = update.message();

        if (receivedMessage == null) {
            log.info("Received message does not have body. ");
            return homePage();
        }

        User currentUser = receivedMessage.from();
        String receivedMessageText = receivedMessage.text();


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
                sendMessage(currentUser, "Не по масти шелестишь, петушок.");
            }
        }

        return homePage();
    }


    public void handleStartRequest(Update update) {
        long chatId = update.message().chat().id();

        if (!roommateRepository.existsById(chatId)) {
            registerNewRoommate(update.message().chat());
        }
        Optional<Roommate> optRoommate = roommateRepository.findById(chatId);
        if (optRoommate.isPresent()) {
            Roommate roommate = optRoommate.get();
            sendMessage(roommate.getChatId(), "Ас-саламу алейкум, братик");
            log.info("New roommate was registered: " + roommate);

        }
    }

    public void handleStatsRequest(Update update) {
        sendMessage(update.message().from(), generateStatisticsMessage());
    }

    public void handleNewPointRequest(Update update) {
        User currentUser = update.message().from();
        Chat currentChat = update.message().chat();

        Optional<Roommate> potentialRoommate = roommateRepository.findById(currentChat.id());
        if (potentialRoommate.isEmpty()) {
            log.error("The user does not belong to any room." + currentUser.username());
            sendMessage(currentUser, "You do not belong to any room.");
        } else {
            Roommate currentRoommate = potentialRoommate.get();
            NewPoint newPoint = generateNewPoint(currentRoommate);
            currentRoommate.getNewPointList().add(newPoint);
            newPointRepository.save(newPoint);


            List<Roommate> otherRoommates = getListOfRoommates().stream().filter(x -> !x.equals(currentRoommate)).collect(Collectors.toList());
            List<NewPointMessage> messages = new ArrayList<>();

            String pollMessageText = generatePollMessage(currentRoommate);
            for (Roommate roommate : otherRoommates) {
                SendMessage message = new SendMessage(roommate.getChatId(), pollMessageText);

                InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton(TRUE.keyword).callbackData(TRUE.keyword), new InlineKeyboardButton(False.keyword).callbackData(False.keyword));
                message.replyMarkup(replyKeyboardMarkup);

                long messageId = sendMessage(roommate.getChatId(), message);

                NewPointMessage newPointMessage = new NewPointMessage(messageId, roommate.getChatId());
                newPointMessage.setNewPoint(newPoint);
                newPoint.getMessageList().add(newPointMessage);
                messages.add(newPointMessage);
            }

            messages.forEach(newPointMessageRepository::save);
            roommateRepository.save(currentRoommate);
            newPointRepository.save(newPoint);

        }
    }


    public void handlePollRequest(CallbackQuery callbackQuery, State state) {
        Message currentMessage = callbackQuery.message();
        User currentUser = callbackQuery.from();
        Chat currentChat = callbackQuery.message().chat();

        log.info(String.format("%s voted for %s.", currentUser.username(), state.equals(TRUE) ? "yes" : "no"));


        log.info("Get roommate by chat_id: " + currentChat.id());
        var optSentRoommate = roommateRepository.findById(currentChat.id());
        Roommate sentRoommate;
        if (optSentRoommate.isPresent()) {
            sentRoommate = optSentRoommate.get();
        } else {
            log.info("There is no such a roommate with chat_id: " + currentChat.id());
            return;
        }

        log.info("Get new point entity by new_point_message_id: " + currentMessage.messageId() + " " + currentChat.id());
        var optCheckedPoint = newPointMessageRepository.findById(currentMessage.messageId() + " " + currentChat.id());
        NewPoint checkedPoint;
        if (optCheckedPoint.isPresent()) {
            checkedPoint = optCheckedPoint.get().getNewPoint();
        } else {
            log.info("There is no such a new point message with id: " + currentMessage.messageId() + " " + currentChat.id());
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(callbackQuery.id());
            answerCallbackQuery.text("Wrong new point request");
            bot.execute(answerCallbackQuery);

            DeleteMessage deleteMessage = new DeleteMessage(currentChat.id(), currentMessage.messageId());
            var response = bot.execute(deleteMessage);
            if (response.isOk()) {
                log.info("Wrong new point message was deleted from the chat. ");
            } else {
                log.info("Wrong new point message was NOT deleted from the chat. Description:" + response.description());
            }
            return;
        }

        Roommate answeredRoommate = checkedPoint.getRoommate();

        if (state.equals(TRUE))
            answeredRoommate.incrementPoints();
        roommateRepository.save(answeredRoommate);

        String messageText = String.format("%s has %s that %s has done his job.", sentRoommate.getRealName(), state.equals(TRUE) ? "approved" : "declined", answeredRoommate.getRealName());

        for (int i = checkedPoint.getMessageList().size() - 1; i >= 0; i--) {
            NewPointMessage message = checkedPoint.getMessageList().get(i);
            EditMessageText editMessageText = new EditMessageText(message.getChatId(), message.getMessageId(), messageText);

            BaseResponse response = bot.execute(editMessageText);
            if (response.isOk())
                log.info(String.format("Message %d %d was edited.", message.getMessageId(), message.getChatId()));
            else
                log.info(String.format("Message %d %d WAS NOT edited. Description: %s", message.getMessageId(), message.getChatId(), response.description()));

            checkedPoint.getMessageList().remove(i);
            newPointRepository.save(checkedPoint);
            newPointMessageRepository.delete(message);
        }
        newPointRepository.delete(checkedPoint);

        sendMessage(answeredRoommate.getChatId(), messageText);
    }

    public void handlePollYesRequest(CallbackQuery callbackQuery) {
        handlePollRequest(callbackQuery, TRUE);
    }


    public void handlePollNoRequest(CallbackQuery callbackQuery) {
        handlePollRequest(callbackQuery, False);
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

    public void registerNewRoommate(Chat chat) {
        Roommate newRoommate = new Roommate();
        newRoommate.setUserName(chat.username());
        newRoommate.setRealName(chat.firstName());
        newRoommate.setChatId(chat.id());
        newRoommate.setNewPointList(new ArrayList<>());

        log.info("Attempt to add a new roommate to the room: " + newRoommate.toString());
        try {
            roommateRepository.save(newRoommate);
            log.info("New roommate was successfully registered. " + newRoommate.toString());
        } catch (Exception e) { //in case of saving was unsuccessful
            log.info("There is a problem with adding a new roommate to the room: " + newRoommate.toString());
            log.info(e.getMessage());
        }
    }


    public NewPoint generateNewPoint(Roommate roommate) {
        NewPoint newPoint = new NewPoint((int) UUID.randomUUID().getLeastSignificantBits());
        newPoint.setRoommate(roommate);
        newPoint.setCreatedAt(new Date());
        newPoint.setMessageList(new ArrayList<>());
        return newPoint;
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
