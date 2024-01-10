package com.example.trailblazer;

import android.net.Uri;

/**
 * The Contract class defines constants for accessing a content provider in the TrailBlazer app.
 * It includes the authority and the base content URI.
 */
public final class Contract {
    /**
     * The authority for the content provider.
     */
    public static final String AUTHORITY = "com.example.TrailBlazer.provider";

    /**
     * The base content URI for accessing the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
}
