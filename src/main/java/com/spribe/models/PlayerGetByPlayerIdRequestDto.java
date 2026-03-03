package com.spribe.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerGetByPlayerIdRequestDto {

    @JsonProperty("playerId")
    private Long playerId;

    public PlayerGetByPlayerIdRequestDto() {
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}
