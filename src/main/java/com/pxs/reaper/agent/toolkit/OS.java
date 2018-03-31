package com.pxs.reaper.agent.toolkit;

import java.util.logging.Logger;

/**
 * This class has operating system functions, like checking if this is the correct os to execute
 * some tests on, as some tests don't work on CentOs for some obscure reason.
 *
 * @author Michael Couck
 * @version 01.00
 * @since 28-03-2014
 */
public final class OS {

    private static Logger log = Logger.getLogger(OS.class.getSimpleName());

    private static String OS = os();

    public static boolean isOs(final String osName) {
        return OS.contains(osName);
    }

    public static String os() {
        String localOsName = System.getProperty("os.name");
        String localOsVersion = System.getProperty("os.version");
        String localOsArch = System.getProperty("os.arch");
        log.info("OS : " + localOsName + ", OS version : " + localOsVersion + " OS architecture : " + localOsArch);
        return localOsName.concat(" ").concat(localOsVersion).concat(" ").concat(localOsArch);
    }

}

