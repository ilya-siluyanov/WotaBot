package org.innopolis.wotabot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication(scanBasePackageClasses = WotaBotApplication.class)
@EntityScan("org.innopolis.wotabot.models")
@Slf4j
public class WotaBotApplication {


    static TelegramWebhookBot webhookBot;
    static TelegramBotsApi botsApi;

    public WotaBotApplication(TelegramWebhookBot webhookBot) {
        WotaBotApplication.webhookBot = webhookBot;
    }

    public static void main(String[] args) throws TelegramApiException {
        SpringApplication.run(WotaBotApplication.class, args);
//        String url = String.format("https://api.telegram.org/bot%s/setWebhook?url=%s",
//                BotConfig.BOT_TOKEN,
//                BotConfig.BOT_REDIRECT_URL);
//        DefaultWebhook defaultWebhook = new DefaultWebhook();
//        //TODO: set internal url
//        defaultWebhook.registerWebhook(webhookBot);
//        botsApi = new TelegramBotsApi(DefaultBotSession.class, defaultWebhook);
//        botsApi.registerBot(webhookBot, new SetWebhook(url));
    }
}
