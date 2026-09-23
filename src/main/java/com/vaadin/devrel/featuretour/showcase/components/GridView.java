package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Switch;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridI18n;
import com.vaadin.flow.component.grid.GridMultiSelectionModel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.local.ValueSignal;

import java.util.List;
import java.util.stream.IntStream;

@Route("grid")
@PageTitle("Grid")
public class GridView extends ShowcasePage {

    record Employee(int id, String name, String team) {
    }

    record Node(String name, List<Node> children) {
        Node(String name) {
            this(name, List.of());
        }
    }

    static final List<Employee> EMPLOYEES = IntStream.rangeClosed(1, 200)
            .mapToObj(i -> new Employee(i, "Employee " + i, List.of("Flow", "Hilla", "Design System", "Copilot").get(i % 4)))
            .toList();

    final ValueSignal<Integer> expensiveCalls = new ValueSignal<>(0);
    final Grid<Employee> employees = new Grid<>();
    final Grid.Column<Employee> expensiveColumn;
    final TreeGrid<Node> tree = new TreeGrid<>();

    public GridView() {
        super("Grid",
                "Hidden columns no longer generate or send data, selection checkboxes and sorters get accessible names "
                        + "through GridI18n, and TreeGrid's select all now includes all descendants.",
                "https://vaadin.com/docs/latest/components/grid", Tier.NEW, Tier.FREE);

        employees.addColumn(Employee::name).setHeader("Name").setSortable(true);
        employees.addColumn(Employee::team).setHeader("Team").setSortable(true);
        expensiveColumn = employees.addColumn(this::expensiveScore).setHeader("Risk score (expensive)");
        expensiveColumn.setVisible(false);
        employees.setItems(EMPLOYEES);
        employees.setSelectionMode(Grid.SelectionMode.MULTI);
        employees.setI18n(new GridI18n()
                .setSelectAll("Select all employees")
                .setSelectRow("Select this employee")
                .setSorter("Sort by this column"));
        employees.setHeight("22rem");

        addHiddenColumnDemo();
        addI18nDemo();
        addTreeDemo();
    }

    private String expensiveScore(Employee employee) {
        // Imagine a remote call or a heavy calculation here
        expensiveCalls.set(expensiveCalls.peek() + 1);
        return String.valueOf((employee.id() * 37) % 100);
    }

    private void addHiddenColumnDemo() {
        var show = new Switch("Show the expensive column");
        show.addValueChangeListener(e -> expensiveColumn.setVisible(e.getValue()));
        var counter = new Span();
        counter.addClassName("showcase-status");
        counter.bindText(expensiveCalls.map(calls -> "Value provider calls: " + calls));
        var refresh = new Button("Refresh data", e -> employees.getDataProvider().refreshAll());
        var reset = new Button("Reset counter", e -> expensiveCalls.set(0));

        var controls = new HorizontalLayout(show, refresh, reset, counter);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);
        controls.setWrap(true);

        addDemo("No data for hidden columns",
                "While the risk column is hidden, scroll and refresh: the counter stays put, because Grid skips "
                        + "value providers of hidden columns. Show the column and the calls start.",
                new VerticalLayout(controls, employees), """
                        Grid.Column<Employee> risk = grid.addColumn(this::expensiveScore)
                                .setHeader("Risk score");
                        risk.setVisible(false);  // 25.3: expensiveScore() is not called at all
                        """);
    }

    private void addI18nDemo() {
        addDemo("Accessible names with GridI18n",
                "The grid above uses GridI18n. Turn on a screen reader, or inspect the checkboxes: the select all checkbox, "
                        + "the row checkboxes and the sort buttons are announced with these texts.",
                new Span("See the grid in the section above."), """
                        grid.setI18n(new GridI18n()
                                .setSelectAll("Select all employees")
                                .setSelectRow("Select this employee")
                                .setSorter("Sort by this column"));
                        """);
    }

    private void addTreeDemo() {
        var root = new Node("Vaadin", List.of(
                new Node("Engineering", List.of(new Node("Flow"), new Node("Hilla"), new Node("Design System"))),
                new Node("Product", List.of(new Node("Copilot"), new Node("Docs"))),
                new Node("Sales", List.of(new Node("EMEA"), new Node("Americas")))));
        tree.setItems(List.of(root), Node::children);
        tree.addHierarchyColumn(Node::name).setHeader("Organization");
        tree.setSelectionMode(Grid.SelectionMode.MULTI);
        tree.setAllRowsVisible(true);

        var selected = new Span();
        selected.addClassName("showcase-status");
        tree.addSelectionListener(e -> selected.setText(e.getAllSelectedItems().size() + " of 11 nodes selected"));

        var selectAll = new Button("selectAll()", e -> ((GridMultiSelectionModel<Node>) tree.getSelectionModel()).selectAll());
        var clear = new Button("Deselect all", e -> tree.deselectAll());
        var controls = new HorizontalLayout(selectAll, clear, selected);
        controls.setAlignItems(FlexComponent.Alignment.CENTER);

        addDemo("TreeGrid: select all includes descendants",
                "The tree starts collapsed. selectAll() now selects every node, including children that were never expanded or loaded.",
                new VerticalLayout(controls, tree), """
                        treeGrid.setSelectionMode(Grid.SelectionMode.MULTI);
                        ((GridMultiSelectionModel<Node>) treeGrid.getSelectionModel()).selectAll();
                        // 25.3: selects all 11 nodes, not only the root
                        """);
    }
}
