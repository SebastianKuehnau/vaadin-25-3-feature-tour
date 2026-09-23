package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.InputMode;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.splitlayout.SplitLayoutI18n;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("accessibility")
@PageTitle("Accessibility")
public class AccessibilityView extends ShowcasePage {

    public AccessibilityView() {
        super("Accessibility",
                "Keyboard-resizable Split Layout, input modes for on-screen keyboards, ARIA roles and descriptions, "
                        + "and configurable heading levels.",
                "https://vaadin.com/docs/latest/building-apps/accessibility", Tier.NEW, Tier.FREE);

        addSplitLayoutDemo();
        addInputModeDemo();
        addAriaDemo();
        addHeadingLevelDemo();
    }

    private void addSplitLayoutDemo() {
        var split = new SplitLayout(pane("Navigation"), pane("Content"));
        split.setI18n(new SplitLayoutI18n().setSeparator("Resize navigation and content"));
        split.setSplitterPosition(35);
        split.addClassName("a11y-split");

        addDemo("Split Layout with the keyboard",
                "Press Tab until the splitter is focused, then use the arrow keys to move it. "
                        + "SplitLayoutI18n gives the splitter an accessible name.",
                split, """
                        SplitLayout split = new SplitLayout(navigation, content);
                        split.setI18n(new SplitLayoutI18n()
                                .setSeparator("Resize navigation and content"));
                        """);
    }

    private static Div pane(String text) {
        var pane = new Div(new Span(text));
        pane.addClassName("a11y-pane");
        return pane;
    }

    private void addInputModeDemo() {
        var pin = new TextField("PIN code");
        pin.setInputMode(InputMode.NUMERIC);
        var amount = new TextField("Amount");
        amount.setInputMode(InputMode.DECIMAL);
        var phone = new TextField("Phone");
        phone.setInputMode(InputMode.TEL);
        var search = new TextField("Search");
        search.setInputMode(InputMode.SEARCH);

        var row = new FlexLayout(pin, amount, phone, search);
        row.addClassName("showcase-row");
        addDemo("Input modes",
                "Open this page on a phone: each field brings up the matching on-screen keyboard, while the field stays a plain TextField.",
                row, """
                        pin.setInputMode(InputMode.NUMERIC);
                        amount.setInputMode(InputMode.DECIMAL);
                        phone.setInputMode(InputMode.TEL);
                        """);
    }

    private void addAriaDemo() {
        var hint = new Paragraph("Use at least 12 characters, including a number.");
        hint.setId("password-hint");
        var password = new TextField("New password");
        password.setAriaDescribedBy("password-hint");

        var card = new Card();
        card.setTitle("System status");
        card.add(new Span("All services operational."));
        card.setAriaRole("status");

        var dialog = new Dialog();
        dialog.setHeaderTitle("Delete project?");
        dialog.add(new Paragraph("This can't be undone."));
        var cancel = new Button("Cancel", e -> dialog.close());
        cancel.setAutofocus(true);
        dialog.getFooter().add(cancel, new Button("Delete", e -> dialog.close()));
        dialog.setAriaRole("alertdialog");
        dialog.setAutofocus(true);
        var open = new Button("Open alert dialog", e -> dialog.open());

        addDemo("ARIA roles, descriptions and dialog autofocus",
                "HasAriaDescription links a field to a description, HasAriaRole sets a role on Dialog, Popover, Badge and Card, "
                        + "and the new Dialog autofocus API moves focus into the dialog when it opens.",
                new VerticalLayout(hint, password, card, open), """
                        password.setAriaDescribedBy("password-hint");
                        card.setAriaRole("status");

                        dialog.setAriaRole("alertdialog");
                        dialog.setAutofocus(true);
                        """);
    }

    private void addHeadingLevelDemo() {
        var accordion = new Accordion();
        accordion.setHeadingLevel(3);
        accordion.add("Personal information", new Span("Name, email, phone"));
        accordion.add("Billing address", new Span("Street, city, country"));

        addDemo("Heading levels",
                "Accordion and Login let you pick the heading level of their headings, so they fit the page outline. "
                        + "These panels render as h3 below this section's h2.",
                accordion, """
                        accordion.setHeadingLevel(3);
                        loginForm.setHeadingLevel(2);
                        """);
    }
}
