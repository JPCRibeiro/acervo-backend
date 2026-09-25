package br.app.acervo.organization.exception;

public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException() {
        super("Organização não encontrada");
    }
}
