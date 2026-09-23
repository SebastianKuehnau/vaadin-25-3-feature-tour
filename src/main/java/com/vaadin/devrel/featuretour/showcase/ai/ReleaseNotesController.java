package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.flow.component.ai.orchestrator.AIController;
import com.vaadin.flow.component.ai.provider.LLMProvider;
import com.vaadin.flow.component.ai.provider.ToolException;
import tools.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A custom, framework-agnostic controller that gives the LLM a tool for
 * looking up Vaadin 25.3 release notes. Custom controllers only need the free
 * vaadin-ai-core-flow module.
 */
class ReleaseNotesController implements AIController {

    static final Map<String, String> NOTES = new LinkedHashMap<>();

    static {
        NOTES.put("switch", "Switch: a new on/off input with helper text, required indicator, small, reverse and icon variants. Free.");
        NOTES.put("table", "Table: a new family of semantic HTML table components with signal-bound rows. Replaces NativeTable. Free.");
        NOTES.put("breadcrumbs", "Breadcrumbs: built from routes or defined manually; no feature flag any more. Free.");
        NOTES.put("date-picker", "Date Picker: disabled dates and weekdays, DateMetadataProvider, default time in Date Time Picker. Free.");
        NOTES.put("combo-box", "Combo Box: PartialMatchMode FIRST_MATCH and ONLY_MATCH. Free.");
        NOTES.put("message-list", "Message List: bubble and one-to-one variants, SELF and FULL_WIDTH item variants, experimental typing indicator. Free.");
        NOTES.put("upload", "Upload: UploadValidator with metadata, header and complete phases; UploadRejectedException. Free.");
        NOTES.put("binder", "Binder: JSR-303 validation groups in BeanValidationBinder. Free.");
        NOTES.put("event-bus", "Service event bus: session lock, RPC invocation and data provider events. Replaces the listeners from 25.2. Free.");
        NOTES.put("ai", "AI: split into free vaadin-ai-core-flow and commercial vaadin-ai-extensions-flow. New: request interceptor, response metadata with token usage, ToolException, form filler source tracking.");
        NOTES.put("observability", "Observability Kit 5: agent-less, one dependency, Vaadin-specific metrics via Micrometer, insights endpoint. Commercial.");
        NOTES.put("typescript-engine", "The client engine was ported from GWT to TypeScript. No application changes needed; add-ons using com.vaadin.client.* must be rewritten.");
        NOTES.put("dev-loop", "Dev Loop CLI (preview): mvn vaadin:install-dev-cli, then vaadin-dev apply reports by exit code whether a change is live. Built for AI agents.");
        NOTES.put("sse-push", "Experimental Server-Sent Events push transport behind the ssePushTransport feature flag.");
    }

    private final Consumer<String> toolLog;

    ReleaseNotesController(Consumer<String> toolLog) {
        this.toolLog = toolLog;
    }

    @Override
    public List<LLMProvider.ToolSpec> getTools() {
        return List.of(new LLMProvider.ToolSpec() {
            @Override
            public String getName() {
                return "ReleaseNotes_lookup";
            }

            @Override
            public String getDescription() {
                // Deliberately no list of keys here: an unknown key makes the tool throw
                // a ToolException, and the model learns the valid keys from its message
                return "Looks up the Vaadin 25.3 release notes for one feature, by a short lowercase key "
                        + "such as 'switch' or 'table'.";
            }

            @Override
            public String getParametersSchema() {
                return """
                        {
                          "type": "object",
                          "properties": {
                            "feature": { "type": "string", "description": "One of the known feature keys" }
                          },
                          "required": ["feature"]
                        }""";
            }

            @Override
            public String execute(JsonNode arguments) {
                String feature = arguments.path("feature").asString("").toLowerCase(Locale.ROOT).trim();
                String note = NOTES.get(feature);
                if (note == null) {
                    toolLog.accept("ReleaseNotes_lookup(\"" + feature + "\") → ToolException");
                    // The message is relayed to the LLM, so it can retry with a valid key
                    throw new ToolException("Unknown feature '" + feature + "'. Use one of: "
                            + String.join(", ", NOTES.keySet()));
                }
                toolLog.accept("ReleaseNotes_lookup(\"" + feature + "\") → ok");
                return note;
            }
        });
    }
}
