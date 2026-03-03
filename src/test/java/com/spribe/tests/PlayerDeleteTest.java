package com.spribe.tests;

import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.testdata.PlayerDataProviders;
import com.spribe.testdata.TestDataGenerator;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Feature("Delete Player Endpoint")
public class PlayerDeleteTest extends BaseTest {

    @DataProvider(name = "deleteRoleMatrixWithIds", parallel = true)
    public Object[][] deleteRoleMatrixWithIds() {

        return new Object[][]{
                // Supervisor acting
                /**
                 * NOTE:
                 * The supervisor could not be deleted in accordance with the requirements.
                 * However, I decided not to attempt it since the test environment is shared
                 * with other candidates, and I do not have permission to restore it if
                 * anything goes wrong.
                */
                //{SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 403},
                {SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.ADMIN.value(), createPlayerAndReturnId(Role.ADMIN.value()), 204},
                {SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.USER.value(), createPlayerAndReturnId(Role.USER.value()), 204},

                // Admin acting
                //{ADMIN_EDITOR, Role.ADMIN.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 403},
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.ADMIN.value(), createPlayerAndReturnId(Role.ADMIN.value()), 204},
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.USER.value(), createPlayerAndReturnId(Role.USER.value()), 204},

                // User acting
                //{USER_EDITOR, Role.USER.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 403},
                {USER_EDITOR, Role.USER.value(), Role.ADMIN.value(), createPlayerAndReturnId(Role.ADMIN.value()), 403},
                {USER_EDITOR, Role.USER.value(), Role.USER.value(), createPlayerAndReturnId(Role.USER.value()), 403},
        };
    }

    @Test(dataProvider = "deleteRoleMatrixWithIds", description = "Delete player with different roles")
    @Issue("BUG-006")
    @Severity(SeverityLevel.CRITICAL)
    public void testDeletePlayer(String editorLogin, String editorRole, String targetRole, Long targetPlayerId,
                                 int expectedStatus) {
        Response deleteResponse = playerClient.deletePlayer(editorLogin, targetPlayerId);
        int actualStatus = deleteResponse.getStatusCode();

        String context = String.format("editorLogin=%s editorRole=%s targetRole=%s targetId=%s", editorLogin,
                                       editorRole, targetRole, targetPlayerId);
        Assert.assertEquals(actualStatus, expectedStatus, "Unexpected status code for player deletion: " + context);
    }

    @Test(dataProvider = "invalidPlayerIds", dataProviderClass = PlayerDataProviders.class, description = "Delete " +
            "player with invalid ID returns correct error status")
    @Issue("BUG-020")
    @Severity(SeverityLevel.NORMAL)
    public void testDeletePlayerWithInvalidId(String caseName, Long playerId, int expectedStatus) {

        Response deleteResponse = playerClient.deletePlayer(SUPERVISOR_EDITOR, playerId);

        Assert.assertEquals(deleteResponse.getStatusCode(), expectedStatus,
                            String.format("Unexpected status for deleting a player with: %s (playerId=%s)", caseName,
                                          playerId));
    }

    private Long createPlayerAndReturnId(String role) {
        Response createResponse = playerClient.createPlayer(SUPERVISOR_EDITOR, TestDataGenerator.generateValidAge(),
                                                            TestDataGenerator.generateValidGender(),
                                                            TestDataGenerator.generateUniqueLogin(),
                                                            TestDataGenerator.generateValidPassword(), role,
                                                            TestDataGenerator.generateUniqueScreenName());

        Assert.assertEquals(createResponse.getStatusCode(), 200,
                            "Unexpected status code for player creation with role:" + role);
        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);
        Assert.assertNotNull(created.getId(), "Created player id must not be null for role: " + role);

        return created.getId();
    }
}