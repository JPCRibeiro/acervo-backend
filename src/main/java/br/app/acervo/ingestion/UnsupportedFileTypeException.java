package br.app.acervo.ingestion;

public class UnsupportedFileTypeException extends RuntimeException {
    public UnsupportedFileTypeException(String detectedType) {
        super("Tipo de arquivo não suportado: " + detectedType);
    }
}