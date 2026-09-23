package com.vaadin.devrel.featuretour.showcase.components;

import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class UploadValidatorTest {

    @Test
    void recognizes_png_signature() {
        byte[] png = UploadView.png(10, 10);

        assertThat(UploadView.ImageUploadValidator.isPng(ByteBuffer.wrap(png, 0, 8))).isTrue();
        assertThat(UploadView.ImageUploadValidator.isJpeg(ByteBuffer.wrap(png, 0, 8))).isFalse();
    }

    @Test
    void rejects_text_disguised_as_png() {
        var text = ByteBuffer.wrap("I am not an image.".getBytes(StandardCharsets.UTF_8), 0, 8);

        assertThat(UploadView.ImageUploadValidator.isPng(text)).isFalse();
        assertThat(UploadView.ImageUploadValidator.isJpeg(text)).isFalse();
    }

    @Test
    void recognizes_jpeg_signature() {
        var jpeg = ByteBuffer.wrap(new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0});

        assertThat(UploadView.ImageUploadValidator.isJpeg(jpeg)).isTrue();
    }
}
