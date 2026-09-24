package br.app.acervo;

import br.app.acervo.ingestion.service.FileTypeValidator;
import br.app.acervo.ingestion.exception.UnsupportedFileTypeException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileTypeValidatorTest {
    private final FileTypeValidator validator = new FileTypeValidator();

    @Test
    void shouldRejectSpoofedFiles() throws IOException {
        byte[] scriptPayload = "#!/bin/bash\nrm -rf /".getBytes();
        MockMultipartFile spoofedFile = new MockMultipartFile(
                "file", "curriculo.pdf", "application/pdf", scriptPayload);

        assertThatThrownBy(() -> validator.validate(spoofedFile))
                .isInstanceOf(UnsupportedFileTypeException.class)
                .hasMessageContaining("não suportado");
    }

    @Test
    void shouldAcceptSupportedFile() throws IOException {
        MockMultipartFile pdf = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf",
                "conteúdo texto simples".getBytes());
        assertThat(validator.validate(pdf)).isNotBlank();
    }
}
