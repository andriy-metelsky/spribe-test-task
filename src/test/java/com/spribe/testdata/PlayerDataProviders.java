package com.spribe.testdata;

import com.spribe.constants.PlayerConstraints;
import com.spribe.enums.Gender;
import com.spribe.enums.Role;
import org.testng.annotations.DataProvider;

public class PlayerDataProviders {

    @DataProvider(name = "boundaryAges", parallel = true)
    public Object[][] provideBoundaryAges() {
        return new Object[][]{
                {String.valueOf(PlayerConstraints.MIN_VALID_AGE), "minimum valid age", 200},
                {String.valueOf(PlayerConstraints.MAX_VALID_AGE), "maximum valid age", 200},
                {String.valueOf(PlayerConstraints.MIN_AGE_EXCLUSIVE), "under minimum valid age", 400},
                {String.valueOf(PlayerConstraints.MAX_AGE_EXCLUSIVE), "over maximum valid age", 400},
        };
    }

    @DataProvider(name = "invalidPasswords", parallel = true)
    public Object[][] provideInvalidPasswords() {
        return new Object[][]{
                {TestDataGenerator.generateTooShortPassword(), "too short (6 chars, min 7)"},
                {TestDataGenerator.generateTooLongPassword(), "too long (16 chars, max 15)"},
                {TestDataGenerator.generatePasswordWithoutDigits(), "no digits"},
                {TestDataGenerator.generatePasswordWithoutLetters(), "no letters"}
        };
    }

    @DataProvider(name = "userRoles", parallel = true)
    public Object[][] provideUserRoles() {
        return new Object[][]{
                {Role.ADMIN.value()},
                {Role.USER.value()}
        };
    }

    @DataProvider(name = "genders", parallel = true)
    public Object[][] provideGenders() {
        return new Object[][]{
                {Gender.MALE.value()},
                {Gender.FEMALE.value()}
        };
    }

    @DataProvider(name = "invalidPlayerIds", parallel = true)
    public Object[][] invalidPlayerIds() {
        return new Object[][]{
                {"non-existent ID", 999999999L, 404},
                {"null ID", null, 400},
                {"negative ID", -1L, 400},
                {"zero ID", 0L, 400},
        };
    }
}