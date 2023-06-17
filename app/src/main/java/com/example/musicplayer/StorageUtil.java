package com.example.musicplayer;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.musicplayer.database.Songs;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class StorageUtil {

    private final String STORAGE = " com.example.musicplayer.STORAGE";
    private SharedPreferences preferences;
    private final Context context;

    public StorageUtil(Context context) {
        this.context = context;
    }



    public void storeAudio(ArrayList<Songs> arrayList) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("audioArrayList", json);
        editor.apply();
    }



    public ArrayList<Songs> loadAudio() {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("audioArrayList", null);
        return gson.fromJson(json, new TypeToken<List<Songs>>() {}.getType());
    }


    public void storeAudioIndexAndPostion(int index, int position) {
        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("audioIndex", index);
        editor.putInt("position", position);
        editor.apply();
    }


    public int[] loadAudioIndexAndPosition() {

        preferences = context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE);
        return new int[]{preferences.getInt("audioIndex", -1), preferences.getInt("position", 0)};//return -1 if no data found
    }

}


