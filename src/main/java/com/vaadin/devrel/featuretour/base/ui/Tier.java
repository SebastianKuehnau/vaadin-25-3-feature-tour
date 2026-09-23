package com.vaadin.devrel.featuretour.base.ui;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;

/**
 * How a showcased feature is licensed or how stable it is.
 */
public enum Tier {
    NEW("New in 25.3", BadgeVariant.FILLED),
    FREE("Free", BadgeVariant.SUCCESS),
    COMMERCIAL("Commercial", BadgeVariant.WARNING),
    PREVIEW("Preview", BadgeVariant.CONTRAST),
    EXPERIMENTAL("Experimental", BadgeVariant.ERROR);

    private final String label;
    private final BadgeVariant variant;

    Tier(String label, BadgeVariant variant) {
        this.label = label;
        this.variant = variant;
    }

    public Badge badge() {
        var badge = new Badge(label);
        badge.addThemeVariants(variant, BadgeVariant.SMALL);
        return badge;
    }
}
