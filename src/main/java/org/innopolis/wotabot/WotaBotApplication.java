package org.innopolis.wotabot;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.controllers.MainController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import java.io.IOException;

@SpringBootApplication(scanBasePackageClasses = WotaBotApplication.class)
@EntityScan("org.innopolis.wotabot.models")
@Slf4j
public class WotaBotApplication {

    static TelegramWebhookBot webhookBot;
    static MainController mainController;

    public WotaBotApplication(TelegramWebhookBot webhookBot, MainController mainController) {
        WotaBotApplication.webhookBot = webhookBot;
        WotaBotApplication.mainController = mainController;
    }

    public static void main(String[] args) throws IOException {
        SpringApplication.run(WotaBotApplication.class, args);
        mainController.sendBroadcastMessage(mainController.getListOfRoommates(), "Хозяин начал работать надо мной. Извините, если будет много лишних сообщений");
    }
}
