package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SwitchViewTest extends SpringBrowserlessTest {

    @Test
    void status_follows_the_switch() {
        var view = navigate(SwitchView.class);
        assertThat(test(view.notifications).isOn()).isTrue();
        assertThat(view.notificationStatus.getText()).contains("You will get an email");

        test(view.notifications).switchOff();

        assertThat(view.notificationStatus.getText()).contains("Notifications are off");
    }

    @Test
    void required_switch_becomes_invalid_when_turned_off() {
        var view = navigate(SwitchView.class);

        test(view.terms).switchOn();
        test(view.terms).switchOff();

        assertThat(view.terms.isInvalid()).isTrue();
    }
}
