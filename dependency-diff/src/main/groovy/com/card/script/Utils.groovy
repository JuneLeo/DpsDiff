package com.card.script

class Utils {
    //

    static String getSpace(String space, int totalSpace, String displayName) {
        StringBuilder stringBuilder = new StringBuilder();
        int displayTotal = totalSpace;
        stringBuilder.append(displayName)
        int gap = displayTotal - displayName.length();
        if (gap > 0) {
            for (i in 0..<gap) {
                stringBuilder.append(space)
            }
        }
        return stringBuilder.toString()
    }

    static String getSpace(int totalSpace, String displayName) {
        return getSpace(" ", totalSpace, displayName)
    }
}