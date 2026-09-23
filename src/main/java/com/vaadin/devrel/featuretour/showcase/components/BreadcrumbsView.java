package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsItem;
import com.vaadin.flow.component.breadcrumbs.BreadcrumbsVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("breadcrumbs")
@PageTitle("Breadcrumbs")
public class BreadcrumbsView extends ShowcasePage {

    public BreadcrumbsView() {
        super("Breadcrumbs",
                "A navigation trail that shows where the user is in the page hierarchy. "
                        + "No longer behind a feature flag in 25.3.",
                "https://vaadin.com/docs/latest/components/breadcrumbs", Tier.NEW, Tier.FREE);

        var deepLink = new RouterLink("Open Catalog → Laptops → Vaadin Book 16", CatalogProductView.class);
        addDemo("Built automatically from your routes",
                "Look at the header of this app: it holds a single Breadcrumbs component in router mode. "
                        + "It follows the route hierarchy, which Vaadin derives from the URL paths, and uses each route's page title. "
                        + "Follow the link to a nested route and watch the trail in the header grow.",
                new VerticalLayout(deepLink), """
                        // In MainLayout: one component, no configuration
                        addToNavbar(new DrawerToggle(), new Breadcrumbs());

                        // Routes build the hierarchy through their URL paths
                        @Route("breadcrumbs/catalog")               @PageTitle("Catalog")
                        @Route("breadcrumbs/catalog/laptops")       @PageTitle("Laptops")
                        @Route("breadcrumbs/catalog/laptops/book-16") @PageTitle("Vaadin Book 16")
                        """);

        var manual = new Breadcrumbs(Breadcrumbs.Mode.MANUAL);
        var home = new BreadcrumbsItem("Home", "");
        home.setPrefixComponent(VaadinIcon.HOME.create());
        manual.add(home,
                new BreadcrumbsItem("Documents", "breadcrumbs"),
                new BreadcrumbsItem("2026", "breadcrumbs"),
                new BreadcrumbsItem("Q3 report.pdf"));

        var slash = new Breadcrumbs(Breadcrumbs.Mode.MANUAL);
        slash.addThemeVariants(BreadcrumbsVariant.SLASH, BreadcrumbsVariant.AURA_ACCENT);
        slash.add(new BreadcrumbsItem("vaadin", "breadcrumbs"),
                new BreadcrumbsItem("platform", "breadcrumbs"),
                new BreadcrumbsItem("releases", "breadcrumbs"),
                new BreadcrumbsItem("25.3.0"));

        addDemo("Defined manually",
                "In manual mode you add the items yourself, for example for a file browser. "
                        + "Items support prefix icons, and the last item without a path is the current page.",
                new VerticalLayout(manual, slash), """
                        var breadcrumbs = new Breadcrumbs(Breadcrumbs.Mode.MANUAL);
                        var home = new BreadcrumbsItem("Home", "");
                        home.setPrefixComponent(VaadinIcon.HOME.create());
                        breadcrumbs.add(home,
                                new BreadcrumbsItem("Documents", "documents"),
                                new BreadcrumbsItem("2026", "documents/2026"),
                                new BreadcrumbsItem("Q3 report.pdf"));

                        slash.addThemeVariants(BreadcrumbsVariant.SLASH, BreadcrumbsVariant.AURA_ACCENT);
                        """);

        addNote("Tip: when a route's logical parent is not its URL parent, declare it with @RouteParent.");
    }
}
