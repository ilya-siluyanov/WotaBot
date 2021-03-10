package org.innopolis.wotabot.config;

public class Constants {
    public static final String SEND_MESSAGE = "https://api.telegram.org/bot%s/sendMessage?chat_id=%s&text=%s";

    public static final String workIsInProgress = "Хозяин начал работать надо мной. Извините, если будет много лишних сообщений";

    public static final String POLL_MESSAGE = "Your roommate %s brought water or took out trash. Is it true?";
    public static class Commands {
        public static final String START = "/start";
        public static final String STATS = "Statistics";
        public static final String NEW_POINT = "New point request";
        public static final String WATER_IS_EMPTY = "Water bottle is empty";
        public static final String TRASH_IS_FULL = "Trash bin is full";

    }
}
