package com.spribe.listeners;

import io.qameta.allure.Allure;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

@Plugin(name = "AllureLogAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE)
public class AllureLogAppender extends AbstractAppender {

    private static final ThreadLocal<StringBuilder> LOG_COLLECTOR = ThreadLocal.withInitial(StringBuilder::new);

    protected AllureLogAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, false, null);
    }

    @PluginFactory
    public static AllureLogAppender createAppender(@PluginAttribute("name") String name,
                                                   @PluginElement("Layout") Layout<? extends Serializable> layout,
                                                   @PluginElement("Filter") Filter filter) {

        return new AllureLogAppender(name, filter, layout != null ? layout : PatternLayout.createDefaultLayout());
    }

    public static void attachLogsToAllure() {
        StringBuilder logs = LOG_COLLECTOR.get();
        if (!logs.isEmpty()) {
            Allure.addAttachment("Execution Logs", "text/plain",
                                 logs.toString(), ".log");
            logs.setLength(0);
        }
    }

    @Override
    public void append(LogEvent event) {
        LOG_COLLECTOR.get().append(getLayout().toSerializable(event));
    }
}