package fr.epita.assistants.jws.converter;

import fr.epita.assistants.jws.data.model.GameModel;
import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;

import java.util.ArrayList;
import java.util.List;

public class ModelToEnt {
    public static PlayerEntity PlayerModelToEntity(PlayerModel player) {
        return new PlayerEntity(player.getId(), player.getLives(), player.getName(), player.getPosx(), player.getPosy());
    }
    public static GameEntity GameModelToEntity(GameModel game) {
        List<PlayerEntity> entityPlayers = new ArrayList<>();
        for (PlayerModel player: game.getPlayers()) {
            PlayerEntity tmp = ModelToEnt.PlayerModelToEntity(player);
            entityPlayers.add(tmp);
        }
        return new GameEntity(game.getStarttime(), entityPlayers, game.getId(), game.getMap(), game.getState());
    }
}
