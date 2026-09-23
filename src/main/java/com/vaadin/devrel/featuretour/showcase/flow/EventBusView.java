package com.vaadin.devrel.featuretour.showcase.flow;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.StatTile;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.SessionLockAcquiredEvent;
import com.vaadin.flow.server.SessionLockReleasedEvent;
import com.vaadin.flow.server.SessionLockRequestedEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceEventBus;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.communication.RpcInvocationEndedEvent;
import com.vaadin.flow.server.communication.RpcInvocationStartedEvent;
import com.vaadin.flow.server.data.DataFetchEndedEvent;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.signals.local.ValueSignal;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Route("service-event-bus")
@PageTitle("Service Event Bus")
public class EventBusView extends ShowcasePage {

    record LoggedEvent(String time, String type, String detail) {
    }

    record Stats(long rpcCalls, double avgRpcMs, long lockHolds, double avgLockWaitMs, double avgLockHoldMs,
                 long fetches, long rowsFetched) {
        static final Stats EMPTY = new Stats(0, 0, 0, 0, 0, 0, 0);
    }

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    final ConcurrentLinkedDeque<LoggedEvent> recent = new ConcurrentLinkedDeque<>();
    final ValueSignal<Stats> stats = new ValueSignal<>(Stats.EMPTY);
    final Grid<LoggedEvent> eventGrid = new Grid<>(LoggedEvent.class, false);
    final Grid<Integer> sampleGrid = new Grid<>();

    private final AtomicLong rpcCalls = new AtomicLong();
    private final AtomicLong rpcNanos = new AtomicLong();
    private final AtomicLong lockHolds = new AtomicLong();
    private final AtomicLong lockWaitNanos = new AtomicLong();
    private final AtomicLong lockHoldNanos = new AtomicLong();
    private final AtomicLong fetches = new AtomicLong();
    private final AtomicLong rowsFetched = new AtomicLong();
    private final ThreadLocal<Long> rpcStartedAt = new ThreadLocal<>();
    private final ThreadLocal<Long> lockRequestedAt = new ThreadLocal<>();
    private final ThreadLocal<Long> lockAcquiredAt = new ThreadLocal<>();

    public EventBusView() {
        super("Service Event Bus",
                "VaadinService has an event bus that reports session lock waits and holds, every RPC invocation and every "
                        + "data provider query. It replaces the session lock and RPC listeners and is ideal for monitoring and tracing.",
                "https://vaadin.com/docs/latest/flow/advanced/session-lock-and-rpc-listeners", Tier.NEW, Tier.FREE);

        whenAttached(this::subscribe);

        addDashboard();
        addPlayground();
    }

