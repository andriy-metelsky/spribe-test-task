package com.spribe.tests;

import com.spribe.enums.Gender;
import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.testdata.PlayerDataProviders;
import com.spribe.testdata.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Feature("Create Player Endpoint")
public class PlayerCreateTest extends BaseTest {

    private String existingPlayerLogin;
    private String existingPlayerScreenName;

    @BeforeClass
    public void setupExistingPlayer() {
        existingPlayerLogin = TestDataGenerator.generateUniqueLogin();
        existingPlayerScreenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, Gender.MALE.value(), existingPlayerLogin,
                                                      password, Role.USER.value(), existingPlayerScreenName);

        Assert.assertEquals(response.getStatusCode(), 200, "Unexpected status code for player creation");
    }

    // ========== POSITIVE TESTS ==========

    @Test(dataProvider = "userRoles", dataProviderClass = PlayerDataProviders.class, description = "Create players " +
            "with valid data and different roles as supervisor ")
    @Issue("BUG-001")
    @Severity(SeverityLevel.BLOCKER)
    public void testCreatePlayersAllRolesAsSupervisor(String role) {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String gender = TestDataGenerator.generateValidGender();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password, role,
                                                      screenName);

        SoftAssert soft = new SoftAssert();
        soft.assertEquals(response.getStatusCode(), 200, "Unexpected status code for player creation");

        PlayerCreateResponseDto player = response.as(PlayerCreateResponseDto.class);
        soft.assertNotNull(player.getId(), "Player ID should not be null");
        soft.assertEquals(player.getLogin(), login, "Login should match");
        soft.assertEquals(player.getScreenName(), screenName, "ScreenName should match");
        soft.assertEquals(player.getAge(), Integer.valueOf(age), "Age should match");
        soft.assertEquals(player.getGender(), gender, "Gender should match");
        soft.assertEquals(player.getRole(), role, "Role should be " + role);
        soft.assertNull(player.getPassword(), "Password should not be returned in response");

        soft.assertAll();
    }

    @Test(dataProvider = "userRoles", dataProviderClass = PlayerDataProviders.class, description = "Create players " +
            "with valid data and different roles as admin")
    @Issue("BUG-001")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayersAllRolesAsAdmin(String role) {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String gender = TestDataGenerator.generateValidGender();

        Response response = playerClient.createPlayer(ADMIN_EDITOR, age, gender, login, password, role, screenName);
        Assert.assertEquals(response.getStatusCode(), 200, "Unexpected status code for player creation");

        SoftAssert soft = new SoftAssert();
        PlayerCreateResponseDto player = response.as(PlayerCreateResponseDto.class);
        soft.assertNotNull(player.getId(), "Player ID should not be null");
        soft.assertEquals(player.getLogin(), login, "Login should match");
        soft.assertEquals(player.getScreenName(), screenName, "ScreenName should match");
        soft.assertEquals(player.getAge(), Integer.valueOf(age), "Age should match");
        soft.assertEquals(player.getGender(), gender, "Gender should match");
        soft.assertEquals(player.getRole(), role, "Role should be " + role);
        soft.assertNull(player.getPassword(), "Password should not be returned in response");

        soft.assertAll();
    }

    @Test(dataProvider = "genders", dataProviderClass = PlayerDataProviders.class, description = "Create player " +
            "with valid gender values")
    @Issue("BUG-001")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePlayerAllGenders(String gender) {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password,
                                                      Role.USER.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 200,
                            "Unexpected status code for player creation with gender: " + gender);
    }

    @Test(dataProvider = "boundaryAges", dataProviderClass = PlayerDataProviders.class, description = "Create player " +
            "with age around boundary values")
    @Issue("BUG-001")
    @Issue("BUG-013")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePlayerAgeBoundariesValidation(String age, String description, int expectedStatus) {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String gender = TestDataGenerator.generateValidGender();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password,
                                                      Role.USER.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), expectedStatus,
                            String.format("Unexpected status code for player creation with age: %s (%s)", age,
                                          description));
    }

    // ========== NEGATIVE TESTS ==========

    @Test(dataProvider = "invalidPasswords", dataProviderClass = PlayerDataProviders.class,
            description = "Attempt to create player with invalid password formats")
    @Issue("BUG-002")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayerWithInvalidPassword(String password, String reason) {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();

        String age = TestDataGenerator.generateValidAge();
        String gender = TestDataGenerator.generateValidGender();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, gender, login, password,
                                                      Role.USER.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 400,
                            "Unexpected status code for player creation with invalid password: " + reason);
    }

    @Test(description = "Attempt to create supervisor role (should be forbidden)")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayerWithSupervisorRole() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, Gender.MALE.value(), login, password,
                                                      Role.SUPERVISOR.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 400,
                            "Unexpected status code for player creation with role: SUPERVISOR");
    }

    @Test(description = "Attempt to create player as user (should be forbidden)")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayerAsUser() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(USER_EDITOR, age, Gender.MALE.value(), login, password,
                                                      Role.USER.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 403,
                            "Unexpected status code for player creation as user");
    }

    @Test(description = "Attempt to create player with invalid role")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePlayerWithInvalidRole() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String invalidRole = "player";

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, Gender.FEMALE.value(), login, password,
                                                      invalidRole, screenName);

        Assert.assertEquals(response.getStatusCode(), 400,
                            "Unexpected status code for player creation with invalid role: " + invalidRole);
    }

    @Test(description = "Attempt to create player with invalid gender")
    @Issue("BUG-005")
    @Severity(SeverityLevel.NORMAL)
    public void testCreatePlayerWithInvalidGender() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();
        String invalidGender = "non-binary";

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age,
                                                      invalidGender, login, password,
                                                      Role.USER.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 400,
                            "Unexpected status code for player creation with invalid gender: " + invalidGender);
    }

    @Test(description = "Attempt to create player with duplicated login")
    @Issue("BUG-003")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayerWithDuplicatedLogin() {
        String login = existingPlayerLogin;
        String screenName = TestDataGenerator.generateUniqueScreenName();
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, Gender.FEMALE.value(), login, password,
                                                      Role.ADMIN.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 409,
                            "Unexpected status code for player creation with duplicated login");
    }

    @Test(description = "Attempt to create player with duplicated screenName")
    @Issue("BUG-004")
    @Severity(SeverityLevel.CRITICAL)
    public void testCreatePlayerWithDuplicatedScreenName() {
        String login = TestDataGenerator.generateUniqueLogin();
        String screenName = existingPlayerScreenName;
        String password = TestDataGenerator.generateValidPassword();
        String age = TestDataGenerator.generateValidAge();

        Response response = playerClient.createPlayer(SUPERVISOR_EDITOR, age, Gender.FEMALE.value(), login, password,
                                                      Role.ADMIN.value(), screenName);

        Assert.assertEquals(response.getStatusCode(), 409,
                            "Unexpected status code for player creation with duplicated screen name");
    }
}
