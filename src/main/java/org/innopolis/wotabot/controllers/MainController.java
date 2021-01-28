package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

import java.util.Date;

@Controller
@Slf4j
public class MainController {
    @Autowired
    TelegramWebhookBot bot;

    @GetMapping
    public String homePage() {
        return "home";
    }

    @PostMapping
    public String post(@RequestBody String req) {
        JSONObject jsonObject = new JSONObject(req);
        println(jsonObject);
        return "home";
    }


    void println(JSONObject obj) {
        JSONObject messageInfo = obj.getJSONObject("message");
        JSONObject chatInfo = obj.getJSONObject("chat");
        Date sent = new Date(obj.getLong("date"));
        String text = obj.getString("text");

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append(messageInfo.toString());
        sb.append(chatInfo.toString());
        sb.append("}");
        System.out.println(sb.toString());

    }
}
