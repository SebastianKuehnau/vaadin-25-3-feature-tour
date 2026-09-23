package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;

/**
 * Small nested pages that only exist to demonstrate router-driven breadcrumbs.
 */
abstract class CatalogViews extends VerticalLayout {

    CatalogViews(String title, String text, RouterLink... links) {
        addClassName("showcase-page");
        add(new H1(title), new Paragraph(text));
        add(links);
        add(new Paragraph("Watch the breadcrumb trail in the header, and use it to navigate back up."));
    }
}
