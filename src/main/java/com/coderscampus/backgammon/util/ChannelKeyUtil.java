package com.coderscampus.backgammon.util;

import java.util.Locale;
import java.util.regex.Pattern;

public final class ChannelKeyUtil {

    private static final Pattern ILLEGAL_CHARS = Pattern.compile("[^a-z0-9_-]");

    private ChannelKeyUtil() {
    }

    public static String sanitize(String rawKey) {
        if (rawKey == null) {
            return null;
        }
        String trimmed = rawKey.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toLowerCase(Locale.ENGLISH);
        return ILLEGAL_CHARS.matcher(normalized).replaceAll("_");
    }
}
