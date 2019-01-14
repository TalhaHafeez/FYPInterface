package com.example.rabiaqayyum.fypinterface;

import android.app.ActionBar;
import android.app.Activity;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.example.rabiaqayyum.fypinterface.Database.SongContract;
import com.example.rabiaqayyum.fypinterface.Database.SongDatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.security.AccessController.getContext;

/**
 * Created by RABIA QAYYUM on 4/7/2018.
 */

public class songsList extends ActionBarActivity implements  ClickListener {
    public static ArrayList<Song> songList;
   // private ArrayList<Images> imageList;
   public static final String Broadcast_PLAY_NEW_AUDIO = "com.example.rabiaqayyum.fypinterface.PlayNewAudio";
    RecyclerView list;
    TextView noSong;
    Button addSong;
    FloatingActionButton fabAdd;
    Toolbar toolbar;





    static SongDatabaseHelper sdh;
    AddingRemovingSongs ars;
    Cursor res;
    boolean serviceBound = false;
    private MediaPlayerService player;
   // SharedPreference sharedPrefe`rence;

    ArrayList<String> songs;

    String playlistName;
    int songId;
    String songName,songPath;
    float songDuration;

    customListAdaptor adapter;
    Fragment fragment;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songs_listview);

        songs=new ArrayList<>();
        sdh=new SongDatabaseHelper(this);
        ars=new AddingRemovingSongs(this,new songsList());
        fabAdd= (FloatingActionButton) findViewById(R.id.fab2);
     //   sharedPreference = new SharedPreference();

        list = (RecyclerView) findViewById(R.id.android_list);
        noSong=(TextView) findViewById(R.id.noSong);
        addSong=(Button)  findViewById(R.id.addSongs);


        toolbar = (Toolbar) findViewById(R.id.toolbarlist);
        setSupportActionBar(toolbar);






        LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
        list.setLayoutManager(llm);
        songList = new ArrayList<Song>();

        Intent myIntent=getIntent();
        Bundle bundle=myIntent.getExtras();
        playlistName=bundle.getString("playlistName");
        getSupportActionBar().setTitle(playlistName);


        Boolean isSongs=getSongList(playlistName);
        adapter = new customListAdaptor(this,songList,playlistName);
        adapter.setClickListener(this);
        list.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ars.addSongs(playlistName);
                adapter.notifyDataSetChanged();
                //adapter.notifyDataSetChanged();
            }
        });
        if(isSongs==false)
        {
            noSong.setVisibility(View.VISIBLE);
            addSong.setVisibility(View.VISIBLE);
            addSong.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ars.addSongs(playlistName);
                    adapter.notifyDataSetChanged();
                }
            });
           // adapter.notifyDataSetChanged();
        }
        else
        {
          //  adapter.notifyDataSetChanged();
            Collections.sort(songList, new Comparator<Song>() {
                public int compare(Song a, Song b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });

            for(int i=0;i<songList.size();i++)
            {
                Log.e("song id",songList.get(i).getID()+"");
                Log.e("song name",songList.get(i).getTitle());
            }
        }
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            player = binder.getService();
            serviceBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };


    public boolean getSongList(String playlistName)
    {
       Log.e("playlistname",playlistName.toString());
            res = sdh.getAllSongs(playlistName);
        if(res.getCount()<=0&& res.moveToFirst()==false)
        {
            Log.e("cursor","null");
            return false;
        }
        else
        {
            res.moveToFirst();

            do {
                int songIdIndex=res.getColumnIndex(SongContract.SongsTable.COLUMN_Song_ID);
                int songNameIndex=res.getColumnIndex(SongContract.SongsTable.COLUMN_Song_Name);
                int songPathIndex=res.getColumnIndex(SongContract.SongsTable.COLUMN_Song_Path);
                int songDurationIndex=res.getColumnIndex(SongContract.SongsTable.COLUMN_Song_duration);

                songId=res.getInt(songIdIndex);
                songName=res.getString(songNameIndex);
                songPath=res.getString(songPathIndex);
                songDuration=res.getFloat(songDurationIndex);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(songPath);
                byte[] coverBytes = retriever.getEmbeddedPicture();

                if (coverBytes==null)
                {
                    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.default_artwork);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    coverBytes = stream.toByteArray();
                }
                songList.add(new Song(songId,songName,songPath,songDuration,coverBytes));


            }while(res.moveToNext());
            return true;
        }
   }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean("serviceStatus", serviceBound);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        serviceBound = savedInstanceState.getBoolean("serviceStatus");
    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
    @Override
    public void itemClicked(View view, int position,String playlistName) {

       int songPosition=position;
       Log.e("playlistname1",playlistName);
       String previousPlaylist=new StorageUtility(getApplicationContext()).loadPlaylist();
        boolean isServiceRunning = Utilities.isServiceRunning(MediaPlayerService.class.getName(), getApplicationContext());
       if(!isServiceRunning ) {
           StorageUtility storage = new StorageUtility(getApplicationContext());
           Log.e("pl",playlistName);
           storage.storePlaylist(playlistName);
           storage.storeAudio(songList);
           storage.storeAudioIndex(songPosition);


           Intent playerIntent = new Intent(this, MediaPlayerService.class);
           playerIntent.putExtra("currentSongPosition","0");
           startService(playerIntent);
           Intent playing=new Intent(this,songPlayer.class);

           startActivity(playing);
       }
       else
       {
           StorageUtility storage = new StorageUtility(getApplicationContext());
           Log.e("pl",playlistName);
           storage.storePlaylist(playlistName);
           storage.storeAudio(songList);
           storage.storeAudioIndex(songPosition);

           //Service is active
           //Send a broadcast to the service -> PLAY_NEW_AUDIO
           Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
           sendBroadcast(broadcastIntent);
           Intent playing=new Intent(this,songPlayer.class);
           startActivity(playing);
       }
    }

    @Override
    public void popUpClicked(View view,final int listPosition) {

        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.songs_options_menu, popup.getMenu());

        Log.e("pop clicked","for removing");

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                             @Override
                                             public boolean onMenuItemClick(MenuItem item) {
                                                 //do your things in each of the following cases
                                                 switch (item.getItemId()) {

                                                     case R.id.deletesongs:

                                                         int songId;
                                                         songId=songList.get(listPosition).getID();
                                                         int rowsDeleted=sdh.delete(songId);

                                                         if(rowsDeleted>0)
                                                         {
                                                             songList.remove(listPosition);
                                                             adapter.notifyDataSetChanged();
                                                            // adapter.notifyItemRemoved(listPosition);
                                                            // songsList.this.notifyAll();
                                                            // getSongList(playlistName);

                                                         }

                                                         return true;
                                                     default:
                                                         return false;
                                                 }
                                             }
                                         });
        popup.show();

       /* int songId;
        songId=songList.get(listPosition).getID();
        int rowsDeleted=sdh.delete(songId);

        if(rowsDeleted>0)
        {
            songList.remove(listPosition);
        }*/

    }

    @Override
    public void favouriteButton(View view, int listPosition,String playlistName, boolean fav) {
        if(fav==false)
        {
            view.setSelected(true);
            // sharedPreference.addFavorite(this, songList.get(listPosition));
            sdh.updateSongs(songList.get(listPosition).getID(),1);
        }
        else
        {
            view.setSelected(false);
            // sharedPreference.removeFavorite(this, songList.get(listPosition));
            sdh.updateSongs(songList.get(listPosition).getID(),0);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 7 && resultCode == RESULT_OK) {
            String song[];
            Uri uri = data.getData();

            //if(uri.getScheme().equals("content"))
            //  {
            // Log.e("content","the path has content");
            //}
            if (uri != null) {
                song = getRealPathFromURI(getApplicationContext(), uri);
                for (int i = 0; i < song.length; i++) {
                    Log.e("path", song[i]);
               //     getSongList(playlistName);

                }
            } else {
                song = null;
            }


            ars.addToDatabaseAndListView(song[1], song[0], Float.parseFloat(song[2]));
            adapter = new customListAdaptor(this,songList,playlistName);
            adapter.setClickListener(this);
            list.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            finish();
            startActivity(getIntent());


        }
    }
    private String[] getRealPathFromURI(Context context, Uri contentUri) {
        String[] song=new String[5];
        CursorLoader loader = new CursorLoader(context, contentUri, null, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int path_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int name_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int duration_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int artist_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        int album= cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);

        cursor.moveToFirst();

        String songPath=cursor.getString(path_index);
        String songName=cursor.getString(name_index);
        String songDuration=cursor.getString(duration_index);
        String songArtist=cursor.getString(artist_index);
        String albumm=cursor.getString(album);

        song[0]=songPath;
        song[1]=songName;
        song[2]=songDuration;
        song[3]=songArtist;
        song[4]=albumm;

        return song;

    }
}
