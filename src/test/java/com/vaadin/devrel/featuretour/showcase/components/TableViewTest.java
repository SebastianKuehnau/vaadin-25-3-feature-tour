package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class TableViewTest extends SpringBrowserlessTest {

    @Test
    void rows_follow_the_list_signal() {
        var view = navigate(TableView.class);
        assertThat(view.planetTable.getBodyRows()).hasSize(2);
        assertThat(view.planetTable.getCaptionText()).isEqualTo("Planets of our solar system (2 of 8)");

        test($(Button.class).withText("Add next planet").single()).click();

        assertThat(view.planetTable.getBodyRows()).hasSize(3);
        assertThat(view.planetTable.getBodyRows().getLast().getHeaderCells().getFirst().getText()).isEqualTo("Earth");
        assertThat(view.planetTable.getCaptionText()).isEqualTo("Planets of our solar system (3 of 8)");
    }

    @Test
    void removing_a_planet_removes_its_row() {
        var view = navigate(TableView.class);

        view.planets.remove(view.planets.peek().getFirst());

        assertThat(view.planetTable.getBodyRows()).hasSize(1);
        assertThat(view.planetTable.getBodyRows().getFirst().getHeaderCells().getFirst().getText()).isEqualTo("Venus");
    }
}
