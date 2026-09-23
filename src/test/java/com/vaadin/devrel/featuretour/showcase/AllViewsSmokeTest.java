package com.vaadin.devrel.featuretour.showcase;

import com.vaadin.devrel.featuretour.showcase.ai.AssistantView;
import com.vaadin.devrel.featuretour.showcase.ai.DataExplorerView;
import com.vaadin.devrel.featuretour.showcase.ai.FormFillerView;
import com.vaadin.devrel.featuretour.showcase.components.AccessibilityView;
import com.vaadin.devrel.featuretour.showcase.components.BreadcrumbsView;
import com.vaadin.devrel.featuretour.showcase.components.CatalogProductView;
import com.vaadin.devrel.featuretour.showcase.components.ComboBoxView;
import com.vaadin.devrel.featuretour.showcase.components.DatePickerView;
import com.vaadin.devrel.featuretour.showcase.components.GridView;
import com.vaadin.devrel.featuretour.showcase.components.MessageListView;
import com.vaadin.devrel.featuretour.showcase.components.SwitchView;
import com.vaadin.devrel.featuretour.showcase.components.TableView;
import com.vaadin.devrel.featuretour.showcase.components.UploadView;
import com.vaadin.devrel.featuretour.showcase.flow.BinderGroupsView;
import com.vaadin.devrel.featuretour.showcase.flow.EventBusView;
import com.vaadin.devrel.featuretour.showcase.flow.ReactiveElementsView;
import com.vaadin.devrel.featuretour.showcase.home.HomeView;
import com.vaadin.devrel.featuretour.showcase.ops.ObservabilityView;
import com.vaadin.devrel.featuretour.showcase.ops.UnderTheHoodView;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.Component;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Opens every showcase page once, so a broken page fails the build.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class AllViewsSmokeTest extends SpringBrowserlessTest {

    @ParameterizedTest
    @ValueSource(classes = {HomeView.class, SwitchView.class, TableView.class, BreadcrumbsView.class,
            CatalogProductView.class, DatePickerView.class, ComboBoxView.class, MessageListView.class,
            UploadView.class, GridView.class, AccessibilityView.class, BinderGroupsView.class,
            ReactiveElementsView.class, EventBusView.class, FormFillerView.class, DataExplorerView.class,
            AssistantView.class, ObservabilityView.class, UnderTheHoodView.class})
    void page_opens(Class<? extends Component> view) {
        assertThat(navigate(view)).isInstanceOf(view);
    }
}
