package br.app.acervo.document.exception;

public class DocumentNotFoundException extends RuntimeException {
    public DocumentNotFoundException() {
        super("Documento não encontrado");
    }
}
