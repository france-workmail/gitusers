package com.snarfapps.gitusers;

public class Constants {
    /**
     * Color matrix that flips the components (<code>-1.0f * c + 255 = 255 - c</code>)
     * and keeps the alpha intact.
     */
    public static final float[] NEGATIVE = {
            -1.0f,     0,     0,    0, 255, // r
            0, -1.0f,     0,    0, 255, // g
            0,     0, -1.0f,    0, 255, // b
            0,     0,     0, 1.0f,   0  // a
    };

    public static final String GET_USERS_URL = "https://api.github.com/users?since=";
}
