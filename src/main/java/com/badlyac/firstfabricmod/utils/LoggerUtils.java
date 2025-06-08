package com.badlyac.firstfabricmod.utils;

import com.badlyac.firstfabricmod.FirstFabricMod;
import org.slf4j.LoggerFactory;

public class LoggerUtils {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(FirstFabricMod.MOD_ID);

    public static void info(String s) {
        logger.info(s);
    }
}
