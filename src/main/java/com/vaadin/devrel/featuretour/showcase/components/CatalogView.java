package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

@Route("breadcrumbs/catalog")
@PageTitle("Catalog")
public class CatalogView extends CatalogViews {

    public CatalogView() {
        super("Catalog", "The top level of a fictional product catalog.",
                new RouterLink("Laptops", CatalogCategoryView.class));
    }
}
