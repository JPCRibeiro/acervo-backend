package br.app.acervo.shared.exception;

import br.app.acervo.document.exception.DocumentNotFoundException;
import br.app.acervo.ingestion.exception.IngestionException;
import br.app.acervo.ingestion.exception.UnsupportedFileTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    public record ErrorField(String field, String message) {}

    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            List<ErrorField> fieldErrors
    ) {}

    @ExceptionHandler(DocumentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDocumentNotFound(Exception ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnsupportedFileTypeException.class)
    public ResponseEntity<ErrorResponse>  handleUnsupportedType(UnsupportedFileTypeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IngestionException.class)
    public ResponseEntity<ErrorResponse>  handleIngestion(IngestionException ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        log.error("Erro não tratado", e);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno");
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return build(status, message, List.of());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<ErrorField> fields) {
        ErrorResponse body = new ErrorResponse(
                Instant.now(),
                status.value(),
                message,
                fields
        );
        return ResponseEntity.status(status).body(body);
    }
}
