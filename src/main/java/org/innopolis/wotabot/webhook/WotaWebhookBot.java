package org.innopolis.wotabot.webhook;

import lombok.extern.slf4j.Slf4j;
import org.innopolis.wotabot.config.BotConfig;
import org.springframework.stereotype.Component;
@Component
@Slf4j
public class WotaWebhookBot{

    public String getBotUsername() {
        return BotConfig.BOT_USERNAME;
    }

    public String getBotToken() {
        return BotConfig.BOT_TOKEN;
    }


    public String getBotPath() {
        return BotConfig.BOT_REDIRECT_URL;
    }
}
