package com.pxs.reaper.toolkit;

import org.apache.log4j.Logger;

/**
 * This class has operating system functions, like checking if this is the correct os to execute
 * some tests on, as some tests don't work on CentOs for some obscure reason.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2014
 */
public final class OS {

    private static final Logger LOGGER = Logger.getLogger(OS.class);

    private static String OS = os();

    public static boolean isOs(final String osName) {
        return OS.contains(osName);
    }

    public static String os() {
        String localOsName = System.getProperty("os.name");
        String localOsVersion = System.getProperty("os.version");
        String localOsArch = System.getProperty("os.arch");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Name of the OS: " + localOsName);
            LOGGER.info("Version of the OS: " + localOsVersion);
            LOGGER.info("Architecture of the OS: " + localOsArch);
        }
        return localOsName.concat(" ").concat(localOsVersion).concat(" ").concat(localOsArch);
    }

}

