package br.app.acervo.membership.exception;

public class AlreadyAMemberException extends RuntimeException {
    public AlreadyAMemberException() {
        super("Usuário já pertence a esta organização");
    }
}