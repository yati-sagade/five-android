package com.five.mobile;

import java.net.URL;

public final class Constants
{
    public static final String FIVE_PROTO = "http";

    public static final String FIVE_HOST = "ec2-54-186-156-59.us-west-2.compute.amazonaws.com";
    public static final int FIVE_PORT = 8000;

    public static final String FIVE_SHARED_PREFS_NAME = "fiveprefs";

    public static final String SESSION_ACTIVE_KEY = "sessionactive";

    // The server will send this cookie which should be sent back on every
    // post to prevent CSRF.
    public static final String CSRF_COOKIE_NAME = "csrftoken";

    public static final int CMD_LOGOUT = 0;

    public static final int CMD_DETAILS = 1;

    public static final int CMD_LIST_NEARBY = 2;

    public static final int CMD_CHECK_IN = 3;

    public static final String GOOGLE_MAPS_API_KEY = "AIzaSyDqTrReKAkxC7ApJf6gD6u1kTo_NeiFX_Y";
}
