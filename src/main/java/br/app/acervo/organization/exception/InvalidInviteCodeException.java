package br.app.acervo.organization.exception;

public class InvalidInviteCodeException extends RuntimeException {
    public InvalidInviteCodeException() {
        super("Código de convite inválido");
    }
}
