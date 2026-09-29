package br.app.acervo.conversation.exception;

public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException() {
        super("Conversa não encontrada");
    }
}