    private Registration subscribe(UI ui) {
        VaadinSession session = ui.getSession();
        VaadinServiceEventBus bus = VaadinService.getCurrent().getEventBus();
        List<Registration> registrations = new ArrayList<>();

        registrations.add(bus.addListener(RpcInvocationStartedEvent.class, event -> {
            if (event.getUI() == ui) {
                rpcStartedAt.set(System.nanoTime());
            }
        }));
        registrations.add(bus.addListener(RpcInvocationEndedEvent.class, event -> {
            Long start = rpcStartedAt.get();
            rpcStartedAt.remove();
            // Skip the dashboard grid's own bookkeeping calls
            boolean ownGrid = event.getNodeId() == eventGrid.getElement().getNode().getId();
            if (event.getUI() == ui && start != null && !ownGrid) {
                rpcCalls.incrementAndGet();
                rpcNanos.addAndGet(System.nanoTime() - start);
                log("RPC", event.getType() + " · " + event.getName() + " (node " + event.getNodeId() + ")");
            }
        }));
        registrations.add(bus.addListener(SessionLockRequestedEvent.class, event -> {
            if (VaadinSession.getCurrent() == session) {
                lockRequestedAt.set(System.nanoTime());
            }
        }));
        registrations.add(bus.addListener(SessionLockAcquiredEvent.class, event -> {
            Long requested = lockRequestedAt.get();
            if (requested != null) {
                long now = System.nanoTime();
                lockWaitNanos.addAndGet(now - requested);
                lockAcquiredAt.set(now);
                lockRequestedAt.remove();
            }
        }));
        registrations.add(bus.addListener(SessionLockReleasedEvent.class, event -> {
            Long acquired = lockAcquiredAt.get();
            if (acquired != null) {
                lockHolds.incrementAndGet();
                lockHoldNanos.addAndGet(System.nanoTime() - acquired);
                lockAcquiredAt.remove();
            }
        }));
        registrations.add(bus.addListener(DataFetchEndedEvent.class, event -> {
            // Skip the dashboard's own grid, which is refreshed every two seconds
            if (event.getUI() == ui && event.getComponent().filter(c -> c == eventGrid).isEmpty()) {
                fetches.incrementAndGet();
                rowsFetched.addAndGet(event.getRowsReturned());
                String component = event.getComponent().map(c -> c.getClass().getSimpleName()).orElse("?");
                log("Data fetch", component + " · offset " + event.getOffset() + ", limit " + event.getLimit()
                        + " → " + event.getRowsReturned() + " rows" + (event.isFiltered() ? " (filtered)" : ""));
            }
        }));

        // Refresh the dashboard every two seconds through server push
        var refresher = new java.util.Timer("event-bus-dashboard", true);
        refresher.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                ui.access(EventBusView.this::refresh);
            }
        }, 1000, 2000);

        return () -> {
            refresher.cancel();
            registrations.forEach(Registration::remove);
        };
    }

    private void log(String type, String detail) {
        recent.addFirst(new LoggedEvent(LocalTime.now().format(TIME), type, detail));
        while (recent.size() > 40) {
            recent.pollLast();
        }
    }

    void refresh() {
        long calls = rpcCalls.get();
        long holds = lockHolds.get();
        stats.set(new Stats(calls, calls == 0 ? 0 : rpcNanos.get() / 1e6 / calls,
                holds, holds == 0 ? 0 : lockWaitNanos.get() / 1e6 / holds,
                holds == 0 ? 0 : lockHoldNanos.get() / 1e6 / holds,
                fetches.get(), rowsFetched.get()));
        eventGrid.setItems(List.copyOf(recent));
    }

    private void addDashboard() {
        var tiles = new FlexLayout(
                new StatTile("RPC invocations", () -> String.valueOf(stats.get().rpcCalls())),
                new StatTile("Avg. RPC handling", () -> String.format("%.2f ms", stats.get().avgRpcMs())),
                new StatTile("Session lock holds", () -> String.valueOf(stats.get().lockHolds())),
                new StatTile("Avg. lock wait", () -> String.format("%.3f ms", stats.get().avgLockWaitMs())),
                new StatTile("Avg. lock hold", () -> String.format("%.2f ms", stats.get().avgLockHoldMs())),
                new StatTile("Data fetches / rows", () -> stats.get().fetches() + " / " + stats.get().rowsFetched()));
        tiles.addClassName("stat-tiles");

        eventGrid.addColumn(LoggedEvent::time).setHeader("Time").setAutoWidth(true).setFlexGrow(0);
        eventGrid.addColumn(LoggedEvent::type).setHeader("Event").setAutoWidth(true).setFlexGrow(0);
        eventGrid.addColumn(LoggedEvent::detail).setHeader("Details");
        eventGrid.setHeight("18rem");

        addDemo("Live metrics of your own session",
                "This page listens to the bus while it's attached. Lock holds include the dashboard's own push updates.",
                new VerticalLayout(tiles, eventGrid), """
                        VaadinServiceEventBus bus = VaadinService.getCurrent().getEventBus();

                        bus.addListener(RpcInvocationEndedEvent.class, event ->
                                log(event.getType() + " " + event.getName()));

                        bus.addListener(SessionLockAcquiredEvent.class, event -> recordWait());
                        bus.addListener(SessionLockReleasedEvent.class, event -> recordHold());

                        bus.addListener(DataFetchEndedEvent.class, event ->
                                log(event.getComponent() + " fetched " + event.getRowsReturned() + " rows"));

                        // Dispatch is by exact type: listening to AbstractDataFetchEvent never fires
                        """);
    }


    private void addPlayground() {
        var name = new TextField("Type something");
        name.setValueChangeMode(ValueChangeMode.LAZY);
        var click = new Button("Click me");
        var clicks = new java.util.concurrent.atomic.AtomicInteger();
        click.addClickListener(e -> click.setText("Clicked " + clicks.incrementAndGet() + "×"));

        sampleGrid.addColumn(i -> "Row " + i).setHeader("Lazy grid with 10,000 rows");
        sampleGrid.setItems(query -> IntStream.range(query.getOffset(), Math.min(10_000, query.getOffset() + query.getLimit()))
                .boxed());
        sampleGrid.setHeight("14rem");

        addDemo("Generate some events",
                "Click the button, type into the field, or scroll the lazy grid. Each action shows up in the dashboard above.",
                new VerticalLayout(new HorizontalLayout(click, name), sampleGrid), "");
    }
}
