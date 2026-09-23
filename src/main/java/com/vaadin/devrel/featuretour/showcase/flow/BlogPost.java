package com.vaadin.devrel.featuretour.showcase.flow;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

/**
 * A blog post that can be saved as a draft with only a title, but needs all
 * fields before it can be published.
 */
public class BlogPost {

    /** Constraints that apply when saving a draft. */
    public interface Draft {
    }

    /** Constraints that apply when publishing. */
    public interface Publish {
    }

    @NotEmpty(groups = {Draft.class, Publish.class}, message = "Every post needs a title")
    @Size(max = 80, groups = {Draft.class, Publish.class}, message = "Keep the title under 80 characters")
    private String title = "";

    @NotEmpty(groups = Publish.class, message = "Add a summary before publishing")
    @Size(min = 20, groups = Publish.class, message = "The summary needs at least 20 characters")
    private String summary = "";

    @NotNull(groups = Publish.class, message = "Pick a category before publishing")
    private @Nullable String category;

    @NotNull(groups = Publish.class, message = "Set a publish date")
    @FutureOrPresent(groups = Publish.class, message = "The publish date can't be in the past")
    private @Nullable LocalDate publishDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public @Nullable String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    public @Nullable LocalDate getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(@Nullable LocalDate publishDate) {
        this.publishDate = publishDate;
    }
}
