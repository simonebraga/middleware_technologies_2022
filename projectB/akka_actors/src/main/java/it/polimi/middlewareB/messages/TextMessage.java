package it.polimi.middlewareB.messages;

public class TextMessage {

    public TextMessage(String text) {
	this.text = text;
    }

    public String getText() {
	return text;
    }

    private String text;
}
