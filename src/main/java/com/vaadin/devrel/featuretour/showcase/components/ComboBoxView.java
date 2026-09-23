package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.PartialMatchMode;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route("combo-box")
@PageTitle("Combo Box")
public class ComboBoxView extends ShowcasePage {

    static final List<String> COUNTRIES = List.of("Austria", "Australia", "Belgium", "Brazil", "Canada",
            "Denmark", "Estonia", "Finland", "France", "Germany", "Greece", "Iceland", "India", "Ireland",
            "Italy", "Japan", "Netherlands", "Norway", "Poland", "Portugal", "Spain", "Sweden",
            "Switzerland", "United Kingdom", "United States");

    public ComboBoxView() {
        super("Combo Box",
                "A partial match mode controls what happens when the user types a partial value and leaves the field.",
                "https://vaadin.com/docs/latest/components/combo-box", Tier.NEW, Tier.FREE);

        var none = comboBox("Default (NONE)", PartialMatchMode.NONE,
                "Typed text that isn't an item is discarded.");
        var first = comboBox("FIRST_MATCH", PartialMatchMode.FIRST_MATCH,
                "Type \"au\" and press Tab: the first match, Austria, is selected.");
        var only = comboBox("ONLY_MATCH", PartialMatchMode.ONLY_MATCH,
                "Type \"au\": two matches, nothing is selected. Type \"fin\": Finland is the only match.");

        var row = new FlexLayout(none, first, only);
        row.addClassName("showcase-row");
        addDemo("Partial match modes",
                "Try each field: type a few letters, then press Tab or Enter without picking from the list.",
                row, """
                        ComboBox<String> country = new ComboBox<>("Country", countries);

                        // Select the first item that matches the typed text
                        country.setPartialMatchMode(PartialMatchMode.FIRST_MATCH);

                        // Select only when exactly one item matches
                        country.setPartialMatchMode(PartialMatchMode.ONLY_MATCH);
                        """);
    }

    private ComboBox<String> comboBox(String label, PartialMatchMode mode, String helper) {
        var comboBox = new ComboBox<>(label, COUNTRIES);
        comboBox.setPartialMatchMode(mode);
        comboBox.setHelperText(helper);
        comboBox.setWidth("18rem");
        return comboBox;
    }
}
