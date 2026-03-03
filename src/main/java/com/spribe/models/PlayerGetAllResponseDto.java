package com.spribe.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class PlayerGetAllResponseDto {

    @JsonProperty("players")
    private List<PlayerItem> players = new ArrayList<>();

    public PlayerGetAllResponseDto() {
    }

    public List<PlayerItem> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerItem> players) {
        this.players = players;
    }
}
