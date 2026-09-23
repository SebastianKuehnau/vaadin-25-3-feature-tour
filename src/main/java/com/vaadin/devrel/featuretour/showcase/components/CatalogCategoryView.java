package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("breadcrumbs/catalog/laptops")
@PageTitle("Laptops")
public class CatalogCategoryView extends CatalogViews {

    public CatalogCategoryView() {
        super("Laptops", "A category page, one level below the catalog.",
                new RouterLink("Vaadin Book 16", CatalogProductView.class));
    }
}
