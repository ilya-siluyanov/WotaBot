package org.innopolis.wotabot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class WotaWebhookBot extends TelegramWebhookBot {

    @Override
    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        log.info("BOT GOT A MESSAGE");
        if (update.hasMessage() && !update.getMessage().getText().isEmpty()) {
            SendMessage message = new SendMessage();
            message.setText("Well, I got a message");
            message.setChatId(update.getMessage().getChatId().toString());
            return message;
        }
        return null;
    }

    @Override
    public String getBotPath() {
        return BotConfig.BOT_REDIRECT_URL;
    }
}
