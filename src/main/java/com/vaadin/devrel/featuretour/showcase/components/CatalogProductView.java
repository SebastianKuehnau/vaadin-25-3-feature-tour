package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("breadcrumbs/catalog/laptops/book-16")
@PageTitle("Vaadin Book 16")
public class CatalogProductView extends CatalogViews {

    public CatalogProductView() {
        super("Vaadin Book 16", "A product page, the deepest level of the hierarchy.",
                new RouterLink("Back to the Breadcrumbs demo", BreadcrumbsView.class));
    }
}
