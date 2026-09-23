package br.app.acervo.ingestion;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
public class FileTypeValidator {
    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
            "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
            "text/plain",
            "text/markdown",
            "text/html"
    );

    public String validate(MultipartFile file) throws IOException {
        String detectedType = tika.detect(file.getInputStream());

        if (!ALLOWED_TYPES.contains(detectedType)) {
            throw new UnsupportedFileTypeException(detectedType);
        }
        return detectedType;
    }
}