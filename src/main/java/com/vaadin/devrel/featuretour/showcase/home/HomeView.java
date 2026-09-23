package com.vaadin.devrel.featuretour.showcase.home;

import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.devrel.featuretour.showcase.ai.AssistantView;
import com.vaadin.devrel.featuretour.showcase.ai.DataExplorerView;
import com.vaadin.devrel.featuretour.showcase.ai.FormFillerView;
import com.vaadin.devrel.featuretour.showcase.components.AccessibilityView;
import com.vaadin.devrel.featuretour.showcase.components.BreadcrumbsView;
import com.vaadin.devrel.featuretour.showcase.components.ComboBoxView;
import com.vaadin.devrel.featuretour.showcase.components.DatePickerView;
import com.vaadin.devrel.featuretour.showcase.components.GridView;
import com.vaadin.devrel.featuretour.showcase.components.MessageListView;
import com.vaadin.devrel.featuretour.showcase.components.SwitchView;
import com.vaadin.devrel.featuretour.showcase.components.TableView;
import com.vaadin.devrel.featuretour.showcase.components.UploadView;
import com.vaadin.devrel.featuretour.showcase.flow.BinderGroupsView;
import com.vaadin.devrel.featuretour.showcase.flow.EventBusView;
import com.vaadin.devrel.featuretour.showcase.flow.ReactiveElementsView;
import com.vaadin.devrel.featuretour.showcase.ops.ObservabilityView;
import com.vaadin.devrel.featuretour.showcase.ops.UnderTheHoodView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("")
@PageTitle("Home")
public class HomeView extends VerticalLayout {

    record Feature(String title, String summary, Class<? extends Component> view, Tier... tiers) {
    }

    public HomeView() {
        addClassName("home-view");

        var eyebrow = new Span("Released September 23, 2026 · Curated by Vaadin DevRel");
        eyebrow.addClassName("home-eyebrow");
        var title = new H1("What's new in Vaadin 25.3");
        var lead = new Paragraph("A hands-on tour of the release: new components, smarter existing ones, framework "
                + "APIs, AI features and operations. Every page is a live demo with the Java code behind it.");
        var releaseNotes = new Anchor("https://github.com/vaadin/platform/releases/tag/25.3.0", "Release notes");
        releaseNotes.setTarget(AnchorTarget.BLANK);
        var blog = new Anchor("https://vaadin.com/blog/vaadin-25-3-release", "Release blog post");
        blog.setTarget(AnchorTarget.BLANK);
        var links = new HorizontalLayout(releaseNotes, blog);
        var hero = new Div(eyebrow, title, lead, links);
        hero.addClassName("home-hero");
        add(hero);

        section("New components",
                new Feature("Switch", "An on/off input for settings that apply immediately.", SwitchView.class, Tier.NEW, Tier.FREE),
                new Feature("Table", "Semantic HTML tables in Java, with signal-bound rows.", TableView.class, Tier.NEW, Tier.FREE),
                new Feature("Breadcrumbs", "A navigation trail built from your routes. Look at the header!", BreadcrumbsView.class, Tier.FREE));

        section("Component improvements",
                new Feature("Date Picker", "Disabled dates and weekdays, date metadata, default time.", DatePickerView.class, Tier.FREE),
                new Feature("Combo Box", "Partial match modes for typed values.", ComboBoxView.class, Tier.FREE),
                new Feature("Message List", "Bubble and one-to-one chat variants, typing indicator.", MessageListView.class, Tier.FREE, Tier.EXPERIMENTAL),
                new Feature("Upload Validation", "Reject uploads by metadata, magic bytes or content.", UploadView.class, Tier.FREE),
                new Feature("Grid", "No data for hidden columns, GridI18n, TreeGrid select all.", GridView.class, Tier.FREE),
                new Feature("Accessibility", "Keyboard Split Layout, input modes, ARIA roles.", AccessibilityView.class, Tier.FREE));

        section("Flow framework",
                new Feature("Validation Groups", "Draft vs. publish rules in BeanValidationBinder.", BinderGroupsView.class, Tier.FREE),
                new Feature("Size Signal & whenAttached", "Reactive element sizes and attach-scoped setup.", ReactiveElementsView.class, Tier.FREE),
                new Feature("Service Event Bus", "Session lock, RPC and data provider events.", EventBusView.class, Tier.FREE));

        section("AI",
                new Feature("AI Form Filler", "Fill forms from documents, with sources and confidence.", FormFillerView.class, Tier.COMMERCIAL, Tier.PREVIEW),
                new Feature("AI Data Explorer", "Natural-language questions to Grid and Chart.", DataExplorerView.class, Tier.COMMERCIAL, Tier.PREVIEW),
                new Feature("AI Assistant", "Request interceptor, token usage and ToolException.", AssistantView.class, Tier.FREE, Tier.PREVIEW));

        section("Operations & tooling",
                new Feature("Observability Kit 5", "Agent-less metrics for Vaadin apps.", ObservabilityView.class, Tier.COMMERCIAL),
                new Feature("Under the Hood", "TypeScript engine, Dev Loop CLI, Copilot, deprecations.", UnderTheHoodView.class, Tier.NEW));
    }

    private void section(String heading, Feature... features) {
        var cards = new Div();
        cards.addClassName("card-grid");
        for (Feature feature : features) {
            var card = new Card();
            card.setTitle(new RouterLink(feature.title(), feature.view()));
            card.setSubtitle(feature.summary());
            var badges = new Div();
            badges.addClassName("showcase-badges");
            for (Tier tier : feature.tiers()) {
                badges.add(tier.badge());
            }
            card.add(badges);
            card.addClassName("feature-card");
            cards.add(card);
        }
        var section = new Div(new H2(heading), cards);
        section.addClassName("home-section");
        add(section);
    }
}
