package fr.epita.assistants.presentation.rest.response;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class HelloResponse {
    String content;

    public HelloResponse(String Content) {
        this.content = Content;
    }
}
