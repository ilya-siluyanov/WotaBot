package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramWebhookBot;

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
        System.out.println(jsonObject.toString());
        return "home";
    }

}
