package org.innopolis.wotabot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.models.Roommate;

@Slf4j
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

    public static void sendMessage(long chatId, String messageText) {
        if (messageText.isEmpty()) {
            messageText = "Почему-то пустое сообщение";
        }

        SendMessage message = new SendMessage(chatId, messageText);
        log.info(String.format("Attempt to send message \"%s\" to chat \"%d\"", messageText, chatId));
        BaseResponse response = bot.execute(message);
        if (response.isOk()) {
            log.info("Message was sent successfully.");
        } else {
            log.info(String.format("Problems with message sending! %s %d", messageText, chatId));
        }
    }

    public static void sendMessage(Chat chat, String message) {
        sendMessage(chat.id(), message);
    }

}
