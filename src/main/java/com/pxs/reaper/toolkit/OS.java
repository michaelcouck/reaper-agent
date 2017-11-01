package com.pxs.reaper.toolkit;

import lombok.extern.slf4j.Slf4j;

/**
 * This class has operating system functions, like checking if this is the correct os to execute
 * some tests on, as some tests don't work on CentOs for some obscure reason.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2014
 */
@Slf4j
public final class OS {

    private static String OS = os();

    public static boolean isOs(final String osName) {
        return OS.contains(osName);
    }

    public static String os() {
        String localOsName = System.getProperty("os.name");
        String localOsVersion = System.getProperty("os.version");
        String localOsArch = System.getProperty("os.arch");
        if (log.isDebugEnabled()) {
            log.info("Name of the OS: " + localOsName);
            log.info("Version of the OS: " + localOsVersion);
            log.info("Architecture of the OS: " + localOsArch);
        }
        return localOsName.concat(" ").concat(localOsVersion).concat(" ").concat(localOsArch);
    }

}

