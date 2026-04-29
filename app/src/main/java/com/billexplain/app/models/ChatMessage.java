package com.billexplain.app.models;

public class ChatMessage {
    public static final int TYPE_AI = 0;
    public static final int TYPE_USER = 1;

    public String text;
    public int type;

    public ChatMessage(String text, int type) {
        this.text = text;
        this.type = type;
    }
}
