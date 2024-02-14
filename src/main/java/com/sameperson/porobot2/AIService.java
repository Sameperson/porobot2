package com.sameperson.porobot2;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class AIService {

    public static final int HOURLY_LIMIT = 50;
    private AtomicInteger promptCount = new AtomicInteger(0);

    private final ChatClient chatClient;
    private final OpenAiChatClient openAiChatClient;

    @Autowired
    public AIService(ChatClient chatClient,
                     OpenAiChatClient openAiChatClient) {
        this.chatClient = chatClient;
        this.openAiChatClient = openAiChatClient;
    }

    public String getResponse(String message) {
        if (promptCount.get() >= HOURLY_LIMIT) {
            return "Я втомився. Повертайся через годину";
        }

        promptCount.incrementAndGet();

        return openAiChatClient.call(message);
    }

    public Flux<ChatResponse> getResponseFlux(String message) {
        promptCount.incrementAndGet();

        Prompt prompt = new Prompt(new UserMessage(message));
        return openAiChatClient.stream(prompt);
    }

    @Scheduled(fixedRate = 3600000) // 3600000 milliseconds = 1 hour
    public void resetPromptCount() {
        promptCount.set(0);
    }
}
