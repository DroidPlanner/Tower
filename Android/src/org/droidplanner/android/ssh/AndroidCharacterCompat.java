package org.droidplanner.android.ssh;

/*
 * Copyright (C) 2011 Steven Luo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.text.AndroidCharacter;

/**
 * Definitions related to android.text.AndroidCharacter
 */
public class AndroidCharacterCompat {
    public static final int EAST_ASIAN_WIDTH_NEUTRAL = 0;
    public static final int EAST_ASIAN_WIDTH_AMBIGUOUS = 1;
    public static final int EAST_ASIAN_WIDTH_HALF_WIDTH = 2;
    public static final int EAST_ASIAN_WIDTH_FULL_WIDTH = 3;
    public static final int EAST_ASIAN_WIDTH_NARROW = 4;
    public static final int EAST_ASIAN_WIDTH_WIDE = 5;

    private static class Api8OrLater {
        public static int getEastAsianWidth(char c) {
            return AndroidCharacter.getEastAsianWidth(c);
        }
    }
    public static int getEastAsianWidth(char c) {
        if (android.os.Build.VERSION.SDK_INT >= 8) {
            return Api8OrLater.getEastAsianWidth(c);
        } else {
            return EAST_ASIAN_WIDTH_NARROW;
        }
    }
}
