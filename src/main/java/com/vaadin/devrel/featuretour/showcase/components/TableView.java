package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Table;
import com.vaadin.flow.component.html.TableColumn;
import com.vaadin.flow.component.html.TableRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ListSignal;

import java.util.List;

@Route("table")
@PageTitle("Table")
public class TableView extends ShowcasePage {

    record Planet(String name, String mass, String diameter) {
    }

    private static final List<Planet> ALL_PLANETS = List.of(
            new Planet("Mercury", "0.330", "4,879"),
            new Planet("Venus", "4.87", "12,104"),
            new Planet("Earth", "5.97", "12,756"),
            new Planet("Mars", "0.642", "6,792"),
            new Planet("Jupiter", "1,898", "142,984"),
            new Planet("Saturn", "568", "120,536"),
            new Planet("Uranus", "86.8", "51,118"),
            new Planet("Neptune", "102", "49,528"));

    final ListSignal<Planet> planets = new ListSignal<>();
    final Table planetTable = new Table();

    public TableView() {
        super("Table",
                "A new component family for semantic HTML tables in Java: caption, column groups, "
                        + "head, bodies and foot, with typed factories and signal-bound rows. It replaces the deprecated NativeTable.",
                "https://vaadin.com/docs/latest/components/html-elements/table", Tier.NEW, Tier.FREE);

        addInvoiceDemo();
        addSignalDemo();
        addSpanningDemo();
        addNote("Table suits small data that is part of the page. For lazy loading, sorting and editing, Grid is still the right choice.");
    }

    private void addInvoiceDemo() {
        var invoice = new Table();
        invoice.addClassName("showcase-table");
        invoice.setCaptionText("Invoice 2026-114");
        invoice.addColumnGroup(new TableColumn(), new TableColumn(2)).addClassName("numbers");
        invoice.addHeaderRow("Item", "Hours", "Amount (€)");
        invoice.addRowWithHeader("Consulting", "12", "1,440.00");
        invoice.addRowWithHeader("Code review", "4", "480.00");
        invoice.addRowWithHeader("Workshop: Vaadin 25.3", "8", "1,200.00");
        var total = invoice.addFooterRow();
        total.addRowHeaderCell("Total");
        total.addDataCells("24", "3,120.00");

        addDemo("Captions, sections and row headers",
                "Typed factories create the right cell types with the right scope attributes, so screen readers can announce "
                        + "\"Consulting, Amount, 1,440.00\" instead of a bare number.",
                invoice, """
                        Table table = new Table();
                        table.setCaptionText("Invoice 2026-114");
                        table.addHeaderRow("Item", "Hours", "Amount (€)");
                        table.addRowWithHeader("Consulting", "12", "1,440.00");
                        table.addRowWithHeader("Code review", "4", "480.00");

                        TableRow total = table.addFooterRow();
                        total.addRowHeaderCell("Total");
                        total.addDataCells("24", "3,120.00");
                        """);
    }

    private void addSignalDemo() {
        planets.insertAllLast(ALL_PLANETS.subList(0, 2));

        planetTable.addClassName("showcase-table");
        planetTable.getCaption().bindText(planets.map(list -> "Planets of our solar system (" + list.size() + " of 8)"));
        planetTable.addHeaderRow("Name", "Mass (10²⁴ kg)", "Diameter (km)", "");
        planetTable.getBody().bindChildren(planets, planetSignal -> {
            Planet planet = planetSignal.peek();
            TableRow row = new TableRow();
            row.addRowHeaderCell(planet.name());
            row.addDataCells(planet.mass(), planet.diameter());
            var remove = new Button(VaadinIcon.TRASH.create(), e -> planets.remove(planetSignal));
            remove.addThemeVariants(ButtonVariant.TERTIARY, ButtonVariant.SMALL);
            remove.setAriaLabel("Remove " + planet.name());
            row.addDataCell(remove);
            return row;
        });

        var add = new Button("Add next planet", VaadinIcon.PLUS.create(), e -> ALL_PLANETS.stream()
                .filter(planet -> planets.peekValues().noneMatch(planet::equals))
                .findFirst()
                .ifPresent(planets::insertLast));
        add.bindEnabled(planets.map(list -> list.size() < 8));
        var reset = new Button("Reset", e -> {
            planets.clear();
            planets.insertAllLast(ALL_PLANETS.subList(0, 2));
        });

        addDemo("Rows bound to a signal",
                "TableBody.bindChildren() keeps the rows in sync with a ListSignal. Add and remove planets: "
                        + "only the affected rows change, and the caption follows the list size.",
                new VerticalLayout(new HorizontalLayout(add, reset), planetTable), """
                        ListSignal<Planet> planets = new ListSignal<>();

                        table.getCaption().bindText(planets.map(list ->
                                "Planets of our solar system (" + list.size() + " of 8)"));
                        table.addHeaderRow("Name", "Mass (10²⁴ kg)", "Diameter (km)", "");
                        table.getBody().bindChildren(planets, planetSignal -> {
                            Planet planet = planetSignal.peek();
                            TableRow row = new TableRow();
                            row.addRowHeaderCell(planet.name());
                            row.addDataCells(planet.mass(), planet.diameter());
                            row.addDataCell(new Button(VaadinIcon.TRASH.create(),
                                    e -> planets.remove(planetSignal)));
                            return row;
                        });
                        """);
    }

    private void addSpanningDemo() {
        var plans = new Table();
        plans.addClassName("showcase-table");
        plans.setCaptionText("Vaadin subscriptions (illustrative)");
        var groupRow = plans.addHeaderRow();
        groupRow.addDataCell("").setColspan(2);
        groupRow.addColumnGroupHeaderCell("Free", 1);
        groupRow.addColumnGroupHeaderCell("Commercial", 2);
        plans.addHeaderRow().addColumnHeaderCells("Area", "Feature", "Core", "Prime", "Ultimate");

        var components = plans.addRow();
        components.addRowGroupHeaderCell("UI", 2);
        components.addRowHeaderCell("Core components");
        components.addDataCells("✓", "✓", "✓");
        var commercialUi = plans.addRow();
        commercialUi.addRowHeaderCell("Charts, Grid Pro, Dashboard");
        commercialUi.addDataCells("–", "✓", "✓");

        var ai = plans.addRow();
        ai.addRowGroupHeaderCell("AI", 2);
        ai.addRowHeaderCell("Orchestrator & custom controllers");
        ai.addDataCells("✓", "✓", "✓");
        var commercialAi = plans.addRow();
        commercialAi.addRowHeaderCell("Form, Grid & Chart AI controllers");
        commercialAi.addDataCells("–", "✓", "✓");

        addDemo("Spanning cells",
                "Header cells for column and row groups take a colspan or rowspan and get scope=\"colgroup\" or scope=\"rowgroup\".",
                plans, """
                        TableRow groups = table.addHeaderRow();
                        groups.addDataCell("").setColspan(2);
                        groups.addColumnGroupHeaderCell("Free", 1);
                        groups.addColumnGroupHeaderCell("Commercial", 2);

                        TableRow ui = table.addRow();
                        ui.addRowGroupHeaderCell("UI", 2);   // spans two rows
                        ui.addRowHeaderCell("Core components");
                        ui.addDataCells("✓", "✓", "✓");
                        """);
    }
}
