package com.spribe.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        logger.info("STARTING TEST: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        logger.info("TEST PASSED: {}.{} (Duration: {}ms)", result.getTestClass().getName(),
                    result.getMethod().getMethodName(), result.getEndMillis() - result.getStartMillis());
        AllureLogAppender.attachLogsToAllure();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        logger.error("TEST FAILED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName(),
                     result.getThrowable());
        AllureLogAppender.attachLogsToAllure();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        logger.warn("TEST SKIPPED: {}.{}", result.getTestClass().getName(), result.getMethod().getMethodName());
        AllureLogAppender.attachLogsToAllure();
    }
}