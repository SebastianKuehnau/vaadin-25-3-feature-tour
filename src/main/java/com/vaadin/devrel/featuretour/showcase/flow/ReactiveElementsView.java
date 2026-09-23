package com.vaadin.devrel.featuretour.showcase.flow;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.Size;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.signals.Signal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Route("reactive-elements")
@PageTitle("Size Signal & whenAttached")
public class ReactiveElementsView extends ShowcasePage {

    private static final ScheduledExecutorService TICKER = Executors.newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "showcase-clock");
        thread.setDaemon(true);
        return thread;
    });

    final Div lifecycleLog = new Div();

    public ReactiveElementsView() {
        super("Size Signal & whenAttached",
                "Element.sizeSignal() exposes an element's rendered size as a signal, so no custom ResizeObserver is needed. "
                        + "Component.whenAttached() runs setup on attach and hands back the cleanup for detach.",
                "https://vaadin.com/docs/latest/flow/ui-state/building-ui", Tier.NEW, Tier.FREE);

        addSizeSignalDemo();
        addWhenAttachedDemo();
    }

    private void addSizeSignalDemo() {
        var panel = new Div();
        panel.addClassName("size-panel");
        Signal<Size> size = panel.getElement().sizeSignal();

        var dimensions = new Span();
        dimensions.addClassName("size-dimensions");
        dimensions.bindText(size.map(s -> s.width() + " × " + s.height() + " px"));
        var layout = new Span();
        layout.addClassName("size-layout");
        layout.bindText(size.map(s -> s.width() < 300 ? "compact layout" : s.width() < 500 ? "medium layout" : "wide layout"));
        panel.add(dimensions, layout);
        panel.bindClassName("compact", size.map(s -> s.width() < 300));
        panel.bindClassName("wide", size.map(s -> s.width() >= 500));

        var split = new SplitLayout(panel, new Div(new Span("Drag the splitter ←→")));
        split.setSplitterPosition(60);
        split.addClassName("size-split");

        addDemo("Element.sizeSignal()",
                "Drag the splitter. The panel reacts to its own width, not the window's, so it also adapts when the "
                        + "surrounding layout changes.",
                split, """
                        Signal<Size> size = panel.getElement().sizeSignal();

                        dimensions.bindText(size.map(s -> s.width() + " × " + s.height() + " px"));
                        panel.bindClassName("compact", size.map(s -> s.width() < 300));
                        panel.bindClassName("wide", size.map(s -> s.width() >= 500));
                        """);
    }

    private void addWhenAttachedDemo() {
        var clock = new Span();
        clock.addClassName("showcase-clock");
        clock.whenAttached(ui -> {
            log("attached: clock started");
            var task = TICKER.scheduleAtFixedRate(() -> ui.access(() -> clock.setText(
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")))), 0, 1, TimeUnit.SECONDS);
            return () -> {
                task.cancel(false);
                log("detached: clock stopped");
            };
        });

        var holder = new Div(clock);
        holder.addClassName("clock-holder");
        var toggle = new Button("Detach clock");
        toggle.addClickListener(e -> {
            if (clock.isAttached()) {
                holder.remove(clock);
                toggle.setText("Attach clock");
            } else {
                holder.add(clock);
                toggle.setText("Detach clock");
            }
        });
        lifecycleLog.addClassName("lifecycle-log");

        addDemo("Component.whenAttached()",
                "The clock starts a timer when it's attached and stops it on detach. Setup and cleanup live in one place, "
                        + "and the returned Registration can also cancel the hook.",
                new VerticalLayout(new HorizontalLayout(toggle, holder), lifecycleLog), """
                        clock.whenAttached(ui -> {
                            var task = scheduler.scheduleAtFixedRate(() -> ui.access(() ->
                                    clock.setText(LocalTime.now().toString())), 0, 1, SECONDS);
                            return () -> task.cancel(false);   // runs on detach
                        });
                        """);
    }

    private void log(String text) {
        lifecycleLog.addComponentAsFirst(new Div(new Span(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "  " + text)));
    }
}
