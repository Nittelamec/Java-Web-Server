package fr.epita.assistants.jws.converter;

import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.presentation.rest.response.PlayerResponse;

import java.util.ArrayList;
import java.util.List;

public class EntToResponse {
    public static GameDetailResponse gameDetailResponse(GameEntity game) {
        List<PlayerResponse> list = new ArrayList<>();
        for (PlayerEntity player : game.getPlayers()) {
            list.add(playerResponse(player));
        }
        return new GameDetailResponse(game.getStartTime(), game.getState(), list, game.getMap(), game.getId());
    }
    public static PlayerResponse playerResponse(PlayerEntity player) {
        return new PlayerResponse(player.getId(), player.getLives(), player.getName(), player.getPosX(), player.getPosY());
    }
}
