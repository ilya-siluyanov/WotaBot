package org.innopolis.wotabot.config;

public class Constants {
    public static final String SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    public static class Commands {
        public static final String START = "/start";
        public static final String STATS = "/stats";
        public static final String NEW_POINT = "/new_point";
        public static final String POLL_YES = "/poll_yes";

    }
}
