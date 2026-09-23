package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.flow.component.ai.provider.SpringAILLMProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Creates LLM providers for the AI demos. Every orchestrator needs its own
 * provider instance, so views ask for a fresh one.
 * <p>
 * The chat model comes from Spring AI; {@code spring.ai.model.chat} picks
 * OpenAI (default) or Anthropic.
 */
@Component
public class AiSupport {

    private final ObjectProvider<ChatModel> chatModel;
    private final String provider;
    private final String apiKey;
    private final String model;

    AiSupport(ObjectProvider<ChatModel> chatModel, Environment env) {
        this.chatModel = chatModel;
        this.provider = env.getProperty("spring.ai.model.chat", "openai").toLowerCase(Locale.ROOT);
        this.apiKey = env.getProperty("spring.ai." + provider + ".api-key", "");
        this.model = env.getProperty("spring.ai." + provider + ".chat.options.model", provider);
    }

    /**
     * Tells whether an API key is configured, so the demos can show a hint
     * instead of failing on the first prompt.
     */
    public boolean isConfigured() {
        return !apiKey.isBlank() && !"missing".equals(apiKey) && chatModel.getIfAvailable() != null;
    }

    public String modelName() {
        return model;
    }

    public String providerName() {
        return "anthropic".equals(provider) ? "Anthropic" : "OpenAI";
    }

    public SpringAILLMProvider newProvider() {
        return new SpringAILLMProvider(chatModel.getObject());
    }
}
