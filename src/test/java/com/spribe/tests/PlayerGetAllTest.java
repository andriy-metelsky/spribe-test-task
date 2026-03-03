package com.spribe.tests;

import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.models.PlayerGetAllResponseDto;
import com.spribe.models.PlayerItem;
import com.spribe.testdata.TestDataGenerator;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Feature("Get All Players Endpoint")
public class PlayerGetAllTest extends BaseTest {

    // ========== POSITIVE TESTS ==========

    @Test(description = "Get all players")
    @Issue("BUG-007")
    public void testGetAllPlayers() {
        for (int i = 0; i < 10; i++) {
            String login = TestDataGenerator.generateUniqueLogin();
            Response createResponse = playerClient.createPlayer(SUPERVISOR_EDITOR, TestDataGenerator.generateValidAge(),
                                                                TestDataGenerator.generateValidGender(), login,
                                                                TestDataGenerator.generateValidPassword(),
                                                                Role.USER.value(),
                                                                TestDataGenerator.generateUniqueScreenName());
            Assert.assertEquals(createResponse.getStatusCode(), 200, "Unexpected status code for player creation");
        }

        Response playersResponse = playerClient.getAllPlayers();
        Assert.assertEquals(playersResponse.getStatusCode(), 200, "Unexpected status code for getting all players");
        List<PlayerItem> players = playersResponse.as(PlayerGetAllResponseDto.class).getPlayers();

        Assert.assertTrue(players.size() >= 11,
                          "Expected >= 11 players (10 new + supervisor), but got " + players.size());

        Set<Long> uniqueIds = players.stream().map(PlayerItem::getId).collect(Collectors.toSet());

        Assert.assertEquals(uniqueIds.size(), players.size(),
                            "GET /all returned duplicate player IDs. Unique IDs=" + uniqueIds.size() +
                                    ", returned items=" + players.size());
    }

    @Test(description = "Get all players returns correct field values")
    @Issue("BUG-008")
    @Severity(SeverityLevel.CRITICAL)
    public void testGetAllPlayerFieldValuesValidation() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String gender = TestDataGenerator.generateValidGender();
        String role = Role.USER.value();

        Response createResponse = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password, role,
                                                            screenName);
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Unexpected status code for player creation");

        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);

        Response playersResponse = playerClient.getAllPlayers();
        Assert.assertEquals(playersResponse.getStatusCode(), 200, "Unexpected status code for getting all players");
        List<PlayerItem> players = playersResponse.as(PlayerGetAllResponseDto.class).getPlayers();

        PlayerItem player = players.stream().filter(p -> created.getId().equals(p.getId())).findFirst().orElse(null);

        SoftAssert soft = new SoftAssert();
        soft.assertNotNull(player, "Player with id=" + created.getId() + " was not found in getAll response");
        soft.assertEquals(player.getRole(), role, "Unexpected role");
        soft.assertEquals(player.getGender(), gender, "Unexpected gender");
        soft.assertEquals(player.getAge(), Integer.valueOf(age), "Unexpected age");
        soft.assertEquals(player.getScreenName(), screenName, "Unexpected screenName");

        soft.assertAll();
    }
}
