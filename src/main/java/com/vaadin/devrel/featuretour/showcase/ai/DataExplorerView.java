package com.vaadin.devrel.featuretour.showcase.ai;

import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.ai.chart.ChartAIController;
import com.vaadin.flow.component.ai.grid.AIDataRow;
import com.vaadin.flow.component.ai.grid.GridAIController;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.ai.orchestrator.AIOrchestrator;
import com.vaadin.flow.component.ai.orchestrator.ResponseListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Route("ai-data-explorer")
@PageTitle("AI Data Explorer")
public class DataExplorerView extends AiPage {

    static final String SYSTEM_PROMPT = """
            You help sales managers explore their sales data. Use readable column aliases such as "Revenue (EUR)".
            Round money to whole euros.
            """;

    final Grid<AIDataRow> grid = new Grid<>();
    final Chart chart = new Chart();

    public DataExplorerView(AiSupport ai, SalesDatabase database) {
        super(ai, "AI Data Explorer",
                "GridAIController and ChartAIController turn natural-language questions into SQL. The LLM sees the schema, "
                        + "never the rows: results go straight into the Grid or Chart.",
                "https://vaadin.com/docs/latest/flow/ai-support/ai-powered-grid",
                Tier.COMMERCIAL, Tier.PREVIEW);

        grid.setHeight("22rem");
        grid.setEmptyStateText("Ask a question to fill the grid.");
        chart.setHeight("24rem");
        chart.addClassName("ai-chart");

        var gridInput = new MessageInput();
        gridInput.setWidthFull();
        var chartInput = new MessageInput();
        chartInput.setWidthFull();

        var gridStatus = new Span();
        gridStatus.addClassName("showcase-status");
        var chartStatus = new Span();
        chartStatus.addClassName("showcase-status");

        AIOrchestrator gridOrchestrator = null;
        AIOrchestrator chartOrchestrator = null;
        if (ai.isConfigured()) {
            UI ui = UI.getCurrentOrThrow();
            // One orchestrator, provider and input per controller
            gridOrchestrator = AIOrchestrator.builder(ai.newProvider(), SYSTEM_PROMPT)
                    .withInput(gridInput)
                    .withController(new GridAIController(grid, new JdbcDatabaseProvider(database.readOnlyDataSource())))
                    .withRequestListener(event -> gridStatus.setText("⏳ Thinking…"))
                    .withResponseListener(event -> ui.access(() -> gridStatus.setText(describe(event))))
                    .build();
            chartOrchestrator = AIOrchestrator.builder(ai.newProvider(), SYSTEM_PROMPT)
                    .withInput(chartInput)
                    .withController(new ChartAIController(chart, new JdbcDatabaseProvider(database.readOnlyDataSource())))
                    .withRequestListener(event -> chartStatus.setText("⏳ Thinking…"))
                    .withResponseListener(event -> ui.access(() -> chartStatus.setText(describe(event))))
                    .build();
        } else {
            gridInput.setEnabled(false);
            chartInput.setEnabled(false);
        }

        addDemo("Ask the grid",
                "The grid gets typed columns, right-aligned numbers and lazy loading through LIMIT and OFFSET.",
                new VerticalLayout(examples(gridOrchestrator,
                        "Top 10 countries by revenue in 2026",
                        "Revenue per product and quarter, grouped under Product",
                        "All training deals in Finland with more than 5 units"), gridInput, gridStatus, grid), """
                        Grid<AIDataRow> grid = new Grid<>();
                        DatabaseProvider db = new JdbcDatabaseProvider(readOnlyDataSource);

                        AIOrchestrator.builder(new SpringAILLMProvider(chatModel), systemPrompt)
                                .withInput(messageInput)
                                .withController(new GridAIController(grid, db))
                                .build();
                        """);

        addDemo("Ask the chart",
                "The chart controller writes the SQL for each series and the Highcharts configuration, and applies both "
                        + "at the end of the turn.",
                new VerticalLayout(examples(chartOrchestrator,
                        "Monthly revenue as a column chart",
                        "Revenue by region as a pie chart",
                        "Compare quarterly revenue of 2025 and 2026 per category as a stacked bar chart"), chartInput, chartStatus, chart),
                """
                        Chart chart = new Chart();
                        AIOrchestrator.builder(new SpringAILLMProvider(chatModel), systemPrompt)
                                .withInput(messageInput)
                                .withController(new ChartAIController(chart, db))
                                .build();
                        """);

        addDemo("Letting the LLM fix its own SQL",
                "New in 25.3: when a query fails, the DatabaseProvider throws a ToolException. Its message is relayed to the "
                        + "model, which corrects the query in the same turn. Any other exception reaches the model only as a "
                        + "generic error, so internal details don't leak.",
                new Span("Try \"Revenue per sales rep\" in the grid: there is no such column. The model reads the error, "
                        + "looks at the schema again and explains in the status line what it can show instead."), """
                        @Override
                        public List<Map<String, Object>> executeQuery(String sql) {
                            try (var connection = readOnlyDataSource.getConnection(); ...) {
                                ...
                            } catch (SQLException e) {
                                throw new ToolException("Query failed: " + e.getMessage(), e);
                            }
                        }
                        """);
    }

    private static String describe(ResponseListener.ResponseEvent event) {
        if (event.getError().isPresent()) {
            return "⚠️ The request failed: " + event.getError().get().getMessage();
        }
        String text = event.getResponse().strip();
        return text.isEmpty() ? "✅ Done" : "💬 " + text;
    }

    private HorizontalLayout examples(@Nullable AIOrchestrator orchestrator, String... prompts) {
        var row = new HorizontalLayout();
        row.setWrap(true);
        for (String prompt : List.of(prompts)) {
            var button = new Button(prompt, e -> Objects.requireNonNull(orchestrator).prompt(prompt));
            button.addThemeVariants(ButtonVariant.SMALL);
            button.setEnabled(orchestrator != null);
            row.add(button);
        }
        return row;
    }
}
