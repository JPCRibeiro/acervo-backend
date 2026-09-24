package br.app.acervo;

import br.app.acervo.document.domain.Document;
import br.app.acervo.document.domain.DocumentStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

public class DocumentTest {
    private Document pendingDocument() {
        return Document.create(UUID.randomUUID(), "file.pdf", "organization/file.pdf");
    }

    @Test
    void shouldCreateValidDocument() {
        Document doc = pendingDocument();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(doc.getChunkCount()).isZero();
        assertThat(doc.getUploadedAt()).isNotNull();
    }

    @Test
    void shouldRejectInvalidDocument() {
        assertThatThrownBy(() -> Document.create(null, "f.pdf", "k"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Document.create(UUID.randomUUID(), " ", "k"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Document.create(UUID.randomUUID(), "f.pdf", " "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldTransitionToProcessing() {
        Document doc = pendingDocument();
        doc.startProcessing();
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.PROCESSING);
    }

    @Test
    void shouldFailToTransitionToProcessingIfNotPending() {
        Document doc = pendingDocument();
        doc.startProcessing();
        assertThatThrownBy(doc::startProcessing)
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCompleteProcessingMovesToReady() {
        Document doc = pendingDocument();
        doc.startProcessing();
        doc.completeProcessing(12);
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.READY);
        assertThat(doc.getChunkCount()).isEqualTo(12);
        assertThat(doc.isReady()).isTrue();
    }

    @Test
    void shouldFailToTansitionToCompleteProcessingIfNotProcessing() {
        Document doc = pendingDocument();
        assertThatThrownBy(() -> doc.completeProcessing(5))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFailToTansitionToCompleteProcessingIfChunkIsInvalid() {
        Document doc = pendingDocument();
        doc.startProcessing();
        assertThatThrownBy(() -> doc.completeProcessing(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFailDocument() {
        Document doc = pendingDocument();
        doc.fail("arquivo corrompido");
        assertThat(doc.getStatus()).isEqualTo(DocumentStatus.FAILED);
        assertThat(doc.getFailureReason()).isEqualTo("arquivo corrompido");
    }
}
