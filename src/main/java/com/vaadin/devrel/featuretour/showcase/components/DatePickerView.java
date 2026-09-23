package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.datepicker.DateMetadata;
import com.vaadin.flow.component.datepicker.DateMetadataProvider;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Route("date-picker")
@PageTitle("Date Picker")
public class DatePickerView extends ShowcasePage {

    /** Public holidays that the pickers mark and disable (a small, Finnish-flavoured sample). */
    static final Map<MonthDay, String> HOLIDAYS = Map.of(
            MonthDay.of(1, 1), "New Year's Day",
            MonthDay.of(5, 1), "May Day",
            MonthDay.of(12, 6), "Independence Day",
            MonthDay.of(12, 24), "Christmas Eve",
            MonthDay.of(12, 25), "Christmas Day",
            MonthDay.of(12, 26), "Boxing Day");

    final DatePicker delivery = new DatePicker("Delivery date");
    final DatePicker booking = new DatePicker("Book a meeting room");

    public DatePickerView() {
        super("Date Picker",
                "Disable individual dates and weekdays, attach metadata such as CSS part names to any date, "
                        + "and let Date Time Picker fill in a sensible default time.",
                "https://vaadin.com/docs/latest/components/date-picker", Tier.NEW, Tier.FREE);

        addDisabledDemo();
        addMetadataDemo();
        addDefaultTimeDemo();
    }

    private void addDisabledDemo() {
        LocalDate today = LocalDate.now();
        delivery.setMin(today);
        delivery.setDisabledWeekdays(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        delivery.setDisabledDates(Stream.iterate(today, d -> d.plusDays(1)).limit(400)
                .filter(d -> HOLIDAYS.containsKey(MonthDay.from(d)))
                .toList());
        delivery.setWidth("20rem");
        delivery.setHelperText("No deliveries on weekends and public holidays.");
        delivery.setI18n(new DatePicker.DatePickerI18n()
                .setDisabledDateErrorMessage("We don't deliver on that day. Please pick a weekday."));

        addDemo("Disabled weekdays and dates",
                "Disabled dates can't be picked from the overlay, and typing one in shows a dedicated error message.",
                delivery, """
                        delivery.setMin(LocalDate.now());
                        delivery.setDisabledWeekdays(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
                        delivery.setDisabledDates(holidays);
                        delivery.setI18n(new DatePicker.DatePickerI18n()
                                .setDisabledDateErrorMessage("We don't deliver on that day."));
                        """);
    }

    private void addMetadataDemo() {
        LocalDate today = LocalDate.now();
        booking.setWidth("20rem");
        booking.setHelperText("Green: plenty of rooms · Orange: few rooms left · Struck through: fully booked.");
        booking.addClassName("availability-picker");
        booking.setDateMetadataProvider(DateMetadataProvider.perDate(date -> {
            if (date.isBefore(today) || date.getDayOfWeek().getValue() > 5) {
                return null;
            }
            return switch (date.getDayOfMonth() % 5) {
                case 0 -> new DateMetadata(date, true, "booked");
                case 1, 3 -> new DateMetadata(date, "few-left");
                default -> new DateMetadata(date, "available");
            };
        }));

        var legend = new Span("Open the overlay: the colors come from CSS part names returned by the provider.");
        legend.addClassName("showcase-status");

        addDemo("Date metadata provider",
                "A DateMetadataProvider returns metadata per date: whether it's disabled and a part name for styling. "
                        + "It's asked lazily for the visible month range only.",
                new VerticalLayout(booking, legend), """
                        booking.setDateMetadataProvider(DateMetadataProvider.perDate(date ->
                                switch (availability(date)) {
                                    case FULL -> new DateMetadata(date, true, "booked");
                                    case FEW  -> new DateMetadata(date, "few-left");
                                    default   -> new DateMetadata(date, "available");
                                }));
                        """ + """

                        /* styles.css */
                        .availability-picker vaadin-month-calendar::part(few-left) { color: var(--aura-orange-text); }
                        """);
    }

    private void addDefaultTimeDemo() {
        var meeting = new DateTimePicker("Stand-up meeting");
        meeting.setDefaultTime(LocalTime.of(9, 30));
        meeting.setStep(java.time.Duration.ofMinutes(15));
        meeting.setDisabledWeekdays(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
        meeting.setWidth("24rem");
        meeting.setHelperText("Pick only a date: the time is filled in as 09:30.");

        addDemo("Default time in Date Time Picker",
                "When the user picks a date and leaves the time empty, the default time is applied.",
                meeting, """
                        DateTimePicker meeting = new DateTimePicker("Stand-up meeting");
                        meeting.setDefaultTime(LocalTime.of(9, 30));
                        meeting.setDisabledWeekdays(List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));
                        """);
    }
}
