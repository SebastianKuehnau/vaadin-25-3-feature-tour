package com.vaadin.devrel.featuretour.showcase.flow;

import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class BinderGroupsViewTest extends SpringBrowserlessTest {

    @Test
    void draft_needs_only_a_title() {
        var view = navigate(BinderGroupsView.class);

        test(view.saveDraft).click();
        assertThat(view.result.getText()).contains("needs at least a title");

        test(view.title).setValue("Vaadin 25.3 is here");
        test(view.saveDraft).click();
        assertThat(view.result.getText()).contains("Draft saved");
    }

    @Test
    void publishing_validates_the_publish_group() {
        var view = navigate(BinderGroupsView.class);
        test(view.title).setValue("Vaadin 25.3 is here");

        test(view.publish).click();

        assertThat(view.result.getText()).contains("3 field(s) are missing");
        assertThat(view.summary.isInvalid()).isTrue();
        assertThat(view.title.isInvalid()).isFalse();
    }

    @Test
    void required_indicators_follow_the_active_group() {
        var view = navigate(BinderGroupsView.class);
        assertThat(view.title.isRequiredIndicatorVisible()).isTrue();
        assertThat(view.summary.isRequiredIndicatorVisible()).isFalse();

        view.binder.setValidationGroups(BlogPost.Publish.class);

        assertThat(view.summary.isRequiredIndicatorVisible()).isTrue();
    }
}
