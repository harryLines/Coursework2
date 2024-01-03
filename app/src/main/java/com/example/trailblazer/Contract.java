package com.example.trailblazer;

import android.net.Uri;

public final class Contract {
    public static final String AUTHORITY = "com.example.TrailBlazer.provider";
    // Base content URI for accessing the provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

}
