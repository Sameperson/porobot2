package com.sameperson.porobot2;

import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TypingActionManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Porobot porobot;
    private final long chatId;

    public TypingActionManager(Porobot porobot, long chatId) {
        this.porobot = porobot;
        this.chatId = chatId;
    }

    public void startSendingTypingAction() {
        final Runnable typingIndicator = () -> {
            SendChatAction chatAction = new SendChatAction();
            chatAction.setChatId(String.valueOf(chatId));
            chatAction.setAction(ActionType.TYPING);
            try {
                porobot.execute(chatAction);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        };
        // Schedule the typing action to be sent every 5 seconds
        scheduler.scheduleAtFixedRate(typingIndicator, 0, 1, TimeUnit.SECONDS);
    }

    public void stopSendingTypingAction() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
