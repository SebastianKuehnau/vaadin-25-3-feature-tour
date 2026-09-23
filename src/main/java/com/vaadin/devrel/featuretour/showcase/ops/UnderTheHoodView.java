package com.vaadin.devrel.featuretour.showcase.ops;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("under-the-hood")
@PageTitle("Under the Hood")
public class UnderTheHoodView extends ShowcasePage {

    public UnderTheHoodView() {
        super("Under the Hood",
                "Changes you don't see in the UI, but in your tooling, your debugger and your upgrade plan.",
                "https://github.com/vaadin/platform/releases/tag/25.3.0", Tier.NEW);

        var engine = card("Client engine in TypeScript", Tier.FREE,
                new Paragraph("The browser-side engine of Flow was ported from GWT to TypeScript: more than 19,000 lines. "
                        + "You get real source maps and readable code in the browser's debugger."),
                new Paragraph("No application changes are needed. Add-ons that use com.vaadin.client.* have to be rewritten."));

        var devLoop = card("Dev Loop CLI", Tier.PREVIEW,
                new Paragraph("A daemon that runs the development loop: background compilation, hot swap or restart, "
                        + "CSS pushed to the browser. Exit codes tell AI agents whether a change is live."),
                code("""
                        mvn vaadin:install-dev-cli
                        .vaadin/vaadin-dev start
                        .vaadin/vaadin-dev apply    # exit code: is the change live?
                        .vaadin/vaadin-dev status"""),
                docs("https://vaadin.com/docs/latest/flow/configuration/live-reload/dev-loop-cli"));

        var sse = card("Server-Sent Events push", Tier.EXPERIMENTAL,
                new Paragraph("A push transport over plain HTTP streaming, for networks where WebSockets are blocked."),
                code("""
                        # src/main/resources/vaadin-featureflags.properties
                        com.vaadin.experimental.ssePushTransport=true

                        @Push(transport = Transport.SERVER_SENT_EVENTS)"""),
                docs("https://vaadin.com/docs/latest/flow/advanced/server-push"));

        var copilot = card("Copilot", Tier.NEW,
                list("Kotlin support in the properties panel",
                        "FormLayout drag and drop with editable responsive steps",
                        "Switch, Breadcrumbs and Dashboard in the UI library",
                        "Aura global theme editor: color schemes, accent colors, reset",
                        "Experimental All Components view, icon picker, Spring Security setup buttons",
                        "Live metrics panel for Observability Kit"));

        var testing = card("Browserless testing", Tier.FREE,
                list("New testers: Switch, Grid Pro, Tree Grid, Grid context menu, Split Layout, Card, Avatar Group",
                        "Grid column elements exposed as TestBench elements",
                        "Fixes for the Select, Time Picker and Date Time Picker testers"),
                docs("https://vaadin.com/docs/latest/flow/testing/browserless"));

        var ai = card("AI modules split by tier", Tier.NEW,
                list("vaadin-ai-core-flow: free, ships in vaadin-core. Orchestrator, providers, interceptor, custom controllers",
                        "vaadin-ai-extensions-flow: commercial, ships in vaadin and vaadin-ee. Grid, Chart and Form controllers",
                        "vaadin-ai-components-flow is deprecated"));

        var deprecations = card("Deprecations", Tier.NEW,
                list("NativeTable → Table",
                        "SessionLockListener, RpcInvocationListener → service event bus",
                        "SSO Kit → plain Spring Security (removal in Vaadin 26)",
                        "Collaboration Kit → shared signals (removal in Vaadin 26)",
                        "AppSec Kit → online service (removal in Vaadin 26)",
                        "156 VaadinIcon constants, Image with child components"),
                docs("https://vaadin.com/docs/latest/upgrading"));

        var versions = card("Versions", Tier.NEW,
                list("Spring Boot 4.1.1, Jackson 3.1.5, Jetty 12.1.13",
                        "TypeScript 7.0, Vite 8.3, React 19.3, React Router 8.4",
                        "Node.js 24.21, pnpm 11.26",
                        "Production builds target Chrome/Edge 152, Firefox 140, Safari 17.6"));

        var grid = new Div(engine, devLoop, sse, copilot, testing, ai, deprecations, versions);
        grid.addClassName("card-grid");
        add(grid);
    }

    private static Card card(String title, Tier tier, Component... content) {
        var card = new Card();
        card.setTitle(title);
        card.setHeaderSuffix(tier.badge());
        card.add(content);
        return card;
    }

    private static Pre code(String text) {
        var pre = new Pre(text);
        pre.addClassName("showcase-code");
        return pre;
    }

    private static UnorderedList list(String... items) {
        var list = new UnorderedList();
        for (String item : items) {
            list.add(new ListItem(item));
        }
        return list;
    }

    private static Div docs(String href) {
        var anchor = new Anchor(href, "Documentation →");
        anchor.setTarget(AnchorTarget.BLANK);
        return new Div(anchor);
    }
}
