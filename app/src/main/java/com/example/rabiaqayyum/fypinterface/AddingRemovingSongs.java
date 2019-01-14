package com.example.rabiaqayyum.fypinterface;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.rabiaqayyum.fypinterface.Database.SongContract;
import com.example.rabiaqayyum.fypinterface.Database.SongDatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class AddingRemovingSongs
{
    static songsList  songList;
    Context context;
    String playlistName;
    SongDatabaseHelper sdh;

    public AddingRemovingSongs(Context context, songsList songList)
    {
        this.context=context;
        sdh=new SongDatabaseHelper(context);
        this.songList=songList;
    }
    public void addSongs(String playlistName)
    {
        this.playlistName=playlistName;
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        ((songsList)context).startActivityForResult(i,7);
        Log.e("playlist name",this.playlistName);
    }
    public void addToDatabaseAndListView(String thisTitle,String thisUrl,float thisDuration )
    {
        Boolean flagAdded=false;
        ArrayList<String> checkpaths=sdh.getSongPath(this.playlistName);
        for (int i=0;i<checkpaths.size();i++)
        {
            if(thisUrl.equals(checkpaths.get(i).toString()))
            {
                flagAdded=true;
                break;
            }
        }
        if(flagAdded==true)
        {
            Toast.makeText(context, "already added", Toast.LENGTH_SHORT).show();
        }
        else
        {
            sdh.insertSong(thisTitle,thisUrl,thisDuration,0);
            Log.e("playlist to add",this.playlistName);

            sdh.insertPlaylistsSongs_Assoc(thisTitle,this.playlistName);

            String songName="";
            Cursor res=sdh.getAllSongs("Happy");
            res.moveToFirst();
            int songIndex=res.getColumnIndex(SongContract.SongsTable.COLUMN_Song_Name);
            do
            {
                songName=songName+res.getString(songIndex)+" \n";
            }while(res.moveToNext());
            Log.e("song name",songName);
        }
    }
    public void removeSongs()
    {
        Intent myIntent = new Intent(context, songsList.class);
        context.startActivity(myIntent);
    }
}
