package com.spribe.tests;

import com.spribe.clients.PlayerClient;
import com.spribe.enums.Role;
import com.spribe.models.PlayerCreateResponseDto;
import com.spribe.testdata.TestDataGenerator;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import java.util.concurrent.atomic.AtomicBoolean;

public class BaseTest {

    protected static final Logger logger = LogManager.getLogger(BaseTest.class);
    protected static final String SUPERVISOR_EDITOR = "supervisor";
    protected static final Long SUPERVISOR_EDITOR_ID = 1L;
    private static final AtomicBoolean EDITORS_CREATED = new AtomicBoolean(false);
    protected static String ADMIN_EDITOR;
    protected static Long ADMIN_EDITOR_ID;
    protected static String USER_EDITOR;
    protected static Long USER_EDITOR_ID;
    protected PlayerClient playerClient;

    @BeforeClass(alwaysRun = true)
    public void setupClient() {
        playerClient = new PlayerClient();
    }

    @BeforeSuite(alwaysRun = true)
    public void setupEditors() {
        RestAssured.defaultParser = Parser.JSON;

        if (!EDITORS_CREATED.compareAndSet(false, true)) {
            return;
        }

        PlayerClient suiteClient = new PlayerClient();

        // Admin editor
        ADMIN_EDITOR = TestDataGenerator.generateUniqueLogin();
        Response adminCreate = suiteClient.createPlayer(SUPERVISOR_EDITOR, TestDataGenerator.generateValidAge(),
                                                        TestDataGenerator.generateValidGender(), ADMIN_EDITOR,
                                                        TestDataGenerator.generateValidPassword(),
                                                        Role.ADMIN.value(),
                                                        TestDataGenerator.generateUniqueScreenName());

        Assert.assertEquals(adminCreate.getStatusCode(), 200, "Failed to create ADMIN editor in suite setup");
        ADMIN_EDITOR_ID = adminCreate.as(PlayerCreateResponseDto.class).getId();

        // User editor
        USER_EDITOR = TestDataGenerator.generateUniqueLogin();
        Response userCreate = suiteClient.createPlayer(SUPERVISOR_EDITOR, TestDataGenerator.generateValidAge(),
                                                       TestDataGenerator.generateValidGender(), USER_EDITOR,
                                                       TestDataGenerator.generateValidPassword(),
                                                       Role.USER.value(), TestDataGenerator.generateUniqueScreenName());
        Assert.assertEquals(userCreate.getStatusCode(), 200, "Failed to create USER editor in suite setup");
        USER_EDITOR_ID = userCreate.as(PlayerCreateResponseDto.class).getId();

        logger.info("Suite editors created: ADMIN_EDITOR={}, USER_EDITOR={}", ADMIN_EDITOR, USER_EDITOR);
    }

    @AfterSuite(alwaysRun = true)
    public void teardown() {
        /*
         * NOTE:
         * The test environment is cleaned periodically by a DB cron job.
         * In case if there is no cleanup on the DB level, cleanup via API should be implemented.
         */
    }
}