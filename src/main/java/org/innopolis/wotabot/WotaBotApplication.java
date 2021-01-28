package org.innopolis.wotabot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
public class WotaBotApplication {


    static TelegramWebhookBot webhookBot;

    public WotaBotApplication(TelegramWebhookBot webhookBot) {
        WotaBotApplication.webhookBot = webhookBot;
    }

    public static void main(String[] args) throws TelegramApiException {
        SpringApplication.run(WotaBotApplication.class, args);
        String url = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s",
                BotConfig.BOT_TOKEN,
                BotConfig.BOT_REDIRECT_URL);
        webhookBot.setWebhook(new SetWebhook(url));
    }

}
