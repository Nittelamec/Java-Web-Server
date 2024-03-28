package fr.epita.assistants.jws.domain.service;

import fr.epita.assistants.jws.converter.EntToResponse;
import fr.epita.assistants.jws.converter.ModelToEnt;
import fr.epita.assistants.jws.data.model.GameModel;
import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.data.repository.PlayerRepository;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.utils.GameState;
import fr.epita.assistants.jws.utils.MyPair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PlayerService {
    @Inject
    PlayerRepository playerRepository;
    @Inject
    GameService gameService;
    @ConfigProperty(name = "JWS_TICK_DURATION")
    long tickDuration;
    @ConfigProperty(name = "JWS_DELAY_MOVEMENT")
    long delayMovement;
    @ConfigProperty(name = "JWS_DELAY_BOMB")
    long delayBomb;

    public boolean IsBad(PlayerModel playerModel, Integer posX, Integer posY, List<List<String>> map) {
        Integer x = playerModel.getPosx();
        Integer y = playerModel.getPosy();
        boolean isCardinal = (
                (posX.equals(x + 1) && (posY.equals(y)))
                        || ((posX.equals(x - 1) && (posY.equals(y))))
                        || ((posX.equals(x) && (posY.equals(y + 1))))
                        || ((posX.equals(x) && (posY.equals(y - 1))))
        );
        boolean isPossible = (map.get(posY).get(posX).equals("G") || map.get(posY).get(posX).equals("B"));

        return !(isCardinal && isPossible);
    }

    public boolean HasMoved(PlayerModel playerModel) {
        if (playerModel.getLastmovement() == null) {
            return false;
        }
        long time = playerModel.getLastmovement().getTime() + (delayMovement * tickDuration);
        boolean res = Timestamp.from(Instant.now()).after(new Timestamp(time));
        return !res;
    }

    public boolean HasPLacedBomb(PlayerModel playerModel) {
        if (playerModel.getLastbomb() == null) {
            return false;
        }
        long time = playerModel.getLastbomb().getTime() + (delayBomb * tickDuration);
        boolean res = Timestamp.from(Instant.now()).after(new Timestamp(time));
        return !res;
    }

    @Transactional
    public PlayerEntity CreateNewPlayer(String playerName, GameModel game) {
        int[][] positions = {{15, 1}, {15, 13}, {1, 13}};
        int x = (game.getPlayers().size() == 0) ? 1 : positions[game.getPlayers().size() - 1][0];
        int y = (game.getPlayers().size() == 0) ? 1 : positions[game.getPlayers().size() - 1][1];
        PlayerModel newPlayer =
                new PlayerModel().withGame(game).withName(playerName).withLives(3).withPosx(x).withPosy(y);
        playerRepository.persist(newPlayer);
        game.getPlayers().add(newPlayer);
        return ModelToEnt.PlayerModelToEntity(newPlayer);
    }


    @Transactional
    public MyPair<Integer, GameDetailResponse> move(Integer posX, Integer posY, Long gameId, Long playerId) {
        Optional<GameModel> gameModelOptional = gameService.getGameId(gameId);
        Optional<PlayerModel> playerModelOptional = playerRepository.findByIdOptional(playerId);
        int code;
        GameDetailResponse response;
        if (gameModelOptional.isEmpty() || playerModelOptional.isEmpty()) {
            code = 404;
            response = null;
        } else {
            GameModel gameModel = gameModelOptional.get();
            PlayerModel playerModel = playerModelOptional.get();
            List<List<String>> map = gameService.ParseMap(gameModel.getMap());
            if (gameService.IsPLayerAbsent(gameModel, playerId)) {
                code = 404;
                response = null;
            } else if (IsBad(playerModel, posX, posY, map)) {
                code = 400;
                response = null;
            } else if (gameModel.getState() != GameState.RUNNING) {
                code = 400;
                response = null;
            } else if (playerModel.getLives() == 0) {
                code = 400;
                response = null;
            } else if (HasMoved(playerModel)) {
                code = 429;
                response = null;
            } else {
                code = 200;
                playerModel.setPosx(posX);
                playerModel.setPosy(posY);
                playerModel.setLastmovement(Timestamp.from(Instant.now()));
                response = EntToResponse.gameDetailResponse(ModelToEnt.GameModelToEntity(gameModel));
            }
        }
        return new MyPair<>(code, response);
    }

    @Transactional
    public MyPair<Integer, GameDetailResponse> putBomb(Integer posX, Integer posY, Long gameId, Long playerId) {
        Optional<GameModel> gameModelOptional = gameService.getGameId(gameId);
        Optional<PlayerModel> playerModelOptional = playerRepository.findByIdOptional(playerId);
        int code;
        GameDetailResponse response;
        if (gameModelOptional.isEmpty() || playerModelOptional.isEmpty()) {
            code = 404;
            response = null;
        } else {
            GameModel gameModel = gameModelOptional.get();
            PlayerModel playerModel = playerModelOptional.get();
            List<List<String>> map = gameService.ParseMap(gameModel.getMap());
            if (gameService.IsPLayerAbsent(gameModel, playerId)) {
                code = 404;
                response = null;
            } else if (!(posX.equals(playerModel.getPosx())) || !(posY.equals(playerModel.getPosy()))) {
                code = 400;
                response = null;
            } else if (gameModel.getState() != GameState.RUNNING) {
                code = 400;
                response = null;
            } else if (playerModel.getLives() == 0) {
                code = 400;
                response = null;
            } else if (HasPLacedBomb(playerModel)) {
                code = 429;
                response = null;
            } else {
                code = 200;
                map.get(posY).set(posX, "B");
                List<String> newMap = gameService.RevertMap(map);
                gameModel.setMap(newMap);
                playerModel.setLastbomb(Timestamp.from(Instant.now()));
                startExplosion(gameId, posX, posY);
                response = EntToResponse.gameDetailResponse(ModelToEnt.GameModelToEntity(gameModel));
            }
        }
        return new MyPair<>(code, response);
    }

    public void startExplosion(Long gameId, Integer xbomb, Integer ybomb) {
        Executor executor = CompletableFuture.delayedExecutor(delayBomb * tickDuration, TimeUnit.MILLISECONDS);
        CompletableFuture.runAsync(() -> {
            Explosion(gameId, xbomb, ybomb);
        }, executor);
    }

    @Transactional
    public void Explosion(Long gameId, Integer xbomb, Integer ybomb) {
        Optional<GameModel> gameModelOptional = gameService.getGameId(gameId);
        if (gameModelOptional.isEmpty()) {
            return;
        }
        GameModel gameModel = gameModelOptional.get();
        List<List<String>> map = gameService.ParseMap(gameModel.getMap());
        int[] tile1 = {xbomb, ybomb};
        int[] tile2 = {xbomb - 1, ybomb};
        int[] tile3 = {xbomb + 1, ybomb};
        int[] tile4 = {xbomb, ybomb - 1};
        int[] tile5 = {xbomb, ybomb + 1};
        int[][] tiles = {tile1, tile2, tile3, tile4, tile5};
        int dead = 0;
        for (PlayerModel player : gameModel.getPlayers()) {
            int x = player.getPosx();
            int y = player.getPosy();
            for (int[] tile : tiles) {
                if (x == tile[0] && y == tile[1]) {
                    if (player.getLives() > 0) {
                        player.setLives(player.getLives() - 1);
                        if (player.getLives() == 0)
                            dead += 1;
                    }
                }
            }
        }
        System.out.println(dead);
        for (int[] tile : tiles) {
            String block = map.get(tile[1]).get(tile[0]);
            if (block.equals("W")) {
                map.get(tile[1]).set(tile[0], "G");
            }
        }
        map.get(ybomb).set(xbomb, "G");
        List<String> newMap = gameService.RevertMap(map);
        gameModel.setMap(newMap);
        if (dead == gameModel.getPlayers().size() || dead == gameModel.getPlayers().size() - 1) {
            gameModel.setState(GameState.FINISHED);
        }
    }
}