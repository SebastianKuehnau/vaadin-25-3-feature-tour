package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.messages.MessageListItemVariant;
import com.vaadin.flow.component.messages.MessageListTypingIndicatorType;
import com.vaadin.flow.component.messages.MessageListUser;
import com.vaadin.flow.component.messages.MessageListVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Route("message-list")
@PageTitle("Message List")
public class MessageListView extends ShowcasePage {

    static final String BOT = "Vaadin Bot";
    static final String ME = "You";

    final MessageList chat = new MessageList();
    final MessageInput input = new MessageInput();

    public MessageListView() {
        super("Message List",
                "Chat-style bubble and one-to-one variants, per-message variants for your own messages, "
                        + "and an experimental typing indicator.",
                "https://vaadin.com/docs/latest/components/message-list", Tier.NEW, Tier.FREE, Tier.EXPERIMENTAL);

        chat.addThemeVariants(MessageListVariant.BUBBLE);
        chat.setMarkdown(true);
        chat.addClassName("showcase-chat");
        chat.setTypingIndicatorType(MessageListTypingIndicatorType.ELLIPSIS);
        chat.setItems(
                botMessage("Hi! 👋 I'm a scripted bot. Ask me about **Switch**, **Table** or **AI**."),
                selfMessage("What's the big thing in 25.3?"),
                botMessage("Hard to pick one! The client engine moved from GWT to **TypeScript**, "
                        + "and there are new components like Switch and Table."));

        input.setWidthFull();
        input.addSubmitListener(e -> {
            chat.addItem(selfMessage(e.getValue()));
            replyLater(e.getValue());
        });

        var variant = new RadioButtonGroup<String>("List variant");
        variant.setItems("Bubble", "One-to-one", "Default");
        variant.setValue("Bubble");
        variant.addValueChangeListener(e -> {
            chat.removeThemeVariants(MessageListVariant.values());
            switch (e.getValue()) {
                case "Bubble" -> chat.addThemeVariants(MessageListVariant.BUBBLE);
                case "One-to-one" -> chat.addThemeVariants(MessageListVariant.ONE_TO_ONE);
                default -> {
                }
            }
        });

        var indicator = new RadioButtonGroup<MessageListTypingIndicatorType>("Typing indicator");
        indicator.setItems(MessageListTypingIndicatorType.values());
        indicator.setItemLabelGenerator(type -> type.name().charAt(0) + type.name().substring(1).toLowerCase(Locale.ROOT));
        indicator.setValue(MessageListTypingIndicatorType.ELLIPSIS);
        indicator.addValueChangeListener(e -> chat.setTypingIndicatorType(e.getValue()));

        var options = new FlexLayout(variant, indicator);
        options.addClassName("showcase-row");

        var chatBox = new VerticalLayout(chat, input);
        chatBox.addClassName("showcase-chat-box");
        chatBox.setPadding(false);

        addDemo("A one-to-one chat",
                "Send a message: your own messages use the SELF item variant, and the bot shows a typing indicator "
                        + "before its answer arrives through server push.",
                new VerticalLayout(options, chatBox), """
                        MessageList chat = new MessageList();
                        chat.addThemeVariants(MessageListVariant.BUBBLE);

                        MessageListItem mine = new MessageListItem(text, Instant.now(), "You");
                        mine.addThemeVariants(MessageListItemVariant.SELF);
                        chat.addItem(mine);

                        // Experimental: feature flag com.vaadin.experimental.messageListTypingIndicator
                        chat.setTypingIndicatorType(MessageListTypingIndicatorType.ELLIPSIS);
                        chat.setTypingUsers(new MessageListUser("Vaadin Bot"));
                        """);
    }

    private void replyLater(String question) {
        UI ui = UI.getCurrentOrThrow();
        chat.setTypingUsers(new MessageListUser(BOT));
        CompletableFuture.runAsync(() -> ui.access(() -> {
            chat.setTypingUsers();
            chat.addItem(botMessage(answer(question)));
        }), CompletableFuture.delayedExecutor(1500, TimeUnit.MILLISECONDS));
    }

    static String answer(String question) {
        String q = question.toLowerCase(Locale.ROOT);
        if (q.contains("switch")) {
            return "**Switch** is a new on/off input for settings that apply immediately. It has small, reverse and icon variants.";
        } else if (q.contains("table")) {
            return "The new **Table** family builds semantic HTML tables in Java, and rows can be bound to a `ListSignal`.";
        } else if (q.contains("ai")) {
            return "The AI support is split: `vaadin-ai-core-flow` is free, and the form, grid and chart controllers are commercial. "
                    + "Try the *AI* pages in the menu!";
        } else if (q.contains("hello") || q.contains("hi")) {
            return "Hello there! 😊";
        }
        return "Good question! I'm only a scripted bot, but the *AI Assistant* page has a real LLM behind it.";
    }

    static MessageListItem botMessage(String text) {
        var item = new MessageListItem(text, Instant.now(), BOT);
        item.setUserColorIndex(2);
        return item;
    }

    static MessageListItem selfMessage(String text) {
        var item = new MessageListItem(text, Instant.now(), ME);
        item.addThemeVariants(MessageListItemVariant.SELF);
        item.setUserColorIndex(1);
        return item;
    }
}
