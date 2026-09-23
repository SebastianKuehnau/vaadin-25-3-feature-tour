package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.checkbox.SwitchVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.ColorScheme;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

@Route("switch")
@PageTitle("Switch")
public class SwitchView extends ShowcasePage {

    final Switch notifications = new Switch("Email notifications");
    final Span notificationStatus = new Span();
    final Switch terms = new Switch("I accept the terms of service");

    public SwitchView() {
        super("Switch",
                "A binary on/off input for settings that take effect immediately. "
                        + "Unlike a checkbox, a switch signals \"this changes something right now\".",
                "https://vaadin.com/docs/latest/components/switch", Tier.NEW, Tier.FREE);

        addBasicDemo();
        addStatesDemo();
        addVariantsDemo();
        addLiveSettingsDemo();
    }

    private void addBasicDemo() {
        ValueSignal<Boolean> enabled = new ValueSignal<>(true);
        notifications.setHelperText("We only send release announcements.");
        notifications.bindValue(enabled, enabled::set);
        notificationStatus.addClassName("showcase-status");
        notificationStatus.bindText(() -> enabled.get()
                ? "✅ You will get an email for every release."
                : "🔕 Notifications are off.");

        addDemo("Basic usage",
                "A Switch is a regular HasValue<Boolean> field, so it binds to signals, Binder and listeners like any other field.",
                new VerticalLayout(notifications, notificationStatus), """
                        ValueSignal<Boolean> enabled = new ValueSignal<>(true);
                        Switch notifications = new Switch("Email notifications");
                        notifications.setHelperText("We only send release announcements.");
                        notifications.bindValue(enabled, enabled::set);

                        status.bindText(() -> enabled.get()
                                ? "You will get an email for every release."
                                : "Notifications are off.");
                        """);
    }

    private void addStatesDemo() {
        terms.setRequiredIndicatorVisible(true);
        terms.setI18n(new Switch.SwitchI18n().setRequiredErrorMessage("You need to accept the terms to continue."));
        terms.setHelperText("Toggle on and off again to see the required error.");

        var disabled = new Switch("Disabled", true);
        disabled.setEnabled(false);
        var readOnly = new Switch("Read-only", true);
        readOnly.setReadOnly(true);

        addDemo("Required, disabled and read-only",
                "Switch supports helper text, a required indicator with an error message, and the usual disabled and read-only states.",
                new VerticalLayout(terms, disabled, readOnly), """
                        Switch terms = new Switch("I accept the terms of service");
                        terms.setRequiredIndicatorVisible(true);
                        terms.setI18n(new Switch.SwitchI18n()
                                .setRequiredErrorMessage("You need to accept the terms to continue."));

                        Switch disabled = new Switch("Disabled", true);
                        disabled.setEnabled(false);
                        Switch readOnly = new Switch("Read-only", true);
                        readOnly.setReadOnly(true);
                        """);
    }

    private void addVariantsDemo() {
        var small = new Switch("Small", true);
        small.addThemeVariants(SwitchVariant.AURA_SMALL);
        var reverse = new Switch("Label first (reverse)", true);
        reverse.addThemeVariants(SwitchVariant.AURA_REVERSE);
        reverse.setWidth("16rem");
        var icon = new Switch("With icons", true);
        icon.addThemeVariants(SwitchVariant.ICON);

        var row = new FlexLayout(small, reverse, icon);
        row.addClassName("showcase-row");
        addDemo("Theme variants",
                "Small and reverse styles for the Aura theme, and an icon variant that shows a check or cross inside the thumb.",
                row, """
                        small.addThemeVariants(SwitchVariant.AURA_SMALL);
                        reverse.addThemeVariants(SwitchVariant.AURA_REVERSE);
                        icon.addThemeVariants(SwitchVariant.ICON);
                        """);
    }

    private void addLiveSettingsDemo() {
        var darkMode = new Switch("Dark mode");
        darkMode.setHelperText("Switches the Aura color scheme of this app.");
        darkMode.addValueChangeListener(e -> UI.getCurrentOrThrow().getPage().setColorScheme(
                e.getValue() ? ColorScheme.Value.DARK : ColorScheme.Value.LIGHT));

        ValueSignal<Boolean> compactMode = new ValueSignal<>(false);
        var compact = new Switch("Compact preview card");
        compact.bindValue(compactMode, compactMode::set);
        var card = new Div(new Span("Preview card"), new Span("Density follows the switch."));
        card.addClassName("switch-preview-card");
        card.bindClassName("compact", compactMode);

        addDemo("Settings that apply instantly",
                "The typical use case: settings that take effect right away, without a Save button.",
                new VerticalLayout(darkMode, compact, card), """
                        darkMode.addValueChangeListener(e -> UI.getCurrent().getPage()
                                .setColorScheme(e.getValue() ? ColorScheme.Value.DARK : ColorScheme.Value.LIGHT));

                        card.bindClassName("compact", compactMode);
                        """);
    }
}
