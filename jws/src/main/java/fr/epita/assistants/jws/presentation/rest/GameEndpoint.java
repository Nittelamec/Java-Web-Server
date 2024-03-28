package fr.epita.assistants.jws.presentation.rest;

import fr.epita.assistants.jws.domain.service.GameService;
import fr.epita.assistants.jws.domain.service.PlayerService;
import fr.epita.assistants.jws.presentation.rest.request.CreateGameRequest;
import fr.epita.assistants.jws.presentation.rest.request.JoinGameRequest;
import fr.epita.assistants.jws.presentation.rest.request.MovePlayerRequest;
import fr.epita.assistants.jws.presentation.rest.response.GameDetailResponse;
import fr.epita.assistants.jws.presentation.rest.response.GameListResponse;
import fr.epita.assistants.jws.utils.MyPair;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class GameEndpoint {
    @Inject
    GameService gameService;
    @Inject
    PlayerService playerService;

    @GET
    @Path("games")
    @Produces(MediaType.APPLICATION_JSON)
    public Response gameListEndpoint() {
        List<GameListResponse> games = gameService.getGameList();
        return Response.ok(games).build();
    }

    @POST
    @Path("games")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGameEndpoint(CreateGameRequest request) {
        if (request == null || request.getName() == null || request.getName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        GameDetailResponse response = gameService.createGame(request.getName());
        return Response.ok(response).build();
    }

    @GET
    @Path("games/{gameId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response gameInfoEndpoint(@PathParam(value = "gameId") Long gameId) {
        if (gameId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        GameDetailResponse response = gameService.getGameInfo(gameId);
        if (response == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(response).build();
    }

    @POST
    @Path("games/{gameId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response joinGameEndpoint(@PathParam(value = "gameId") Long gameId, JoinGameRequest joinGameRequest) {
        if (gameId == null || joinGameRequest == null || joinGameRequest.getName() == null || joinGameRequest.getName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MyPair<Integer, GameDetailResponse> response = gameService.joinGame(gameId, joinGameRequest.getName());
        if (response.getA() == 400) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (response.getA() == 404) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(response.getB()).build();
        }
    }

    @PATCH
    @Path("games/{gameId}/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startGameEndpoint(@PathParam(value = "gameId") Long gameId) {
        if (gameId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MyPair<Integer, GameDetailResponse> response = gameService.startGame(gameId);
        if (response.getA() == 404) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (response.getA() == 400) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            return Response.ok(response.getB()).build();
        }
    }

    @POST
    @Path("games/{gameId}/players/{playerId}/move")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response movePlayerEndpoint(@PathParam(value = "gameId") Long gameId, @PathParam(value = "playerId") Long playerId, MovePlayerRequest request) {
        if (request == null || request.getPosX() == null || request.getPosY() == null || gameId == null || playerId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MyPair<Integer, GameDetailResponse> response = playerService.move(request.getPosX(), request.getPosY(), gameId, playerId);
        if (response.getA() == 400) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else if (response.getA() == 404) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (response.getA() == 429) {
            return Response.status(Response.Status.TOO_MANY_REQUESTS).build();
        } else {
            return Response.ok(response.getB()).build();
        }
    }
    @POST
    @Path("games/{gameId}/players/{playerId}/bomb")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response placeBombEndpoint(@PathParam(value = "gameId") Long gameId, @PathParam(value = "playerId") Long playerId, MovePlayerRequest request) {
        if (request == null || request.getPosX() == null || request.getPosY() == null || gameId == null || playerId == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        MyPair<Integer, GameDetailResponse> response = playerService.putBomb(request.getPosX(), request.getPosY(), gameId, playerId);
        if (response.getB() == null) {
            if (response.getA() == 400) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            } else if (response.getA() == 404) {
                return Response.status(Response.Status.NOT_FOUND).build();
            } else if (response.getA() == 429) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS).build();
            }
            else
                return null;
        } else {
            return Response.ok(response.getB()).build();
        }
    }
}