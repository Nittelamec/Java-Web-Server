package fr.epita.assistants.jws.domain.entity;

import fr.epita.assistants.jws.utils.GameState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GameEntity {
    Timestamp startTime;
    List<PlayerEntity> players;
    Long id;
    List<String> map;
    GameState state;
}
