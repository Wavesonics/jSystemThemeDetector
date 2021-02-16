package com.jthemedetecor.util;

import com.jthemedetecor.OsThemeDetector;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.software.os.OperatingSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OsInfo {

    private static final Logger logger = LoggerFactory.getLogger(OsThemeDetector.class);

    private static final PlatformEnum platformType;
    private static final String version;

    static {
        final SystemInfo systemInfo = new SystemInfo();
        final OperatingSystem osInfo = systemInfo.getOperatingSystem();
        final OperatingSystem.OSVersionInfo osVersionInfo = osInfo.getVersionInfo();

        platformType = SystemInfo.getCurrentPlatformEnum();
        version = osVersionInfo.getVersion();
    }

    public static boolean isWindows10OrLater() {
        return hasTypeAndVersionOrHigher(PlatformEnum.WINDOWS, "10");
    }

    public static boolean isLinux() {
        return hasType(PlatformEnum.LINUX);
    }

    public static boolean isMacOsMojaveOrLater() {
        return hasTypeAndVersionOrHigher(PlatformEnum.MACOSX, "10.14");
    }

    public static boolean isGnome() {
        return isLinux() && (
                        queryResultContains("echo $XDG_CURRENT_DESKTOP", "gnome") ||
                        queryResultContains("echo $XDG_DATA_DIRS | grep -Eo 'gnome'", "gnome") ||
                        queryResultContains("ps -e | grep -E -i \"gnome\"", "gnome")
        );
    }

    public static boolean hasType(PlatformEnum platformType) {
        return OsInfo.platformType.equals(platformType);
    }

    public static boolean hasVersionOrHigher(String version) {
        return parseVersion(OsInfo.version) >= parseVersion(version);
    }

    public static boolean hasTypeAndVersionOrHigher(PlatformEnum platformType, String version) {
        return hasType(platformType) && hasVersionOrHigher(version);
    }

    private static int parseVersion(String version) {
        try {
            return Integer.parseInt(version.replace(".", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static boolean queryResultContains(@NotNull String cmd, @NotNull String subResult) {
        return query(cmd).toLowerCase().contains(subResult);
    }

    @NotNull
    private static String query(@NotNull String cmd) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String actualReadLine;
                while ((actualReadLine = reader.readLine()) != null) {
                    if (stringBuilder.length() != 0)
                        stringBuilder.append('\n');
                    stringBuilder.append(actualReadLine);
                }
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            logger.error("Exception caught while querying the OS", e);
            return "";
        }
    }

    private OsInfo() {
    }
}
