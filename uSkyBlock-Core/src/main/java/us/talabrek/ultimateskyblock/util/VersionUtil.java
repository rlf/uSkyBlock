package us.talabrek.ultimateskyblock.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for versions
 */
public enum VersionUtil {;
    private static final Pattern VERSION_PATTERN = Pattern.compile("v?(?<major>[0-9]+)[\\._](?<minor>[0-9]+)(?:[\\._](?<micro>[0-9]+))?(?<sub>.*)");
    public static Version getVersion(String versionString) {
        Matcher m = VERSION_PATTERN.matcher(versionString);
        if (m.matches()) {
            int major = Integer.parseInt(m.group("major"));
            int minor = m.group("minor") != null ? Integer.parseInt(m.group("minor")) : 0;
            int micro = m.group("micro") != null ? Integer.parseInt(m.group("micro")) : 0;
            return new Version(major, minor, micro, m.group("sub"));
        }
        return new Version(0,0,0, null);
    }

    public static class Version {
        private int major;
        private int minor;
        private int micro;
        private String sub;

        public Version(int major, int minor, int micro, String sub) {
            this.major = major;
            this.minor = minor;
            this.micro = micro;
            this.sub = sub;
        }

        public int getMajor() {
            return major;
        }

        public int getMinor() {
            return minor;
        }

        public int getMicro() {
            return micro;
        }

        public String getSub() {
            return sub;
        }

        public boolean isGTE(String version) {
            Version other = getVersion(version);
            return major > other.major ||
                    major >= other.major && minor > other.minor ||
                    major >= other.major && minor >= other.minor && micro >= other.micro;
        }

        public boolean isLT(String version) {
            Version other = getVersion(version);
            return major < other.major ||
                    major <= other.major && minor < other.minor ||
                    major <= other.major && minor <= other.minor && micro < other.micro;
        }

        @Override
        public String toString() {
            return "v" + major + "." + minor + "." + micro + "-" + sub;
        }
    }
}
