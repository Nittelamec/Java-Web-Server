package fr.epita.assistants.presentation.rest.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class ReverseResponse {
    String original;
    String reversed;

    public ReverseResponse(String Original, String Reversed) {
        original = Original;
        reversed = Reversed;
    }
}
