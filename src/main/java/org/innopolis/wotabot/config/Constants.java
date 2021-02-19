package org.innopolis.wotabot.config;

public class Constants {
    public static final String SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    public static class Commands {
        public static final String START = "/start";
        public static final String STATS = "/stats";
        public static final String NEW_POINT = "/new_point";
        public static final String POLL_YES = "/poll_yes";
        public static final String POLL_NO = "/poll_no";
        public static final String WATER_IS_EMPTY = "/water_bottle_is_empty";
        public static final String TRASH_IS_FULL = "/trash_bin_is_full";

    }
}
