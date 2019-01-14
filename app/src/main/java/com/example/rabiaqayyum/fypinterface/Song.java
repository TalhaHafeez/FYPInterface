package com.example.rabiaqayyum.fypinterface;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by RABIA QAYYUM on 4/26/2018.
 */


public class Song implements Serializable{

    private int id;
    private String title;

    private String songURl;
    private  String Artist;

    private float songDuration;
    private byte[] img;

    public Song(int songID, String songTitle, String url,float songDuration,byte[] img){
        id=songID;
        title=songTitle;
        this.songURl = url;
        this.songDuration=songDuration;
        this.img=img;
    }

    public void setImageResource(byte[]img)
    {
        this.img=img;
    }
    public int getID(){return id;}
    public String getTitle(){return title;}

    public String getSongUrl(){return songURl;}
    public float getSongDuration(){return songDuration;}
    public byte[] getImageResource()
    { return img; }

}