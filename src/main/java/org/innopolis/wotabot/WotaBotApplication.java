package org.innopolis.wotabot;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.controllers.MainController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import java.io.IOException;
import java.util.Map;

import static org.innopolis.wotabot.config.Constants.*;

@SpringBootApplication(scanBasePackageClasses = WotaBotApplication.class)
@EntityScan("org.innopolis.wotabot.models")
@Slf4j
@EnableConfigurationProperties(WotaBotApplication.class)
@ConfigurationProperties("wotabot")
public class WotaBotApplication {

    static TelegramWebhookBot webhookBot;
    static MainController mainController;

    static boolean isWorkHasStarted;
    static boolean isWorkDone;

    public static boolean isIsWorkHasStarted() {
        return isWorkHasStarted;
    }

    public static void setIsWorkHasStarted(boolean isWorkHasStarted) {
        WotaBotApplication.isWorkHasStarted = isWorkHasStarted;
    }

    public static boolean isIsWorkDone() {
        return isWorkDone;
    }

    public static void setIsWorkDone(boolean isWorkDone) {
        WotaBotApplication.isWorkDone = isWorkDone;
    }

    public WotaBotApplication(TelegramWebhookBot webhookBot, MainController mainController) {
        WotaBotApplication.webhookBot = webhookBot;
        WotaBotApplication.mainController = mainController;
    }

    public static void main(String[] args) {
        SpringApplication.run(WotaBotApplication.class, args);
        if (isWorkHasStarted) {
            if (!isWorkDone) {
//                mainController.sendBroadcastMessage(mainController.getListOfRoommates(), workIsInProgress);
                log.info("VARIABLES WORK!");
            }
        }
    }
}
