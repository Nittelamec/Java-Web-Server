package fr.epita.assistants.jws.data.model;

import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "player")
@With
public class PlayerModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Timestamp lastbomb;
    public Timestamp lastmovement;
    public Integer lives;
    public String name;
    public Integer posx;
    public Integer posy;
    public Integer position;
    @ManyToOne(targetEntity = GameModel.class)
    public GameModel game;
}
