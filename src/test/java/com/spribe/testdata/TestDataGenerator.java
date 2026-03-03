package com.spribe.testdata;

import com.spribe.constants.PlayerConstraints;
import com.spribe.enums.Gender;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    private static final String LETTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String ALPHANUMERIC = LETTERS + DIGITS;

    public static String generateUniqueLogin() {
        return "login_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generateUniqueScreenName() {
        return "screen_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String generatePassword(int minLength, int maxLength, boolean requireDigit, boolean requireLetter) {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int length = random.nextInt(minLength, maxLength + 1);

        StringBuilder password = new StringBuilder(length);

        if (requireLetter) {
            password.append(LETTERS.charAt(random.nextInt(LETTERS.length())));
        }

        if (requireDigit) {
            password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }

        String allowedChars;
        if (requireDigit && requireLetter) {
            allowedChars = ALPHANUMERIC;
        } else if (requireDigit) {
            allowedChars = DIGITS;
        } else if (requireLetter) {
            allowedChars = LETTERS;
        } else {
            allowedChars = ALPHANUMERIC;
        }

        for (int i = password.length(); i < length; i++) {
            password.append(allowedChars.charAt(random.nextInt(allowedChars.length())));
        }

        return password.toString();
    }

    public static String generateValidPassword() {
        return TestDataGenerator.generatePassword(PlayerConstraints.PASSWORD_MIN_LENGTH,
                                                  PlayerConstraints.PASSWORD_MAX_LENGTH, true, true);
    }

    public static String generateTooShortPassword() {
        return TestDataGenerator.generatePassword(1, 6, true, true);
    }

    public static String generateTooLongPassword() {
        return TestDataGenerator.generatePassword(16, 32, true, true);
    }

    public static String generatePasswordWithoutDigits() {
        return TestDataGenerator.generatePassword(PlayerConstraints.PASSWORD_MIN_LENGTH,
                                                  PlayerConstraints.PASSWORD_MAX_LENGTH, false, true);
    }

    public static String generatePasswordWithoutLetters() {
        return TestDataGenerator.generatePassword(PlayerConstraints.PASSWORD_MIN_LENGTH,
                                                  PlayerConstraints.PASSWORD_MAX_LENGTH, true, false);
    }

    public static String generateValidAge() {
        return Integer.toString(ThreadLocalRandom.current().nextInt(PlayerConstraints.MIN_VALID_AGE,
                                                                    PlayerConstraints.MAX_VALID_AGE));
    }

    public static String generateValidGender() {
        return ThreadLocalRandom.current().nextBoolean() ? Gender.MALE.value() : Gender.FEMALE.value();
    }

}
