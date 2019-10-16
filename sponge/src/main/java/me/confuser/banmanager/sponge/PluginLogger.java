package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonLogger;
import org.slf4j.Logger;

public class PluginLogger implements CommonLogger {

    private final Logger logger;

    public PluginLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warn(msg);
    }

    public void severe(String msg) {
        logger.error(msg);
    }
}
