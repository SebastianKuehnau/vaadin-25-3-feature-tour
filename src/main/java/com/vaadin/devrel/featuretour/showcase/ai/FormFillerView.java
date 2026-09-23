package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasLabel;
import com.vaadin.flow.component.ai.common.AIAttachment;
import com.vaadin.flow.component.ai.common.ConfidenceLevel;
import com.vaadin.flow.component.ai.common.ValueSource;
import com.vaadin.flow.component.ai.form.FieldValueChangeEvent;
import com.vaadin.flow.component.ai.form.FormAIController;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Route("ai-form-filler")
@PageTitle("AI Form Filler")
public class FormFillerView extends AiPage {

    static final String SYSTEM_PROMPT = """
            You help employees fill in expense claims. Use the currency code (e.g. EUR) for the currency field.
            For the business purpose, write one short sentence. Leave fields empty that the user or the
            document doesn't provide.
            """;

    final TextField merchant = new TextField("Merchant");
    final DatePicker date = new DatePicker("Date of purchase");
    final BigDecimalField total = new BigDecimalField("Total amount");
    final ComboBox<String> currency = new ComboBox<>("Currency", "EUR", "USD", "GBP", "SEK", "CHF");
    final Select<String> category = new Select<>();
    final IntegerField guests = new IntegerField("Number of guests");
    final ComboBox<String> payment = new ComboBox<>("Payment method", "Credit card", "Cash", "Bank transfer");
    final TextArea purpose = new TextArea("Business purpose");
    final Checkbox billable = new Checkbox("Billable to a customer");
    final TextField costCenter = new TextField("Internal cost center");
    final FormLayout form = new FormLayout();
    final Div changeLog = new Div();

