package com.vaadin.devrel.featuretour.base.ui;

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
import com.vaadin.devrel.featuretour.showcase.home.HomeView;
import com.vaadin.devrel.featuretour.showcase.ops.ObservabilityView;
import com.vaadin.devrel.featuretour.showcase.ops.UnderTheHoodView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.breadcrumbs.Breadcrumbs;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.ScrollerVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;

@Layout
public final class MainLayout extends AppLayout {

    MainLayout() {
        setPrimarySection(Section.DRAWER);
        addToDrawer(createApplicationHeader(), createApplicationDrawer(), createApplicationFooter());

        // New in 25.3 without a feature flag: the trail is built from the route hierarchy
        var breadcrumbs = new Breadcrumbs();
        breadcrumbs.addClassName("app-breadcrumbs");
        addToNavbar(new DrawerToggle(), breadcrumbs);
    }

    private Component createApplicationHeader() {
        var logo = new Span("25.3");
        logo.addClassName("app-logo");

        var appName = new Span("Vaadin 25.3 Feature Tour");
        appName.addClassName("app-name");
        var appByline = new Span("A Vaadin DevRel demo");
        appByline.addClassName("app-byline");
        var title = new Div(appName, appByline);
        title.addClassName("app-title");

        var header = new HorizontalLayout(logo, title);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        header.addClassName("app-header");
        return header;
    }

    private Component createApplicationDrawer() {
        var home = new SideNav();
        home.addItem(new SideNavItem("Overview", HomeView.class, VaadinIcon.HOME.create()));

        var components = nav("New & improved components",
                item("Switch", SwitchView.class, VaadinIcon.SLIDERS),
                item("Table", TableView.class, VaadinIcon.TABLE),
                item("Breadcrumbs", BreadcrumbsView.class, VaadinIcon.ELLIPSIS_DOTS_H),
                item("Date Picker", DatePickerView.class, VaadinIcon.CALENDAR),
                item("Combo Box", ComboBoxView.class, VaadinIcon.COMBOBOX),
                item("Message List", MessageListView.class, VaadinIcon.CHAT),
                item("Upload Validation", UploadView.class, VaadinIcon.UPLOAD),
                item("Grid", GridView.class, VaadinIcon.GRID_BIG),
                item("Accessibility", AccessibilityView.class, VaadinIcon.EYE));

        var flow = nav("Flow framework",
                item("Validation Groups", BinderGroupsView.class, VaadinIcon.CHECK_SQUARE_O),
                item("Size Signal & whenAttached", ReactiveElementsView.class, VaadinIcon.EXPAND_SQUARE),
                item("Service Event Bus", EventBusView.class, VaadinIcon.BOLT));

        var ai = nav("AI",
                item("AI Form Filler", FormFillerView.class, VaadinIcon.MAGIC),
                item("AI Data Explorer", DataExplorerView.class, VaadinIcon.CHART),
                item("AI Assistant", AssistantView.class, VaadinIcon.COMMENTS));

        var ops = nav("Operations & tooling",
                item("Observability Kit 5", ObservabilityView.class, VaadinIcon.DASHBOARD),
                item("Under the Hood", UnderTheHoodView.class, VaadinIcon.COGS));

        var navs = new VerticalLayout(home, components, flow, ai, ops);
        navs.setPadding(false);
        navs.setSpacing(true);
        navs.addClassName("app-nav");
        var scroller = new Scroller(navs);
        scroller.addThemeVariants(ScrollerVariant.OVERFLOW_INDICATORS);
        return scroller;
    }

    private static SideNav nav(String label, SideNavItem... items) {
        var nav = new SideNav(label);
        nav.setCollapsible(true);
        nav.addItem(items);
        return nav;
    }

    private static SideNavItem item(String label, Class<? extends Component> view, VaadinIcon icon) {
        return new SideNavItem(label, view, icon.create());
    }

    private Component createApplicationFooter() {
        var source = new Anchor("https://github.com/vaadin/platform/releases/tag/25.3.0", "Release notes");
        source.setTarget(AnchorTarget.BLANK);
        var footer = new Div(new Span("Built by Vaadin DevRel with Vaadin 25.3 and ❤️"), source);
        footer.addClassName("app-footer");
        return footer;
    }
}
