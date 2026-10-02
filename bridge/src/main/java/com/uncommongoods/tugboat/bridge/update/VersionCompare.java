// Copyright (c) 2025 Uncommon Goods LLC
// SPDX-License-Identifier: MPL-2.0

package com.uncommongoods.tugboat.bridge.update;

/** compare semantic version numbers against each other */
public final class VersionCompare {

    private VersionCompare() {}

    public static boolean isNewer(String candidate, String current) {
        return compare(candidate, current) > 0;
    }

    public static int compare(String a, String b) {
        String[] segmentsA = a.trim().split("\\.");
        String[] segmentsB = b.trim().split("\\.");
        int length = Math.max(segmentsA.length, segmentsB.length);
        for (int i = 0; i < length; i++) {
            long valueA = i < segmentsA.length ? numericPrefix(segmentsA[i]) : 0;
            long valueB = i < segmentsB.length ? numericPrefix(segmentsB[i]) : 0;
            int result = Long.compare(valueA, valueB);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private static long numericPrefix(String segment) {
        int end = 0;
        while (end < segment.length() && Character.isDigit(segment.charAt(end))) {
            end++;
        }
        if (end == 0) {
            return 0;
        }
        try {
            return Long.parseLong(segment.substring(0, end));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
