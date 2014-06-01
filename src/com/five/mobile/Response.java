package com.five.mobile;

import java.util.*;
import java.net.HttpURLConnection;

public class Response
{
    /** Status code of the response */
    public final int statusCode;
    
    /** MIME type of the response */
    public final String contentType;

    /** The response content */
    public final String content;

    /** Response headers */
    public final Map<String, List<String>> headers;

    public Response(int statusCode, String contentType, String content, Map<String, List<String>> headers)
    {
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.content = content;
        this.headers = headers;
    }

    public Response(String content)
    {
        this(HttpURLConnection.HTTP_OK, "text/plain", content, new HashMap<String, List<String>>());
    }

    @Override
    public String toString()
    {
        return "statusCode=" + statusCode + ", contentType=" + contentType + ", content=" + content;
    }
}
