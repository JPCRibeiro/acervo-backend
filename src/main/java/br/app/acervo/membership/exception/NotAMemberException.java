package br.app.acervo.membership.exception;

public class NotAMemberException extends RuntimeException {
    public NotAMemberException() {
        super("Usuário não pertence à organização informada");
    }
}