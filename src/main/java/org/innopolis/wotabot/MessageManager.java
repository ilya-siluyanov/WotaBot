package org.innopolis.wotabot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.models.Roommate;
import org.springframework.stereotype.Component;

import static org.innopolis.wotabot.config.Constants.Commands.*;

@Slf4j
@Component
public class MessageManager {

    static TelegramBot bot;

    public MessageManager(TelegramBot bot) {
        MessageManager.bot = bot;
    }

    public static void sendBroadcastMessage(Iterable<Roommate> roommates, String message) {
        for (Roommate roommate : roommates) {
            sendMessage(roommate.getChatId(), message);
        }
    }


    public static void sendMessage(User user, String messageText) {
        sendMessage(user.id(), messageText);
    }

    public static void sendMessage(User user, SendMessage message) {
        sendMessage(user.id(), message);
    }

    public static void sendMessage(long chatId, String messageText) {
        SendMessage message = new SendMessage(chatId, messageText);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(new KeyboardButton[][]{
                {new KeyboardButton(STATS), new KeyboardButton(NEW_POINT)},
                {new KeyboardButton(WATER_IS_EMPTY), new KeyboardButton(TRASH_IS_FULL)},
        });
        message.replyMarkup(replyKeyboardMarkup);
        sendMessage(chatId, message);
    }


    public static void sendMessage(long chatId, SendMessage message) {
        log.info(String.format("Attempt to send message \"%s\" to chat \"%d\"", message, chatId));
        //TODO : eradicate this!
        message.disableNotification(true);
        BaseResponse response = bot.execute(message);
        log.info(response.description());
    }
}
