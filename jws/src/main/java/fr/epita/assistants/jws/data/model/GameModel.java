package fr.epita.assistants.jws.data.model;

import fr.epita.assistants.jws.utils.GameState;
import lombok.*;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "game")
@With
public class GameModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public Timestamp starttime;
    public GameState state;
    @OneToMany(targetEntity = PlayerModel.class)
    public List<PlayerModel> players;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "game_map", joinColumns = @JoinColumn(name = "gamemodel_id"))
    public List<String> map;
}