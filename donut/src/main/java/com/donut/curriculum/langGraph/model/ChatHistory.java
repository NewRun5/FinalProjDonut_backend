package com.donut.curriculum.langGraph.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
public class ChatHistory implements Serializable {
    private String sender;
    private String message;

    @Override
    public String toString() {
        return "{" + sender + ":" + message + "}";
    }
}
