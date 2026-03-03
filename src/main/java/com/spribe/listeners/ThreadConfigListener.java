package com.spribe.listeners;

import com.spribe.utils.ConfigManager;
import org.testng.IAlterSuiteListener;
import org.testng.xml.XmlSuite;

import java.util.List;

public class ThreadConfigListener implements IAlterSuiteListener {

    @Override
    public void alter(List<XmlSuite> suites) {
        int threadCount = Math.max(1, ConfigManager.getThreads());

        String parallelRaw = ConfigManager.getParallelMode();
        XmlSuite.ParallelMode parallelMode = parseParallel(parallelRaw);

        for (XmlSuite suite : suites) {
            suite.setParallel(parallelMode);
            suite.setThreadCount(threadCount);
            suite.setDataProviderThreadCount(threadCount);
        }
    }

    private XmlSuite.ParallelMode parseParallel(String value) {
        if (value == null) return XmlSuite.ParallelMode.NONE;
        switch (value.trim().toLowerCase()) {
            case "methods": return XmlSuite.ParallelMode.METHODS;
            case "classes": return XmlSuite.ParallelMode.CLASSES;
            case "tests": return XmlSuite.ParallelMode.TESTS;
            case "instances": return XmlSuite.ParallelMode.INSTANCES;
            case "none":
            default: return XmlSuite.ParallelMode.NONE;
        }
    }
}