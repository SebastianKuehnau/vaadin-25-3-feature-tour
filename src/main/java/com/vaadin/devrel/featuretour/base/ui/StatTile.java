package com.vaadin.devrel.featuretour.base.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.function.SerializableSupplier;

/**
 * A small number tile whose value is computed from signals.
 */
public final class StatTile extends Div {

    public StatTile(String label, SerializableSupplier<String> value) {
        var number = new Span();
        number.addClassName("stat-value");
        number.bindText(value::get);
        var caption = new Span(label);
        caption.addClassName("stat-label");
        add(number, caption);
        addClassName("stat-tile");
    }
}
