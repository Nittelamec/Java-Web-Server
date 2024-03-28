package fr.epita.assistants.jws.domain.entity;

import lombok.*;
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerEntity {
    public Long id;
    public Integer lives;
    public String name;
    public Integer posX;
    public Integer posY;
}
