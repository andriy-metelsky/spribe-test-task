package com.spribe.clients;

import com.spribe.models.PlayerDeleteRequestDto;
import com.spribe.models.PlayerGetByPlayerIdRequestDto;
import com.spribe.models.PlayerUpdateRequestDto;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.Map;

public class PlayerClient extends BaseClient {

    private static final String PLAYER_BASE_PATH = "/player";

    private static final String CREATE_PLAYER_ENDPOINT = PLAYER_BASE_PATH + "/create/{editor}";
    private static final String DELETE_PLAYER_ENDPOINT = PLAYER_BASE_PATH + "/delete/{editor}";
    private static final String GET_PLAYER_ENDPOINT = PLAYER_BASE_PATH + "/get";
    private static final String GET_ALL_PLAYERS_ENDPOINT = GET_PLAYER_ENDPOINT + "/all";
    private static final String UPDATE_PLAYER_ENDPOINT = PLAYER_BASE_PATH + "/update/{editor}/{id}";

    /**
     * NOTE (BUG-001): API uses GET with query params to create a player.
     * This function mirrors current behavior to enable testing of the remaining endpoints of the controller under test.
     * Expected design: POST /players with JSON body and no password in responses.
     */
    public Response createPlayer(String editor, String age, String gender, String login, String password, String role,
                                 String screenName) {
        logger.info("Creating player with login={} and role={}, as editor={}", login, role, editor);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("age", age);
        queryParams.put("gender", gender);
        queryParams.put("login", login);
        if (password != null) {
            queryParams.put("password", password);
        }
        queryParams.put("role", role);
        queryParams.put("screenName", screenName);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("editor", editor);

        return get(CREATE_PLAYER_ENDPOINT, pathParams, queryParams);
    }

    public Response deletePlayer(String editor, PlayerDeleteRequestDto request) {
        logger.info("Deleting player with ID: {}, editor: {}", request.getPlayerId(), editor);
        return delete(DELETE_PLAYER_ENDPOINT, request, "editor", editor);
    }

    public Response deletePlayer(String editor, Long playerId) {
        PlayerDeleteRequestDto request = new PlayerDeleteRequestDto();
        request.setPlayerId(playerId);
        return deletePlayer(editor, request);
    }

    public Response getPlayerByPlayerId(PlayerGetByPlayerIdRequestDto request) {
        logger.info("Getting player with ID: {}", request.getPlayerId());
        return post(GET_PLAYER_ENDPOINT, request);
    }

    public Response getPlayerByPlayerId(Long playerId) {
        PlayerGetByPlayerIdRequestDto request = new PlayerGetByPlayerIdRequestDto();
        request.setPlayerId(playerId);
        return getPlayerByPlayerId(request);
    }

    public Response getAllPlayers() {
        logger.info("Getting all players");
        return get(GET_ALL_PLAYERS_ENDPOINT);
    }

    public Response updatePlayer(String editor, Long id, PlayerUpdateRequestDto request) {
        logger.info("Updating player with ID: {}, editor: {}", id, editor);

        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("editor", editor);
        pathParams.put("id", String.valueOf(id));

        return patch(UPDATE_PLAYER_ENDPOINT, request, pathParams);
    }
}
