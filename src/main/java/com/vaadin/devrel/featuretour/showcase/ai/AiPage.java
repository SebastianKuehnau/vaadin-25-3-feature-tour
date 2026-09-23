package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;

/**
 * Base class for the AI demos: shows a setup hint when no LLM API key is
 * configured.
 */
abstract class AiPage extends ShowcasePage {

    protected final AiSupport ai;

    protected AiPage(AiSupport ai, String title, String lead, String docsUrl, Tier... tiers) {
        super(title, lead, docsUrl, tiers);
        this.ai = ai;
        if (ai.isConfigured()) {
            addNote("Powered by " + ai.modelName() + " through Spring AI. Your prompts are sent to " + ai.providerName() + ".");
        } else {
            addNote("No LLM configured. Set OPENAI_API_KEY (or AI_PROVIDER=anthropic and ANTHROPIC_API_KEY) "
                    + "and restart the app to try this demo live.").addClassName("warning");
        }
    }
}
