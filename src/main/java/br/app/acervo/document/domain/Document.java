package br.app.acervo.document.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="documents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Document {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name="organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "file_name", nullable = false, updatable = false)
    private String fileName;

    @Column(name="s3_key", nullable = false, updatable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(name = "chunk_count", nullable = false)
    private int chunkCount;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private Instant uploadedAt;

    private Document(UUID organizationId, String fileName, String s3Key) {
        this.organizationId = organizationId;
        this.fileName = fileName;
        this.s3Key = s3Key;
        this.status = DocumentStatus.PENDING;
        this.chunkCount = 0;
        this.uploadedAt = Instant.now();
    }

    public static Document create(UUID organizationId, String fileName, String s3Key) {
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId é obrigatório");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName é obrigatório");
        }
        if (s3Key == null || s3Key.isBlank()) {
            throw new IllegalArgumentException("s3Key é obrigatória");
        }
        return new Document(organizationId, fileName, s3Key);
    }

    public void startProcessing() {
        if (status != DocumentStatus.PENDING) {
            throw new IllegalStateException("Apenas documentos PENDING podem iniciar o processamento.");
        }
        this.status = DocumentStatus.PROCESSING;
    }

    public void completeProcessing(int chunkCount) {
        if (status != DocumentStatus.PROCESSING)
            throw new IllegalStateException("O documento deve estar em PROCESSING para ser concluído.");
        if (chunkCount <= 0) {
            throw new IllegalArgumentException("Documento processado precisa ter ao menos 1 chunk");
        }
        this.status = DocumentStatus.READY;
        this.chunkCount = chunkCount;
    }

    public void fail(String reason) {
        this.status = DocumentStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isReady() {
        return status == DocumentStatus.READY;
    }
}
