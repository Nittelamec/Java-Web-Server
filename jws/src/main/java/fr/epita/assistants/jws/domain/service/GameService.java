package fr.epita.assistants.jws.domain.service;

import fr.epita.assistants.jws.converter.EntToResponse;
import fr.epita.assistants.jws.converter.ModelToEnt;
import fr.epita.assistants.jws.data.model.GameModel;
import fr.epita.assistants.jws.data.model.PlayerModel;
import fr.epita.assistants.jws.data.repository.GameRepository;
import fr.epita.assistants.jws.domain.entity.GameEntity;
import fr.epita.assistants.jws.domain.entity.PlayerEntity;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.presentation.rest.response.GameListResponse;
import fr.epita.assistants.jws.utils.GameState;
import fr.epita.assistants.jws.utils.MyPair;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class GameService {
    @Inject
    GameRepository gameRepository;
    @Inject
    PlayerService playerService;

    @ConfigProperty(name = "JWS_MAP_PATH")
    String mapPath;

    /*@ConfigProperty(name = "JWS_DELAY_FREE")
    long delayFree;
    @ConfigProperty(name = "JWS_DELAY_SHRINK")
    long delayShrink;*/


    @Transactional
    public List<GameListResponse> getGameList() {
        List<GameModel> games = gameRepository.listAll();
        List<GameEntity> entities = new ArrayList<>();
        for (GameModel game : games) {
            GameEntity entityGame = ModelToEnt.GameModelToEntity(game);
            entities.add(entityGame);
        }
        List<GameListResponse> responses = new ArrayList<>();
        for (GameEntity tmp : entities) {
            responses.add(new GameListResponse(tmp.getId(), tmp.getPlayers().size(), tmp.getState()));
        }
        return responses;
    }

    public Optional<GameModel> getGameId(Long gameId) {
        return gameRepository.findByIdOptional(gameId);
    }

    public GameDetailResponse getGameInfo(Long gameId) {
        Optional<GameModel> OptGame = gameRepository.findByIdOptional(gameId);
        if (OptGame.isEmpty()) {
            return null;
        }
        GameModel game = OptGame.get();
        return EntToResponse.gameDetailResponse(ModelToEnt.GameModelToEntity(game));
    }

    @Transactional
    public GameDetailResponse createGame(String playerName) {
        List<PlayerModel> players = new ArrayList<>();
        GameModel game = new GameModel()
                .withStarttime(Timestamp.from(Instant.now()))
                .withState(GameState.STARTING)
                .withMap(OpenMap())
                .withPlayers(players);

        playerService.CreateNewPlayer(playerName, game);
        gameRepository.persist(game);
        return EntToResponse.gameDetailResponse(ModelToEnt.GameModelToEntity(game));
    }

    public List<String> OpenMap() {
        try {
            System.out.println(mapPath);
            return Files.readAllLines(Path.of(mapPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public MyPair<Integer, GameDetailResponse> joinGame(Long gameId, String playerName) {
        int code;
        GameDetailResponse res;
        Optional<GameModel> game = gameRepository.findByIdOptional(gameId);
        if (game.isEmpty()) {
            code = 404;
            res = null;
        } else if (game.get().getPlayers().size() == 4 || game.get().getState() != GameState.STARTING) {
            code = 400;
            res = null;
        } else {
            code = 200;
            PlayerEntity newPlayer = playerService.CreateNewPlayer(playerName, game.get());
            GameEntity entityGame = ModelToEnt.GameModelToEntity(game.get());
            res = EntToResponse.gameDetailResponse(entityGame);
        }
        return new MyPair<>(code, res);
    }

    /*public void StartShrinking() {
        Executor executor = CompletableFuture.delayedExecutor((delayFree * playerService.tickDuration), TimeUnit.MILLISECONDS);
        CompletableFuture.runAsync(() -> Shrinking(), )
    }
    public void Shrinking() {

    }*/

    @Transactional
    public MyPair<Integer, GameDetailResponse> startGame(Long gameId) {
        Optional<GameModel> game = gameRepository.findByIdOptional(gameId);
        int code;
        GameDetailResponse res;
        if (game.isEmpty() || game.get().getState() == GameState.FINISHED) {
            code = 404;
            res = null;
        } else {
            code = 200;
            if (game.get().getPlayers().size() == 1) {
                game.get().setState(GameState.FINISHED);
            } else {
                game.get().setState(GameState.RUNNING);
            }
            res = EntToResponse.gameDetailResponse(ModelToEnt.GameModelToEntity(game.get()));
        }
        return new MyPair<>(code, res);
    }

    public List<List<String>> ParseMap(List<String> map) {
        List<List<String>> matrix = new ArrayList<>();
        int pos_matrix = 0;
        for (String line : map) {
            matrix.add(new ArrayList<>());
            int i = 0;
            while (i < line.length()) {
                String integer = String.valueOf(line.charAt(i));
                if (integer.matches("[0-9]")) {
                    for (int j = 0; j < Integer.parseInt(integer); j++) {
                        if (i + 1 == line.length()) {
                            return null;
                        } else {
                            String character = String.valueOf(line.charAt(i + 1));
                            matrix.get(pos_matrix).add(character);
                        }
                    }
                } else {
                    return null;
                }
                i += 2;
            }
            pos_matrix += 1;
        }
        return matrix;
    }

    public List<String> RevertMap(List<List<String>> map) {
        List<String> mapList = new ArrayList<>();
        for (List<String> line : map) {
            int i = 0;
            StringBuilder newLine = new StringBuilder();
            while (i < line.size()) {
                int cpt = 0;
                String currentModel = line.get(i);
                while (i < line.size() && (line.get(i)).equals(currentModel) && cpt < 9) {
                    cpt += 1;
                    i += 1;
                }
                newLine.append(cpt);
                newLine.append(currentModel);
            }
            mapList.add(newLine.toString());
        }
        return mapList;
    }

    public boolean IsPLayerAbsent(GameModel gameModel, Long playerId) {
        for (PlayerModel player : gameModel.getPlayers()) {
            if (player.getId().equals(playerId)) {
                return false;
            }
        }
        return true;
    }
}