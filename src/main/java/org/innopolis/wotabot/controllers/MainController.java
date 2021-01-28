package org.innopolis.wotabot.controllers;

import lombok.extern.slf4j.Slf4j;
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
        for (int i = 0; i < req.length(); i++) {
            int level = 0;
            if (req.charAt(i) == '{') {
                req = req.substring(0, i + 1) + "\n";
                level++;
                for (int j = 0; j < level; j++) {
                    req = req + "\t";
                }
                req += req.substring(i + 1);
            } else if (req.charAt(i) == ',') {
                req = req.substring(0, i + 1) + "\n";
                for (int j = 0; j < level; j++) {
                    req = req + "\t";
                }
                req += req.substring(i + 1);
            } else if (req.charAt(i) == '}') {
                level--;
                req = req.substring(0, i + 1) + "\n";
                for (int j = 0; j < level; j++) {
                    req = req + "\t";
                }
                req += req.substring(i + 1);
            }
        }

        log.info(req);
        return "home";
    }

}
