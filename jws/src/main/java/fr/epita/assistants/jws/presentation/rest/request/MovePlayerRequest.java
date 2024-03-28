package fr.epita.assistants.jws.presentation.rest.request;

import lombok.Value;

@Value
public class MovePlayerRequest {
    Integer posX;
    Integer posY;
}
