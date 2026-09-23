package com.vaadin.devrel.featuretour.showcase.ops;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Measurement;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Route("observability")
@PageTitle("Observability Kit 5")
public class ObservabilityView extends ShowcasePage {

    record MeterRow(String name, String type, String tags, String value) {
    }

    private final transient MeterRegistry registry;
    final Grid<MeterRow> meters = new Grid<>();
    final TextField filter = new TextField();

    public ObservabilityView(MeterRegistry registry) {
        super("Observability Kit 5",
                "Rebuilt without the OpenTelemetry Java agent: add one dependency and get Vaadin-specific metrics through the "
                        + "Micrometer Observation API: sessions, UIs, RPC, session lock, navigation, errors, data provider queries and more.",
                "https://vaadin.com/docs/latest/tools/observability", Tier.NEW, Tier.COMMERCIAL);
        this.registry = registry;

        addDemo("One dependency, no -javaagent",
                "This app runs Observability Kit 5 with Spring Boot Actuator. No agent, no extra JVM flags.",
                new VerticalLayout(
                        link("/actuator/prometheus", "Prometheus scrape endpoint"),
                        link("/actuator/vaadin/observability", "Vaadin insights endpoint: slow and failed interactions")),
                """
                        <dependency>
                            <groupId>com.vaadin</groupId>
                            <artifactId>observability-kit-starter</artifactId>
                        </dependency>
                        <dependency>
                            <groupId>org.springframework.boot</groupId>
                            <artifactId>spring-boot-starter-actuator</artifactId>
                        </dependency>

                        # application.properties
                        management.endpoints.web.exposure.include=health,prometheus,vaadin
                        """);

        meters.addColumn(MeterRow::name).setHeader("Meter").setAutoWidth(true).setSortable(true);
        meters.addColumn(MeterRow::type).setHeader("Type").setAutoWidth(true).setFlexGrow(0);
        meters.addColumn(MeterRow::value).setHeader("Value").setAutoWidth(true).setFlexGrow(0);
        meters.addColumn(MeterRow::tags).setHeader("Tags");
        meters.setHeight("26rem");
        filter.setPlaceholder("Filter meters, e.g. rpc or navigation");
        filter.setValueChangeMode(ValueChangeMode.LAZY);
        filter.setClearButtonVisible(true);
        filter.setWidth("20rem");
        filter.addValueChangeListener(e -> refresh());
        var refresh = new Button("Refresh", e -> refresh());
        refresh();

        addDemo("Live Vaadin meters of this app",
                "Every vaadin.* meter currently in the Micrometer registry. Click around the showcase, come back and refresh.",
                new VerticalLayout(new HorizontalLayout(filter, refresh), meters), """
                        registry.getMeters().stream()
                                .filter(meter -> meter.getId().getName().startsWith("vaadin."))
                                ...
                        """);

        addNote("Observability Kit 5 needs a commercial subscription. In development, a license key is requested in the "
                + "browser the first time it runs.");
    }

    private static Anchor link(String href, String text) {
        var anchor = new Anchor(href, text + " (" + href + ")");
        anchor.setTarget(AnchorTarget.BLANK);
        anchor.setRouterIgnore(true);
        return anchor;
    }

    void refresh() {
        String term = filter.getValue().toLowerCase(Locale.ROOT);
        List<MeterRow> rows = registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("vaadin."))
                .filter(meter -> term.isBlank() || meter.getId().getName().contains(term))
                .map(ObservabilityView::toRow)
                .sorted(Comparator.comparing(MeterRow::name))
                .toList();
        meters.setItems(rows);
    }

    private static MeterRow toRow(Meter meter) {
        String tags = meter.getId().getTags().stream()
                .map(tag -> tag.getKey() + "=" + tag.getValue())
                .collect(Collectors.joining(", "));
        String value = StreamSupport.stream(meter.measure().spliterator(), false)
                .map(ObservabilityView::format)
                .collect(Collectors.joining(" · "));
        return new MeterRow(meter.getId().getName(), meter.getId().getType().name().toLowerCase(Locale.ROOT), tags, value);
    }

    private static String format(Measurement measurement) {
        double value = measurement.getValue();
        String number = value == Math.rint(value) ? String.valueOf((long) value) : String.format(Locale.ROOT, "%.3f", value);
        return measurement.getStatistic().name().toLowerCase(Locale.ROOT) + " " + number;
    }
}
