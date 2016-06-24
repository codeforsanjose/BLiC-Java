package com.codeforsanjose.blic.web;

import java.io.Serializable;

public class BlicMessage implements Serializable {
    private String message;
    private types type;

    public BlicMessage() {
        this.message = null;
    }

    public BlicMessage(String message, types type) {
        this.message = message;
        this.type = type;
    }

    public types getType() {
        return type;
    }

    public void setType(types type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public enum types {error, info}
}
