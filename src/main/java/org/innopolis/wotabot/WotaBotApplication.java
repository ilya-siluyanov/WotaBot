package org.innopolis.wotabot;

import org.innopolis.wotabot.config.BotConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication(scanBasePackageClasses = WotaBotApplication.class)
@EntityScan("org.innopolis.wotabot.models")
public class WotaBotApplication {


    static TelegramWebhookBot webhookBot;
    static TelegramBotsApi botsApi;

    public WotaBotApplication(TelegramWebhookBot webhookBot) throws TelegramApiException {
        WotaBotApplication.webhookBot = webhookBot;
        botsApi = new TelegramBotsApi(DefaultBotSession.class);
    }

    public static void main(String[] args) throws TelegramApiException {
        SpringApplication.run(WotaBotApplication.class, args);
        String url = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s",
                BotConfig.BOT_TOKEN,
                BotConfig.BOT_REDIRECT_URL);
        botsApi.registerBot(webhookBot, new SetWebhook(url));
    }
}
