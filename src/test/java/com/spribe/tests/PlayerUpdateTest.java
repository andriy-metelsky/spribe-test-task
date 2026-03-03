package com.spribe.tests;

import com.spribe.enums.Gender;
import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.models.PlayerUpdateRequestDto;
import com.spribe.models.PlayerUpdateResponseDto;
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
import org.testng.asserts.SoftAssert;

@Feature("Update Player Endpoint")
public class PlayerUpdateTest extends BaseTest {

    // ========== POSITIVE TESTS ==========

    @DataProvider(name = "updateRoleMatrixWithIds", parallel = true)
    public Object[][] updateRoleMatrixWithIds() {
        Long adminIdToUpdate = createPlayerAndReturnId(Role.ADMIN.value());
        Long userIdToUpdate = createPlayerAndReturnId(Role.USER.value());
        return new Object[][]{
                // Supervisor acting - can update any role except supervisor
                {SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 200},
                {SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.ADMIN.value(), adminIdToUpdate, 200},
                {SUPERVISOR_EDITOR, Role.SUPERVISOR.value(), Role.USER.value(), userIdToUpdate, 200},

                // Admin acting - can update user and admin (including self)
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 403},
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.ADMIN.value(), adminIdToUpdate, 200},
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.ADMIN.value(), ADMIN_EDITOR_ID, 200},
                {ADMIN_EDITOR, Role.ADMIN.value(), Role.USER.value(), userIdToUpdate, 200},

                // User acting - can only update self
                {USER_EDITOR, Role.USER.value(), Role.SUPERVISOR.value(), SUPERVISOR_EDITOR_ID, 403},
                {USER_EDITOR, Role.USER.value(), Role.ADMIN.value(), adminIdToUpdate, 403},
                {USER_EDITOR, Role.USER.value(), Role.USER.value(), userIdToUpdate, 403},
                {USER_EDITOR, Role.USER.value(), Role.USER.value(), USER_EDITOR_ID, 200}
        };
    }

    @Test(dataProvider = "updateRoleMatrixWithIds", description = "Update player age with different role permissions")
    @Issue("BUG-012")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerAgeByRole(String editorLogin, String editorRole, String targetRole, Long targetPlayerId,
                                          int expectedStatus) {
        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setAge(30);

        Response updateResponse = playerClient.updatePlayer(editorLogin, targetPlayerId, updateRequest);
        int actualStatus = updateResponse.getStatusCode();

        String context = String.format("editorLogin=%s editorRole=%s targetRole=%s targetId=%s",
                                       editorLogin, editorRole, targetRole, targetPlayerId);
        Assert.assertEquals(actualStatus, expectedStatus, "Unexpected status code for player update: " + context);
    }


    @Test(description = "Admin can update user - multiple fields")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerAsAdminMultipleFields() {
        Response createResponse = playerClient.createPlayer(
                SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                TestDataGenerator.generateUniqueLogin(), TestDataGenerator.generateValidPassword(),
                Role.USER.value(), TestDataGenerator.generateUniqueScreenName()
        );
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);

        Integer new_age = 35;
        String new_gender = Gender.FEMALE.value();
        String new_login = TestDataGenerator.generateUniqueLogin();
        String new_screenName = TestDataGenerator.generateUniqueScreenName();
        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setAge(new_age);
        updateRequest.setGender(new_gender);
        updateRequest.setLogin(new_login);
        updateRequest.setScreenName(new_screenName);

        Response updateResponse = playerClient.updatePlayer(ADMIN_EDITOR, created.getId(), updateRequest);
        Assert.assertEquals(updateResponse.getStatusCode(), 200, "Unexpected status code for player creation");

        SoftAssert soft = new SoftAssert();
        PlayerUpdateResponseDto updated = updateResponse.as(PlayerUpdateResponseDto.class);
        soft.assertEquals(updated.getId(), created.getId(), "ID should remain the same");
        soft.assertEquals(updated.getAge(), new_age, "Age should be updated");
        soft.assertEquals(updated.getGender(), new_gender, "Gender should be updated");
        soft.assertEquals(updated.getLogin(), new_login, "Login should be updated");
        soft.assertEquals(updated.getScreenName(), new_screenName, "ScreenName should be updated");

        soft.assertAll();
    }

    // ========== NEGATIVE TESTS ==========

    @Test(dataProvider = "boundaryAges", dataProviderClass = PlayerDataProviders.class, description = "Update player " +
            "with age around boundary values")
    @Issue("BUG-014")
    @Severity(SeverityLevel.CRITICAL)
    public void testPlayerAgeBoundariesValidation(String age, String description, int expectedStatus) {
        Response createResponse = playerClient.createPlayer(
                SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                TestDataGenerator.generateUniqueLogin(), TestDataGenerator.generateValidPassword(),
                Role.USER.value(), TestDataGenerator.generateUniqueScreenName()
        );
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);

        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setAge(Integer.parseInt(age));

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, created.getId(), updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), expectedStatus,
                            String.format("Unexpected status code for player update with age: %s (%s)", age,
                                          description));
    }

    @Test(description = "Update with invalid gender")
    @Issue("BUG-015")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerWithInvalidGender() {
        Response createResponse = playerClient.createPlayer(
                SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                TestDataGenerator.generateUniqueLogin(), TestDataGenerator.generateValidPassword(),
                Role.USER.value(), TestDataGenerator.generateUniqueScreenName()
        );
        Assert.assertEquals(createResponse.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);

        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setGender("other");

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, created.getId(), updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), 400,
                            "Unexpected status code for player update with invalid gender");
    }

    @Test(description = "Update with duplicate screenName")
    @Issue("BUG-016")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerWithDuplicateScreenName() {
        String screenName1 = TestDataGenerator.generateUniqueScreenName();
        Response createResponse1 = playerClient.createPlayer(
                SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                TestDataGenerator.generateUniqueLogin(), TestDataGenerator.generateValidPassword(),
                Role.USER.value(), screenName1);

        Assert.assertEquals(createResponse1.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created1 = createResponse1.as(PlayerCreateResponseDto.class);

        String screenName2 = TestDataGenerator.generateUniqueScreenName();
        Response createResponse2 = playerClient.createPlayer(
                SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                TestDataGenerator.generateUniqueLogin(), TestDataGenerator.generateValidPassword(),
                Role.USER.value(), screenName2);
        Assert.assertEquals(createResponse2.getStatusCode(), 200, "Unexpected status for player creation");

        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setScreenName(screenName2);

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, created1.getId(), updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), 409, "Unexpected status for player updating with duplicated" +
                " screen name");
    }

    @Test(description = "Update with duplicate login")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerWithDuplicateLogin() {
        String login1 = TestDataGenerator.generateUniqueLogin();
        Response createResponse1 = playerClient.createPlayer(SUPERVISOR_EDITOR, "25", Gender.MALE.value(), login1,
                                                             TestDataGenerator.generateValidPassword(),
                                                             Role.USER.value(),
                                                             TestDataGenerator.generateUniqueScreenName());

        Assert.assertEquals(createResponse1.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created1 = createResponse1.as(PlayerCreateResponseDto.class);

        String login2 = TestDataGenerator.generateUniqueLogin();
        Response createResponse2 = playerClient.createPlayer(SUPERVISOR_EDITOR, "25", Gender.MALE.value(), login2,
                                                             TestDataGenerator.generateValidPassword(),
                                                             Role.USER.value(),
                                                             TestDataGenerator.generateUniqueScreenName());
        Assert.assertEquals(createResponse2.getStatusCode(), 200, "Unexpected status for player creation");

        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setLogin(login2);

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, created1.getId(), updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), 409, "Unexpected status for player updating with " +
                "duplicated screen name");
    }


    @Test(dataProvider = "invalidPlayerIds", dataProviderClass = PlayerDataProviders.class, description = "Update " +
            "player with invalid ID returns correct error status")
    @Issue("BUG-022")
    @Severity(SeverityLevel.NORMAL)
    public void testUpdatePlayerWithInvalidId(String caseName, Long playerId, int expectedStatus) {
        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setAge(30);

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, playerId, updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), expectedStatus,
                            String.format("Unexpected status for updating a player with: %s (playerId=%s)", caseName,
                                          playerId));
    }

    @Test(dataProvider = "invalidPasswords", dataProviderClass = PlayerDataProviders.class,
            description = "Update with invalid password format")
    @Issue("BUG-017")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdatePlayerWithInvalidPassword(String password, String reason) {
        Response createResponse1 = playerClient.createPlayer(SUPERVISOR_EDITOR, "25", Gender.MALE.value(),
                                                             TestDataGenerator.generateUniqueLogin(),
                                                             TestDataGenerator.generateValidPassword(),
                                                             Role.USER.value(),
                                                             TestDataGenerator.generateUniqueScreenName());

        Assert.assertEquals(createResponse1.getStatusCode(), 200, "Unexpected status for player creation");
        PlayerCreateResponseDto created = createResponse1.as(PlayerCreateResponseDto.class);

        PlayerUpdateRequestDto updateRequest = new PlayerUpdateRequestDto();
        updateRequest.setPassword(password);

        Response updateResponse = playerClient.updatePlayer(SUPERVISOR_EDITOR, created.getId(), updateRequest);

        Assert.assertEquals(updateResponse.getStatusCode(), 400,
                            "Unexpected status code for player update with invalid password: " + reason);
    }

    private Long createPlayerAndReturnId(String role) {
        Response createResponse = playerClient.createPlayer(SUPERVISOR_EDITOR, TestDataGenerator.generateValidAge(),
                                                            TestDataGenerator.generateValidGender(),
                                                            TestDataGenerator.generateUniqueLogin(),
                                                            TestDataGenerator.generateValidPassword(), role,
                                                            TestDataGenerator.generateUniqueScreenName());

        Assert.assertEquals(createResponse.getStatusCode(), 200,
                            "Unexpected status code for player creation with role: " + role);
        PlayerCreateResponseDto created = createResponse.as(PlayerCreateResponseDto.class);
        Assert.assertNotNull(created.getId(), "Created player id must not be null for role=" + role);

        return created.getId();
    }
}
