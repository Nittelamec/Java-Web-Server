package fr.epita.assistants.jws.presentation.rest.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    public Long id;
    public Integer lives;
    public String name;
    public Integer posX;
    public Integer posY;
}