package com.spribe.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlayerDeleteRequestDto {

    @JsonProperty("playerId")
    private Long playerId;

    public PlayerDeleteRequestDto() {
    }

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }
}
