package com.training.app.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidationUtil {
    public static final String PDF_EXTENSION = "pdf";

    public static final List<String> IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "bmp", "webp");

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("pdf", "png", "jpg", "jpeg", "gif", "bmp", "webp");

    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "application/pdf",
            "image/png",
            "image/jpeg",
            "image/gif",
            "image/bmp",
            "image/webp"
    );

    public static boolean isPdf(MultipartFile file) {
        if (!isValid(file)) {
            return false;
        }
        return PDF_EXTENSION.equals(getFileExtension(file));
    }

    public static boolean isKnownImage(MultipartFile file) {
        if (!isValid(file)) {
            return false;
        }
        return IMAGE_EXTENSIONS.contains(getFileExtension(file));
    }

    private static boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean isValidFile(MultipartFile file) {
        if (!isValid(file)) {
            return false;
        }

        String extension = getFileExtension(file);

        return ALLOWED_EXTENSIONS.contains(extension);
    }

    private static String getFileExtension(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    public static boolean isValidMimeType(MultipartFile file) {
        return ALLOWED_MIME_TYPES.contains(file.getContentType());
    }
}

