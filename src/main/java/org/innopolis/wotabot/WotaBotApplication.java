package org.innopolis.wotabot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.controllers.MainController;
import org.innopolis.wotabot.webhook.WotaWebhookBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackageClasses = WotaBotApplication.class)
@EntityScan("org.innopolis.wotabot.models")
@Slf4j
public class WotaBotApplication {

    static TelegramBot bot;
    static WotaWebhookBot wotaWebhookBot;
    static MainController mainController;


    public WotaBotApplication(TelegramBot bot, WotaWebhookBot wotaWebhookBot, MainController mainController) {
        WotaBotApplication.mainController = mainController;
        WotaBotApplication.bot = bot;
        WotaBotApplication.wotaWebhookBot = wotaWebhookBot;
    }

    public static void main(String[] args) {
        SpringApplication.run(WotaBotApplication.class, args);

        SetWebhook request = new SetWebhook()
                .url(wotaWebhookBot.getBotPath());

        BaseResponse response = bot.execute(request);

        bot.setUpdatesListener((updates) -> {
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        if (response.isOk()) {
            log.info("Webhook was set successfully.");
        } else {
            log.info("PROBLEMS WITH SETTING WEBHOOK");
        }
    }
}
