package com.example.trailblazer.data;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import android.content.Context;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class PlacesModule {

    @Provides
    public PlacesClient providePlacesClient(@ApplicationContext Context context) {
        Places.initialize(context, "AIzaSyCrKsxTguyZRaVlFrC9ADqGZbmLKyxctWs");
        return Places.createClient(context);
    }
}
