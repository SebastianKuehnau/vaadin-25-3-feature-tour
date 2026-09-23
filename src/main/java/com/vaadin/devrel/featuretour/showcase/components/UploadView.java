package com.vaadin.devrel.featuretour.showcase.components;

import com.vaadin.devrel.featuretour.base.ui.ShowcasePage;
import com.vaadin.devrel.featuretour.base.ui.Tier;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadContent;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadRejectedException;
import com.vaadin.flow.server.streams.UploadValidator;
import com.vaadin.flow.server.streams.TransferContext;
import com.vaadin.flow.server.streams.TransferProgressListener;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Route("upload")
@PageTitle("Upload Validation")
public class UploadView extends ShowcasePage {

    static final long MAX_SIZE = 2 * 1024 * 1024;
    static final int MAX_DIMENSION = 2000;

    /**
     * Accepts real PNG and JPEG images up to 2 MB and 2000 px, checking in all
     * three phases of an upload.
     */
    static class ImageUploadValidator implements UploadValidator {

        @Override
        public void validateMetadata(UploadEvent event) {
            String name = event.getFileName().toLowerCase(Locale.ROOT);
            if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
                event.reject("Metadata check: only .png and .jpg files are accepted");
            } else if (event.getFileSize() > MAX_SIZE) {
                event.reject("Metadata check: the file is larger than 2 MB");
            }
        }

        @Override
        public int headerSize() {
            return 8;
        }

        @Override
        public void validateHeader(UploadEvent event, ByteBuffer header) {
            if (!isPng(header) && !isJpeg(header)) {
                event.reject("Header check: the content is not a PNG or JPEG image, whatever the file name says");
            }
        }

        @Override
        public void validateComplete(UploadEvent event, UploadContent content) throws IOException {
            try (InputStream in = content.getInputStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    event.reject("Complete check: the image could not be decoded");
                } else if (image.getWidth() > MAX_DIMENSION || image.getHeight() > MAX_DIMENSION) {
                    event.reject("Complete check: the image is larger than 2000 × 2000 px");
                }
            }
        }

        static boolean isPng(ByteBuffer header) {
            byte[] signature = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
            if (header.remaining() < signature.length) {
                return false;
            }
            for (int i = 0; i < signature.length; i++) {
                if (header.get(header.position() + i) != signature[i]) {
                    return false;
                }
            }
            return true;
        }

        static boolean isJpeg(ByteBuffer header) {
            return header.remaining() >= 3
                    && header.get(header.position()) == (byte) 0xFF
                    && header.get(header.position() + 1) == (byte) 0xD8
                    && header.get(header.position() + 2) == (byte) 0xFF;
        }
    }

    final Div log = new Div();
    final FlexLayout gallery = new FlexLayout();

    public UploadView() {
        super("Upload Validation",
                "Validate uploads while they are received, without writing your own UploadHandler: "
                        + "check the metadata, sniff the first bytes, or inspect the complete content.",
                "https://vaadin.com/docs/latest/components/upload/file-handling", Tier.NEW, Tier.FREE);

        var rejections = new TransferProgressListener() {
            @Override
            public void onError(TransferContext context, IOException reason) {
                context.getUI().access(() -> logEntry(false, context.fileName() + ": "
                        + (reason instanceof UploadRejectedException ? reason.getMessage() : "upload failed")));
            }
        };
        var handler = UploadHandler.inMemory((metadata, data) -> {
                    // The success callback only runs for uploads that passed every phase
                    var image = new Image(DownloadHandler.fromInputStream(event -> new DownloadResponse(
                            new ByteArrayInputStream(data), metadata.fileName(), metadata.contentType(), data.length)),
                            metadata.fileName());
                    image.addClassName("upload-thumbnail");
                    gallery.getUI().ifPresent(ui -> ui.access(() -> {
                        logEntry(true, metadata.fileName() + " accepted");
                        gallery.add(image);
                    }));
                }, rejections)
                .withValidator(new ImageUploadValidator());

        var upload = new Upload(handler);
        upload.setMaxFiles(10);

        var samples = new FlexLayout(
                sampleDownload("sample.png", "A real PNG image", this::realPng),
                sampleDownload("renamed.png", "A text file named .png",
                        () -> "I am not an image.".getBytes(StandardCharsets.UTF_8)),
                sampleDownload("huge.png", "A real PNG, 2400 px wide", () -> png(2400, 200)),
                sampleDownload("notes.txt", "A text file", () -> "hello".getBytes(StandardCharsets.UTF_8)));
        samples.addClassName("showcase-row");

        log.addClassName("upload-log");
        gallery.addClassName("upload-gallery");

        addDemo("Three validation phases",
                "Download the sample files, then drop them on the upload. Each one is rejected in a different phase, "
                        + "and the rejection reason arrives as an UploadRejectedException.",
                new VerticalLayout(samples, upload, log, gallery), """
                        public class ImageUploadValidator implements UploadValidator {
                            @Override
                            public void validateMetadata(UploadEvent event) {       // before reading any data
                                if (!event.getFileName().endsWith(".png")) {
                                    event.reject("Only .png files are accepted");
                                }
                            }
                            @Override
                            public int headerSize() { return 8; }
                            @Override
                            public void validateHeader(UploadEvent event, ByteBuffer header) {  // first bytes
                                if (!isPng(header)) event.reject("Not a PNG image");
                            }
                            @Override
                            public void validateComplete(UploadEvent event, UploadContent content) {  // all data
                                if (ImageIO.read(content.getInputStream()) == null) event.reject("Can't decode");
                            }
                        }

                        var handler = UploadHandler.inMemory((metadata, data) -> show(data),
                                        new TransferProgressListener() {
                                            @Override
                                            public void onError(TransferContext context, IOException reason) {
                                                if (reason instanceof UploadRejectedException) {
                                                    showError(reason.getMessage());
                                                }
                                            }
                                        })
                                .withValidator(new ImageUploadValidator());
                        """);
    }

    private void logEntry(boolean ok, String text) {
        var icon = ok ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.BAN.create();
        var entry = new Div(icon, new Span(text));
        entry.addClassNames("upload-log-entry", ok ? "ok" : "rejected");
        log.addComponentAsFirst(entry);
    }

    private Anchor sampleDownload(String fileName, String label, com.vaadin.flow.function.SerializableSupplier<byte[]> bytes) {
        var anchor = new Anchor(DownloadHandler.fromInputStream(event -> {
            byte[] data = bytes.get();
            return new DownloadResponse(new ByteArrayInputStream(data), fileName, "application/octet-stream", data.length);
        }), "⬇ " + fileName);
        anchor.getElement().setAttribute("download", fileName);
        anchor.setTitle(label);
        anchor.addClassName("upload-sample");
        return anchor;
    }

    private byte[] realPng() {
        return png(320, 200);
    }

    static byte[] png(int width, int height) {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        g.setPaint(new GradientPaint(0, 0, new Color(0x1676f3), width, height, new Color(0x00b4f0)));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.drawString("Vaadin 25.3", 20, 40);
        g.dispose();
        try (var out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
