package br.app.acervo.organization.exception;

public class DuplicateOrganizationNameException extends RuntimeException {
    public DuplicateOrganizationNameException() {
        super("Você já possui uma organização com este nome");
    }
}