package com.spribe.tests;

import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.models.PlayerGetByPlayerIdResponseDto;
import com.spribe.testdata.PlayerDataProviders;
import com.spribe.testdata.TestDataGenerator;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Feature("Get Player By ID Endpoint")
public class PlayerGetTest extends BaseTest {

    // ========== POSITIVE TESTS ==========

    @Test(description = "Get existing player by valid ID")
    @Issue("BUG-021")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetPlayerById() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String gender = TestDataGenerator.generateValidGender();

        Response createdResponse = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password,
                                                             Role.USER.value(), screenName);
        Assert.assertEquals(createdResponse.getStatusCode(), 200, "Unexpected status code for player creation");
        PlayerCreateResponseDto createdPlayer = createdResponse.as(PlayerCreateResponseDto.class);

        Response getResponse = playerClient.getPlayerByPlayerId(createdPlayer.getId());
        PlayerGetByPlayerIdResponseDto player = getResponse.as(PlayerGetByPlayerIdResponseDto.class);

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(getResponse.getStatusCode(), 200, "Status code should be 200 OK");
        soft.assertEquals(player.getId(), createdPlayer.getId(), "Player ID should match");
        soft.assertEquals(player.getLogin(), login, "Player login should match");
        soft.assertEquals(player.getScreenName(), screenName, "Player screen name should match");
        soft.assertEquals(player.getAge(), Integer.parseInt(age), "Player age should match");
        soft.assertEquals(player.getGender(), gender, "Player gender should match");
        soft.assertEquals(player.getRole(), Role.USER.value(), "Player role should be USER");
        soft.assertNull(player.getPassword(),"Password value must not be returned");

        soft.assertAll();

    }

    //========== NEGATIVE TESTS ==========

    @Test(dataProvider = "invalidPlayerIds", dataProviderClass = PlayerDataProviders.class, description = "Get player" +
            " with invalid ID returns correct error status")
    @Issue("BUG-019")
    @Severity(SeverityLevel.NORMAL)
    public void testGetPlayerWithInvalidId(String caseName, Long playerId, int expectedStatus) {

        Response getResponse = playerClient.getPlayerByPlayerId(playerId);

        Assert.assertEquals(getResponse.getStatusCode(), expectedStatus,
                            String.format("Unexpected status for getting a player with: %s (playerId=%s)", caseName,
                                          playerId));
    }
}
