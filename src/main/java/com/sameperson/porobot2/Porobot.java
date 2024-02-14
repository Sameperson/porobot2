package com.sameperson.porobot2;

import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings({"CallToPrintStackTrace", "deprecation"})
@Component
public class Porobot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(Porobot.class);

    @Value("${bot.username}")
    private String botUsername;

    @Value("${bot.token}")
    private String botToken;

    @Autowired
    private AIService aiService;

    @Override
    public void onUpdateReceived(Update update) {
        Long senderChatId = update.getMessage().getChatId();
        logger.info("Chat id: " + senderChatId);

        if (senderChatId != 171839653 && senderChatId != -1001362242100L && senderChatId != -773436654 && senderChatId != -470582604) {
            logger.info("Unauthorized chat id: " + senderChatId);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = senderChatId;

            if (messageText.equalsIgnoreCase("/ping")) {
                // Direct response for simple commands
                sendTextMessage(chatId, update.getMessage().getMessageId(), "Pong!");
            } else if (messageText.startsWith("/prompt") || messageText.regionMatches(true, 0, "поробот", 0, 7)) {
                // Process command that requires AI service response
                processAIResponse(chatId, update.getMessage().getMessageId(), messageText);
            }
        }
    }

    private void sendTextMessage(long chatId, int replyTo, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyToMessageId(replyTo);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processAIResponse(long chatId, int replyTo, String messageText) {
        TypingActionManager typingManager = new TypingActionManager(this, chatId);
        typingManager.startSendingTypingAction();

        // Use CompletableFuture for async processing
        CompletableFuture.supplyAsync(() -> aiService.getResponse(wrapText(removeSpecificStartsIgnoreCase(messageText))))
                .thenAccept(responseText -> {
                    sendTextMessage(chatId, replyTo, responseText);
                    typingManager.stopSendingTypingAction();
                });
    }

    private static String wrapText(String messageText) {
        String messageStart = "Дай відповідь українською лише на те, що оточено символами ₴₴₴ (три знаки гривні) " +
                "не рахуючи це речення. Даючи відповідь, відразу спробуй приплести Петра Порошенка, навіть якщо питання або промпт його не стосується. Ось промпт:$$$ ";
        String messageEnd = " $$$";
        return messageStart + messageText + messageEnd;
    }

    public String removeSpecificStartsIgnoreCase(String source) {
        String[] toRemoveArray = {"поробот", "поробот,", "/prompt"};
        for (String toRemove : toRemoveArray) {
            while (source.length() >= toRemove.length() && source.substring(0, toRemove.length()).equalsIgnoreCase(toRemove)) {
                source = source.substring(toRemove.length());
            }
        }
        return source;
    }

    public void sendTypingAction(long chatId) {
        SendChatAction chatAction = new SendChatAction();
        chatAction.setChatId(String.valueOf(chatId));
        chatAction.setAction(ActionType.TYPING);

        try {
            execute(chatAction);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }
}
