package com.vaadin.devrel.featuretour.showcase.flow;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.PropertyId;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("binder-validation-groups")
@PageTitle("Validation Groups")
public class BinderGroupsView extends ShowcasePage {

    @PropertyId("title")
    final TextField title = new TextField("Title");
    @PropertyId("summary")
    final TextArea summary = new TextArea("Summary");
    @PropertyId("category")
    final ComboBox<String> category = new ComboBox<>("Category", "Release", "Tutorial", "Community", "Case study");
    @PropertyId("publishDate")
    final DatePicker publishDate = new DatePicker("Publish date");

    final BeanValidationBinder<BlogPost> binder = new BeanValidationBinder<>(BlogPost.class, BlogPost.Draft.class);
    final Span result = new Span();
    final Button saveDraft = new Button("Save draft");
    final Button publish = new Button("Publish");

    public BinderGroupsView() {
        super("Validation Groups in Binder",
                "BeanValidationBinder now supports Jakarta Bean Validation groups. The same bean can be valid as a draft "
                        + "and still be incomplete for publishing.",
                "https://vaadin.com/docs/latest/flow/binding-data/components-binder-beans", Tier.NEW, Tier.FREE);

        binder.bindInstanceFields(this);
        binder.setBean(new BlogPost());

        var activeGroup = new RadioButtonGroup<String>("Validate while typing as");
        activeGroup.setItems("Draft", "Publish");
        activeGroup.setValue("Draft");
        activeGroup.addValueChangeListener(e -> {
            binder.setValidationGroups("Draft".equals(e.getValue()) ? BlogPost.Draft.class : BlogPost.Publish.class);
            result.setText("Required indicators now follow the " + e.getValue() + " group.");
        });

        saveDraft.addClickListener(e -> {
            if (binder.isValid(BlogPost.Draft.class)) {
                showResult(true, "Draft saved. Only the title was required.");
            } else {
                binder.validate(BlogPost.Draft.class);
                showResult(false, "A draft needs at least a title.");
            }
        });
        publish.addThemeVariants(ButtonVariant.PRIMARY);
        publish.addClickListener(e -> {
            var status = binder.validate(BlogPost.Publish.class);
            if (status.isOk()) {
                showResult(true, "Published! 🎉");
            } else {
                showResult(false, status.getFieldValidationErrors().size() + " field(s) are missing for publishing.");
            }
        });

        var form = new FormLayout(title, category, summary, publishDate);
        form.setColspan(summary, 2);
        form.setMaxWidth("40rem");
        result.addClassName("showcase-status");

        addDemo("One bean, two sets of rules",
                "Save a draft with just a title, then try to publish. Switch the group used while typing and "
                        + "watch the required indicators change.",
                new VerticalLayout(activeGroup, form, new HorizontalLayout(saveDraft, publish), result), """
                        public class BlogPost {
                            public interface Draft {}
                            public interface Publish {}

                            @NotEmpty(groups = {Draft.class, Publish.class})
                            private String title;

                            @NotEmpty(groups = Publish.class) @Size(min = 20, groups = Publish.class)
                            private String summary;
                            ...
                        }

                        var binder = new BeanValidationBinder<>(BlogPost.class, BlogPost.Draft.class);
                        binder.bindInstanceFields(this);

                        // Change the groups used while the user types (and for required indicators)
                        binder.setValidationGroups(BlogPost.Publish.class);

                        // One-shot checks when saving
                        if (binder.isValid(BlogPost.Draft.class)) saveDraft();
                        BinderValidationStatus<BlogPost> status = binder.validate(BlogPost.Publish.class);
                        """);
    }

    private void showResult(boolean ok, String text) {
        result.setText((ok ? "✅ " : "⚠️ ") + text);
    }
}
