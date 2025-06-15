package dev.puzzleshq.utils;

import java.util.regex.*;

public class Version implements Comparable<Version> {
    public final int major, minor, patch;
    public final Character patchLetter; // e.g., 'a', 'b', ..., or null
    public final String tag; // optional, e.g., "red", or null

    private static final Pattern VERSION_PATTERN = Pattern.compile(
            "^(\\d+)\\.(\\d+)\\.(\\d+)([a-z])?(?:-([a-z]+))?$"
    );

    public Version(String versionString) {
        Matcher m = VERSION_PATTERN.matcher(versionString);
        if (!m.matches()) {
            throw new IllegalArgumentException("Invalid version format: " + versionString);
        }

        major = Integer.parseInt(m.group(1));
        minor = Integer.parseInt(m.group(2));
        patch = Integer.parseInt(m.group(3));
        patchLetter = m.group(4) != null ? m.group(4).charAt(0) : null;
        tag = m.group(5); // may be null
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(major).append(".").append(minor).append(".").append(patch);
        if (patchLetter != null) sb.append(patchLetter);
        if (tag != null) sb.append("-").append(tag);
        return sb.toString();
    }

    @Override
    public int compareTo(Version other) {
        int cmp = Integer.compare(this.major, other.major);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.minor, other.minor);
        if (cmp != 0) return cmp;

        cmp = Integer.compare(this.patch, other.patch);
        if (cmp != 0) return cmp;

        if (this.patchLetter != null && other.patchLetter != null)
            cmp = Character.compare(this.patchLetter, other.patchLetter);
        else if (this.patchLetter != null)
            return 1;
        else if (other.patchLetter != null)
            return -1;

        // Compare tags by custom order
        cmp = compareTags(this.tag, other.tag);
        return cmp;
    }

    private int compareTags(String a, String b) {
        if (a == null && b == null) return 0;
        if (a == null) return -1;
        if (b == null) return 1;

        return Integer.compare(tagRank(a), tagRank(b));
    }

    private int tagRank(String tag) {
        return switch (tag) {
            case "red" -> 0;
            case "blue" -> 1;
            default -> 999; // unknown tags go last
        };
    }
}
