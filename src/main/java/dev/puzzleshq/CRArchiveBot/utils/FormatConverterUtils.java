package dev.puzzleshq.CRArchiveBot.utils;

import java.util.regex.Pattern;

public class FormatConverterUtils {

    static Pattern fileNameStripper = Pattern.compile("cosmic-reach-client-|cosmic-reach-server-|.jar");
    static Pattern devVersion = Pattern.compile("(?<=\\d)(?=[a-z])");
    static Pattern blueOrRed = Pattern.compile("blue|red");

    public static String convertFormat(String format) {
        String[] split = format.split("-");

        String versionBad = split[0];
        String version = null;
        String dev = null;
        String pre = null;
        String phase;

        if (blueOrRed.matcher(split[1]).find()) {
            versionBad += "." + split[1];
            phase = split[2];
        } else if (split.length == 3) {
            pre = split[1];
            phase = split[2];
        } else {
            phase = split[1];
        }

        if (devVersion.matcher(versionBad).find()) {
            String[] versionSplit = versionBad.split(devVersion.pattern());
            version = versionSplit[0];
            dev = versionSplit[1];
        } else {
            version = versionBad;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(version);
        sb.append("-");
        if (pre != null) {
            sb.append(pre);
            sb.append("-");
        }
        sb.append(phase);
        if (dev != null) {
            sb.append("+");
            sb.append(dev);
        }

        return sb.toString();
    }

    public static String convertFileNameFormat(String fileName) {
        String newFileName = fileName.replaceAll(fileNameStripper.pattern(), "");
        StringBuilder sb = new StringBuilder();
        if (fileName.contains("server")) {
            sb.append("cosmic-reach-server-");
        } else {
            sb.append("cosmic-reach-client-");
        }

        sb.append(convertFormat(newFileName));
        sb.append(".jar");
        return sb.toString();
    }

}
