package com.example.rabiaqayyum.fypinterface;

import android.view.View;

/**
 * Created by RABIA QAYYUM on 4/7/2018.
 */

public interface ClickListener {
    public void itemClicked(View view, int position,String playlistName);
    public void popUpClicked(View view,int listPosition);
    public void favouriteButton(View view,int listPosition,String playlistName,boolean fav);
}