    public FormFillerView(AiSupport ai) {
        super(ai, "AI Form Filler",
                "FormAIController fills any layout of Vaadin fields from a prompt or an attached document. 25.3 adds "
                        + "source tracking with confidence levels, field markers with a revert option, and change events.",
                "https://vaadin.com/docs/latest/flow/ai-support/ai-powered-form",
                Tier.NEW, Tier.COMMERCIAL, Tier.PREVIEW);

        merchant.setId("merchant");
        date.setId("date");
        total.setId("total");
        currency.setId("currency");
        category.setId("category");
        category.setLabel("Category");
        category.setItems("Meals & entertainment", "Travel", "Accommodation", "Office supplies", "Software");
        guests.setId("guests");
        payment.setId("payment");
        purpose.setId("purpose");
        billable.setId("billable");
        costCenter.setId("cost-center");
        costCenter.setValue("CC-4711");
        costCenter.setHelperText("Hidden from the AI with ignoreField()");
        date.setRequiredIndicatorVisible(true);
        total.setRequiredIndicatorVisible(true);

        form.add(merchant, date, total, currency, category, guests, payment, billable, purpose, costCenter);
        form.setColspan(purpose, 2);
        form.addClassName("ai-form");

        var controller = new FormAIController(form)
                .setSourceTrackingEnabled(true)
                .ignoreField(costCenter)
                .describeField(guests, "Number of people served, including the employee")
                .describeField(billable, "True when the expense relates to a customer project or customer meeting")
                .setFieldMarkerPopoverContentProvider(FormFillerView::sourceDetails);
        controller.addFieldValueChangeListener(this::logChange);

        var messages = new MessageList();
        messages.setMarkdown(true);
        messages.addClassName("ai-form-messages");
        var input = new MessageInput();
        input.setWidthFull();
        var upload = new Upload();
        upload.setMaxFiles(1);
        upload.addClassName("ai-upload");

        AIOrchestrator orchestrator = null;
        if (ai.isConfigured()) {
            orchestrator = AIOrchestrator.builder(ai.newProvider(), SYSTEM_PROMPT)
                    .withInput(input)
                    .withMessageList(messages)
                    .withFileReceiver(upload)
                    .withController(controller)
                    .withAssistantName("Expense assistant")
                    .build();
        } else {
            input.setEnabled(false);
            upload.setVisible(false);
        }

        var receiptBytes = SampleReceipt.png();
        var receiptPreview = new Image(DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(receiptBytes), "receipt.png", "image/png", receiptBytes.length)), "Sample receipt");
        receiptPreview.addClassName("receipt-preview");
        var download = new Anchor(DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(receiptBytes), "receipt.png", "image/png", receiptBytes.length)), "Download receipt.png");
        download.getElement().setAttribute("download", "receipt.png");

        var finalOrchestrator = orchestrator;
        var fromReceipt = new Button("Fill from the sample receipt", VaadinIcon.MAGIC.create(), e ->
                Objects.requireNonNull(finalOrchestrator).prompt("Fill in the expense claim from the attached receipt.",
                        List.of(new AIAttachment("receipt.png", "image/png", receiptBytes))));
        fromReceipt.addThemeVariants(ButtonVariant.PRIMARY);
        var fromText = new Button("Fill from a sentence", e -> Objects.requireNonNull(finalOrchestrator).prompt(
                "Taxi from the airport to the hotel yesterday, 38 euros, paid by card. It was for the Vaadin Create conference."));
        fromReceipt.setEnabled(orchestrator != null);
        fromText.setEnabled(orchestrator != null);

        var examples = new HorizontalLayout(fromReceipt, fromText);
        examples.setWrap(true);

        var assistant = new VerticalLayout(examples, messages, upload, input);
        assistant.setPadding(false);
        assistant.addClassName("ai-assistant-panel");

        var receipt = new VerticalLayout(receiptPreview, download);
        receipt.setPadding(false);
        receipt.setAlignItems(FlexComponent.Alignment.CENTER);
        receipt.addClassName("receipt-panel");

        var layout = new HorizontalLayout(receipt, new VerticalLayout(form, assistant));
        layout.setWidthFull();
        layout.addClassName("ai-form-layout");

        addDemo("Expense claim from a receipt",
                "Click \"Fill from the sample receipt\", or attach any receipt and type a request. Fields the AI wrote get an "
                        + "AI badge: open it to see the source text, the confidence and a revert button.",
                layout, """
                        FormAIController controller = new FormAIController(form)
                                .setSourceTrackingEnabled(true)                // new in 25.3
                                .ignoreField(costCenter)                       // never sent to the LLM
                                .describeField(guests, "Number of people served")
                                .setFieldMarkerPopoverContentProvider(change -> // new in 25.3
                                        change.getFieldSource().map(source -> sourceDetails(source)).orElse(null));

                        controller.addFieldValueChangeListener(event ->
                                audit(event.getField(), event.getOldValue(), event.getNewValue()));

                        AIOrchestrator.builder(new SpringAILLMProvider(chatModel), systemPrompt)
                                .withInput(messageInput)
                                .withMessageList(messageList)
                                .withFileReceiver(upload)
                                .withController(controller)
                                .build();
                        """);

        changeLog.addClassName("ai-change-log");
        changeLog.add(new Span("Changes made by the AI appear here."));
        addDemo("Reacting to AI changes",
                "addFieldValueChangeListener() fires once per changed field after a successful turn, with the old value, "
                        + "the new value and the reported source.",
                changeLog, "");
    }

    static Component sourceDetails(FieldValueChangeEvent change) {
        return change.getFieldSource().map(FormFillerView::sourceDetails).orElse(null);
    }

    static Component sourceDetails(ValueSource source) {
        var details = new Div();
        details.addClassName("ai-source");
        if (source.confidence() != null) {
            var confidence = new Span("Confidence: " + source.confidence().name().toLowerCase(Locale.ROOT));
            confidence.addClassNames("ai-confidence", source.confidence() == ConfidenceLevel.HIGH ? "high"
                    : source.confidence() == ConfidenceLevel.MEDIUM ? "medium" : "low");
            details.add(confidence);
        }
        source.extracts().forEach(extract -> {
            var quote = new Span("“" + extract.text() + "”");
            quote.addClassName("ai-extract");
            details.add(quote);
        });
        return details;
    }

    private void logChange(FieldValueChangeEvent event) {
        if (changeLog.getComponentCount() == 1 && changeLog.getComponentAt(0) instanceof Span) {
            changeLog.removeAll();
        }
        String label = event.getField() instanceof HasLabel hasLabel ? hasLabel.getLabel() : "Field";
        String confidence = event.getFieldSource().map(ValueSource::confidence)
                .map(level -> " · confidence " + level.name().toLowerCase(Locale.ROOT)).orElse("");
        var entry = new Div(new Span(label + ": "), new Span(format(event.getOldValue()) + " → " + format(event.getNewValue())),
                new Span(confidence));
        entry.addClassName("ai-change");
        changeLog.addComponentAsFirst(entry);
    }

    private static String format(Object value) {
        return value == null || "".equals(value) ? "(empty)" : String.valueOf(value);
    }
}
