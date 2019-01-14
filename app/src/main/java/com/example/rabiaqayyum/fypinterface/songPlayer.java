package com.example.rabiaqayyum.fypinterface;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rabiaqayyum.fypinterface.Database.SongDatabaseHelper;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by RABIA QAYYUM on 4/10/2018.
 */

public class songPlayer extends Activity implements  MediaPlayer.OnCompletionListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final String ACTION_PLAY = "com.example.rabiaqayyum.fypinterface.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.rabiaqayyum.fypinterface.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.rabiaqayyum.fypinterface.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.rabiaqayyum.fypinterface.ACTION_NEXT";
    public static final String ACTION_REPEAT="com.example.rabiaqayym.fypinterface.ACTION_REPEAT";
    public static final String ACTION_NOREPEAT="com.example.rabiaqayym.fypinterface.ACTION_NOREPEAT";
    public static final String ACTION_STOP = "com.example.rabiaqayyum.fypinterface.ACTION_STOP";
    public static final String ACTION_FORWARD="com.example.rabiaqayym.fypinterface.ACTION_FORWARD";
    public static final String ACTION_REVERSE="com.example.rabiaqayym.fypinterface.ACTION_REVERSE";
    public static final String ACTION_SHUFFLE="com.example.rabiaqayym.fypinterface.ACTION_SHUFFLE";
    public static final String ACTION_SHUFFLE_STOP="com.example.rabiaqayym.fypinterface.ACTION_SHUFFLE_STOP";

    public SharedPreferences prefs;


    Intent serviceIntent;

    private  String previous_Uri="", previous_itemName="";

    private static ClickListener clicklistener = null;
    // --Seekbar variables --
   // private SeekBar seekBar;
    SeekBar songProgressBar;
    private int seekMax;
    private static int songEnded = 0;
    boolean mBroadcastIsRegistered;
    private ImageView ivblur;

    private ImageView ib;
    public static final String BROADCAST_SEEKBAR = "com.example.rabiaqayym.fypinterface.sendseekbar";
    public static final String BROADCAST_SongNameChange = "com.example.rabiaqayym.fypinterface.songNameChange";
    Intent intent;



    Boolean mBound;
    ArrayList<Song> songList;
    public int activeSongIndex;
    ImageView previous,play,next,repeat,shuffle;
    ImageView playing,icon;
    private Boolean isChecked =false;
    public TextView itemname;

   // private  MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();;
  //  private SongsManager songManager;
    private Utilities utils;
    private StorageUtility storage;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private int currentSongIndex = 0;
    private boolean isShuffle = true;
    private boolean isRepeat = true;
    private boolean isFav = true;
    private TextView time,seekTime;

    private boolean isBound=false;
    private MediaPlayerService mps;

    private long currentSeekPosition;
    private long totalDuration;
    private static int state=1;//0  pause    1 playing
    SongDatabaseHelper sdh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        songList=new ArrayList<>();
        mps=new MediaPlayerService();
        setContentView(R.layout.songplay);
        previous=(ImageView) findViewById(R.id.imageButton2offline);
        play=(ImageView) findViewById(R.id.imageButton3offline);
        next=(ImageView) findViewById(R.id.imageButton4offline);
        repeat= (ImageView) findViewById(R.id.imageButton1offline);
        shuffle= (ImageView) findViewById(R.id.imageButton5offline);

        //todo
        time= (TextView) findViewById(R.id.textView8offline);
        seekTime= (TextView) findViewById(R.id.textView8offline2);

        ivblur= (ImageView) findViewById(R.id.ivblurroffline);

         sdh = new SongDatabaseHelper(this);


         prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        isShuffle = prefs.getBoolean("shuffle", false);
        isRepeat = prefs.getBoolean("repeat", false);
//        isFav = prefs.getBoolean("favSong",false);

        if (isShuffle)
            shuffle.setImageResource(R.drawable.ic_shufeel);
        else if (!isShuffle)
            shuffle.setImageResource(R.drawable.shuffle_stop);


        if (isRepeat)
            repeat.setImageResource(R.drawable.ic_repeat);
        else if (!isRepeat)
            repeat.setImageResource(R.drawable.ic_repea_stop);




        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.surpriselist);

        ivblur.setImageBitmap(BlurBuilder.blur(getApplicationContext(),largeIcon));

        itemname=(TextView)findViewById(R.id.textViewTitleoffline);

        icon=(ImageView) findViewById(R.id.view2offline);
       // playing=(ImageView) findViewById(R.id.playing);
        songProgressBar=(SeekBar) findViewById(R.id.seekBaroffline);

        ib= (ImageView) findViewById(R.id.imageButton);
        serviceIntent = new Intent(this, MediaPlayerService.class);


        // --- set up seekbar intent for broadcasting new position to service ---
        intent = new Intent(BROADCAST_SEEKBAR);


        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isChecked) {
                  //  clicklistener.favouriteButton(view, "Happy",false);
                    ib.setImageResource(R.drawable.like_selected);
                    songPlayer.this.sdh.updateSongs(songList.get(activeSongIndex).getID(),1);
                    isChecked=true;
                }
                else
                {
                   // clicklistener.favouriteButton(view, "Happy",true);
                    ib.setImageResource(R.drawable.like_unselected);
                    songPlayer.this.sdh.updateSongs(songList.get(activeSongIndex).getID(),0);
                    isChecked=false;
                }
            }
        });

       // mp=new MediaPlayer();
        utils=new Utilities();
        storage=new StorageUtility(this);
        songList=storage.loadAudio();
        activeSongIndex=storage.loadAudioIndex();
       // byte[] image=songList.get(activeSongIndex).getImageResource();
        //Bitmap songDp=BitmapFactory.decodeByteArray(image,0,image.length);
        //icon.setImageBitmap(songDp);

        songProgressBar.setOnSeekBarChangeListener(this);

        registerReceiver(broadcastReceiver, new IntentFilter(
                MediaPlayerService.BROADCAST_ACTION));


        registerReceiver(songNameReceiver,new IntentFilter("updateSongNameUI"));
        mBroadcastIsRegistered = true;
       // updateProgressBar();

       // songProgressBar.setOnSeekBarChangeListener(this);

//        mp.setOnCompletionListener(this);

        itemname.setText(songList.get(activeSongIndex).getTitle());
       // artistname.setText(songList.get(activeSongIndex).getTitle());

        previous.setOnClickListener(this);


        if(songPlayer.this.sdh.checkFavStatus(songList.get(activeSongIndex).getID())){
            ib.setImageResource(R.drawable.like_selected);
        }
        else {
            ib.setImageResource(R.drawable.like_unselected);
        }




        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                if (!isShuffle) {

                    prefs.edit().putBoolean("shuffle", true).apply();
//                    Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
//                    intent.putExtra("currentSongPosition", "0");
//                    intent.setAction(ACTION_SHUFFLE);
//                    startService(intent);
                    shuffle.setImageResource(R.drawable.ic_shufeel);
                    isShuffle=true;
                }
                else if (isShuffle)
                {

                    prefs.edit().putBoolean("shuffle", false).apply();
//                    Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
//                    intent.putExtra("currentSongPosition", "0");
//                    intent.setAction(ACTION_SHUFFLE_STOP);
//                    startService(intent);
                    shuffle.setImageResource(R.drawable.shuffle_stop);
                    isShuffle=false;
                }
            }
        });


        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isRepeat) {


                    Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
                    intent.putExtra("loop", "repeat");
                    intent.setAction(ACTION_REPEAT);
                    startService(intent);
                    repeat.setImageResource(R.drawable.ic_repeat);
                    prefs.edit().putBoolean("repeat", true).apply();
                    isRepeat=true;
                }
                else if (isRepeat)
                {
                    Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
                    intent.putExtra("loop", "Stoprepeat");
                    intent.setAction(ACTION_NOREPEAT);
                    startService(intent);
                    repeat.setImageResource(R.drawable.ic_repea_stop);
                    prefs.edit().putBoolean("repeat", false).apply();
                    isRepeat=false;
                }

            }
        });




        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true)
                {
                    if (isShuffle) {
                        Random rand = new Random();
                        int songCheck =  songPlayer.this.activeSongIndex;
                        while (songList.size()>1 && songPlayer.this.activeSongIndex==songCheck){
                            songCheck= rand.nextInt(songList.size());
                        }

                        if (songCheck < songList.size()) {
                            itemname.setText(songList.get(songCheck).getTitle());
                            String uri=(songList.get(songCheck).getSongUrl());
                            Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
                            intent.putExtra("currentSongPosition",songCheck+"");

                            intent.putExtra("uri",uri);
                          //  intent.setAction(ACTION_SHUFFLE);

                            songPlayer.this.activeSongIndex=songCheck;
                            startService(intent);
                        }
                        else {
                            songPlayer.this.activeSongIndex=  0;
                            itemname.setText(songList.get(songPlayer.this.activeSongIndex).getTitle());
                        }
                    }

                    else {
                        Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
                        intent.putExtra("currentSongPosition", "0");
                        intent.setAction(ACTION_NEXT);
                        startService(intent);
                        int songCheck = songPlayer.this.activeSongIndex + 1;
                        if (songCheck < songList.size()) {
                            itemname.setText(songList.get(++songPlayer.this.activeSongIndex).getTitle());
                            // artistname.setText(songList.get(++activeSongIndex).A);
                        } else {
                            songPlayer.this.activeSongIndex = 0;
                            itemname.setText(songList.get(songPlayer.this.activeSongIndex).getTitle());
                        }
                    }
                }
                else {

                    if (isShuffle) {
                        Random rand = new Random();
                        int n = rand.nextInt(songList.size()) + 2;
                        int songCheck = n;
                        if (songCheck < songList.size()) {
                            itemname.setText(songList.get(songCheck).getTitle());

                            Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
                            intent.putExtra("currentSongPosition", songCheck + "");
                            String uri = (songList.get(songCheck).getSongUrl());
                            intent.putExtra("uri", uri);
                            //  intent.setAction(ACTION_SHUFFLE);
                            startService(intent);
                        } else {
                            songPlayer.this.activeSongIndex = 0;
                            itemname.setText(songList.get(songPlayer.this.activeSongIndex).getTitle());
                        }
                    } else {
                        StorageUtility storage = new StorageUtility(getApplicationContext());
                        storage.storeAudio(songList);
                        storage.storeAudioIndex(songPlayer.this.activeSongIndex);

                        Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
                        playerIntent.setAction(ACTION_NEXT);
                        playerIntent.putExtra("currentSongPosition", currentSeekPosition);
                        startService(playerIntent);
                        int songCheck = songPlayer.this.activeSongIndex + 1;
                        if (songCheck < songList.size()) {
                            itemname.setText(songList.get(++songPlayer.this.activeSongIndex).getTitle());
                        } else {
                            songPlayer.this.activeSongIndex = 0;
                            itemname.setText(songList.get(songPlayer.this.activeSongIndex).getTitle());
                            itemname.setText(songList.get(songPlayer.this.activeSongIndex).getID());
                        }
                    }




                }
                if(songPlayer.this.sdh.checkFavStatus(songList.get(activeSongIndex).getID())){
                    ib.setImageResource(R.drawable.like_selected);
                }
                else {
                    ib.setImageResource(R.drawable.like_unselected);
                }
                play.setImageResource(R.drawable.ic_my_pause);


            }
        });

//        next.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true)
//                {
//
//
//                    previous_Uri=(songList.get(activeSongIndex).getSongUrl());
//                    previous_itemName=(songList.get(activeSongIndex).getTitle());
//
//
//                    if (isShuffle) {
//                        Random rand = new Random();
//                        int n = rand.nextInt(songList.size()) + 1;
//                        int songCheck =  n;
//                        if (songCheck < songList.size()) {
//                            itemname.setText(songList.get(songCheck).getTitle());
//
//                            Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
//                            intent.putExtra("currentSongPosition",songCheck+"");
//                            String uri=(songList.get(songCheck).getSongUrl());
//                            intent.putExtra("uri",uri);
//                          //  intent.setAction(ACTION_SHUFFLE);
//                            startService(intent);
//                        }
//                    }
//                    else if (!isShuffle)
//                    {
//                    int songCheck=activeSongIndex+1;
//                    if(songCheck<songList.size()) {
//                        itemname.setText(songList.get(++activeSongIndex).getTitle());
//
//                        Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
//                        intent.putExtra("currentSongPosition","0");
//                        intent.setAction(ACTION_NEXT);
//                        startService(intent);
//                        // artistname.setText(songList.get(++activeSongIndex).A);
//                    }
//                    else
//                    {
//                        activeSongIndex=0;
//                      //  itemname.setText(songList.get(activeSongIndex).getTitle());
//                    }
//                    }
//
//                }
//                else
//                {
//
//                    if (isShuffle) {
//                        Random rand = new Random();
//                        int n = rand.nextInt(songList.size()) + 1;
//                        int songCheck =  n;
//                        if (songCheck < songList.size()) {
//
//                            itemname.setText(songList.get(songCheck).getTitle());
//                            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
//                            playerIntent.putExtra("currentSongPosition",songCheck+"");
//                            String uri=(songList.get(songCheck).getSongUrl());
//                            playerIntent.putExtra("uri",uri);
//                           // intent.setAction(ACTION_SHUFFLE);
//                            startService(playerIntent);
//                        }
//                    }
//                    else if (!isShuffle)
//                    {
//
//                        StorageUtility storage = new StorageUtility(getApplicationContext());
//                        storage.storeAudio(songList);
//                        storage.storeAudioIndex(activeSongIndex);
//                        int songCheck=activeSongIndex+1;
//                        if(songCheck<songList.size()) {
//                            itemname.setText(songList.get(++activeSongIndex).getTitle());
//
//
//                            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
//                            playerIntent.setAction(ACTION_NEXT);
//                            playerIntent.putExtra("currentSongPosition",currentSeekPosition);
//                            startService(playerIntent);
//                            // artistname.setText(songList.get(++activeSongIndex).A);
//                        }
//                        else
//                        {
//                            activeSongIndex=0;
//                           // itemname.setText(songList.get(activeSongIndex).getTitle());
//                        }
//                    }
//                }
//                play.setImageResource(R.drawable.ic_my_pause);
//
//
//            }
//        });
        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true)
                {
                    if(state==1)
                    {
                        //Bitmap image=
                        // play.setBackgroundDrawable(R.drawable.pause);
                       // play.setBackgroundDrawable(getResources().getDrawable(R.drawable.play));
                        play.setImageResource(R.drawable.ic_my_play);
                       // gif.setBackgroundDrawable(getResources().getDrawable(R.drawable.beatsstill));


                        state=0;
                        Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
                        intent.putExtra("currentSongPosition",currentSeekPosition);
                        intent.setAction(ACTION_PAUSE);
                        startService(intent);

                    }
                    else
                    {
                        //play.setBackgroundDrawable(getResources().getDrawable(R.drawable.pause));

                        play.setImageResource(R.drawable.ic_my_pause);
                        //gif.setBackgroundDrawable(getResources().getDrawable(R.drawable.beatsgif));

                        state=1;
                        Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
                        intent.putExtra("currentSongPosition",currentSeekPosition);
                        intent.setAction(ACTION_PLAY);
                        startService(intent);

                        registerReceiver(broadcastReceiver, new IntentFilter(
                                MediaPlayerService.BROADCAST_ACTION));
                        ;
                        mBroadcastIsRegistered = true;
                        //  Intent
                    }
                }
                else
                {
                    play.setImageResource(R.drawable.ic_my_play);
                    StorageUtility storage = new StorageUtility(getApplicationContext());
                    storage.storeAudio(songList);
                    storage.storeAudioIndex(activeSongIndex);

                    Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
                    playerIntent.putExtra("currentSongPosition",currentSeekPosition);
                    startService(playerIntent);
                    play.setImageResource(R.drawable.ic_my_pause);
                   state=1;
                }

            }
        });

       // Intent receive=getIntent();

     //   Bundle args=receive.getBundleExtra("bundle");

     //   ArrayList<Song> songList=args.getSerializable("songarraylist");




    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent serviceIntent) {
            updateUI(serviceIntent);
        }
    };

    private BroadcastReceiver songNameReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            next.performClick();



//            int activeSongIndex=intent.getIntExtra("songIndex",0);
//            String songName=songList.get(activeSongIndex).getTitle();
//            itemname.setText(songName);
//            if(songPlayer.this.sdh.checkFavStatus(songList.get(activeSongIndex).getID())){
//                ib.setImageResource(R.drawable.like_selected);
//            }
//            else {
//                ib.setImageResource(R.drawable.like_unselected);
//            }


        }
    };

    public void setClickListener(ClickListener clickListener){
        this.clicklistener = clickListener;
    }
    private void updateUI(Intent serviceIntent) {
        String counter = serviceIntent.getStringExtra("counter");
        String mediamax = serviceIntent.getStringExtra("mediamax");
        String strSongEnded = serviceIntent.getStringExtra("song_ended");
        long songTotalTime = Long.valueOf(mediamax);
        long seekPassedTime = Long.valueOf(counter);


        String totaltime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songTotalTime),
                TimeUnit.MILLISECONDS.toSeconds(songTotalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songTotalTime))
        );


        String seektime = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(seekPassedTime),
                TimeUnit.MILLISECONDS.toSeconds(seekPassedTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(seekPassedTime))
        );


        songPlayer.this.time.setText(totaltime);
        songPlayer.this.seekTime.setText(seektime);



        int seekProgress = Integer.parseInt(counter);
        seekMax = Integer.parseInt(mediamax);
        songEnded = Integer.parseInt(strSongEnded);
        songProgressBar.setMax(seekMax);
        songProgressBar.setProgress(seekProgress);
        if (songEnded == 1) {
            play.setBackgroundResource(R.drawable.play);
            Toast.makeText(this, "Song Ended", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
//
//        if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true)
//        {
//            Intent intent=new Intent(songPlayer.this,MediaPlayerService.class);
//            intent.putExtra("currentSongPosition","0");
//            intent.setAction(ACTION_NEXT);
//            startService(intent);
//            int songCheck=activeSongIndex+1;
//            if(songCheck<songList.size()) {
//                itemname.setText(songList.get(++activeSongIndex).getTitle());
//                // artistname.setText(songList.get(++activeSongIndex).A);
//            }
//            else
//            {
//                activeSongIndex=0;
//                itemname.setText(songList.get(activeSongIndex).getTitle());
//            }
//        }
//        else
//        {
//            StorageUtility storage = new StorageUtility(getApplicationContext());
//            storage.storeAudio(songList);
//            storage.storeAudioIndex(activeSongIndex);
//
//            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
//            playerIntent.setAction(ACTION_NEXT);
//            playerIntent.putExtra("currentSongPosition",currentSeekPosition);
//            startService(playerIntent);
//            int songCheck=activeSongIndex+1;
//            if(songCheck<songList.size()) {
//                itemname.setText(songList.get(++activeSongIndex).getTitle());
//            }
//            else
//            {
//                activeSongIndex=0;
//                itemname.setText(songList.get(activeSongIndex).getTitle());
//            }
//        }


    }
    /*

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            mps = binder.getService();
            mBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };*/



    ///previous button
    @Override
    public void onClick(View view) {
        /*MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(songPlayer.this).getTransportControls();
        controls.skipToNext();*/

        if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true)
        {
            Intent intent=new Intent(this,MediaPlayerService.class);
            intent.putExtra("currentSongPosition","0");
            intent.setAction(ACTION_PREVIOUS);
            startService(intent);
            int songCheck=activeSongIndex-1;
            if(songCheck>=0)
            {
                itemname.setText(songList.get(--activeSongIndex).getTitle());
            }
            else
            {
                activeSongIndex=songList.size()-1;
                itemname.setText(songList.get(activeSongIndex).getTitle());
            }
        }
        else
        {
            StorageUtility storage = new StorageUtility(getApplicationContext());
            storage.storeAudio(songList);
            storage.storeAudioIndex(activeSongIndex);

            Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
            playerIntent.setAction(ACTION_PREVIOUS);
            playerIntent.putExtra("currentSongPosition",currentSeekPosition);
            startService(playerIntent);
            int songCheck=activeSongIndex-1;
            if(songCheck>=0)
            {
                itemname.setText(songList.get(--activeSongIndex).getTitle());
            }
            else
            {
                activeSongIndex=songList.size()-1;
                itemname.setText(songList.get(activeSongIndex).getTitle());
            }
        }
        if(songPlayer.this.sdh.checkFavStatus(songList.get(activeSongIndex).getID())){
            ib.setImageResource(R.drawable.like_selected);
        }
        else {
            ib.setImageResource(R.drawable.like_unselected);
        }
        play.setImageResource(R.drawable.ic_my_pause);

    }



//
//    ///previous button
//    @Override
//    public void onClick(View view) {
//        /*MediaControllerCompat.TransportControls controls = MediaControllerCompat.getMediaController(songPlayer.this).getTransportControls();
//        controls.skipToNext();*/
//
//        if(utils.isServiceRunning(MediaPlayerService.class.getName(),getApplicationContext())==true) {
//
//
//            if (isShuffle && !previous_itemName.isEmpty()) {
//
//                    itemname.setText(previous_itemName);
//
//                    Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
//                   // intent.putExtra("currentSongPosition", songCheck + "");
//                   // String uri = (songList.get(songCheck).getSongUrl());
//                    intent.putExtra("uri", previous_Uri);
//                    //  intent.setAction(ACTION_SHUFFLE);
//                    startService(intent);
//
//            } else if (!isShuffle)
//            {
//                Intent intent = new Intent(this, MediaPlayerService.class);
//            intent.putExtra("currentSongPosition", "0");
//            intent.setAction(ACTION_PREVIOUS);
//            startService(intent);
//            int songCheck = activeSongIndex - 1;
//            if (songCheck >= 0) {
//                itemname.setText(songList.get(--activeSongIndex).getTitle());
//            } else {
//                activeSongIndex = songList.size() - 1;
//                itemname.setText(songList.get(activeSongIndex).getTitle());
//            }
//        }
//        }
//        else {
//
//            if (isShuffle&& !previous_itemName.isEmpty()) {
//
//                itemname.setText(previous_itemName);
//
//                Intent intent = new Intent(songPlayer.this, MediaPlayerService.class);
//                // intent.putExtra("currentSongPosition", songCheck + "");
//                // String uri = (songList.get(songCheck).getSongUrl());
//                intent.putExtra("uri", previous_Uri);
//                //  intent.setAction(ACTION_SHUFFLE);
//                startService(intent);
//
//            } else if (!isShuffle) {
//
//
//                StorageUtility storage = new StorageUtility(getApplicationContext());
//                storage.storeAudio(songList);
//                storage.storeAudioIndex(activeSongIndex);
//
//                Intent playerIntent = new Intent(getApplicationContext(), MediaPlayerService.class);
//                playerIntent.setAction(ACTION_PREVIOUS);
//                playerIntent.putExtra("currentSongPosition", currentSeekPosition);
//                startService(playerIntent);
//                int songCheck = activeSongIndex - 1;
//                if (songCheck >= 0) {
//                    itemname.setText(songList.get(--activeSongIndex).getTitle());
//                } else {
//                    activeSongIndex = songList.size() - 1;
//                    itemname.setText(songList.get(activeSongIndex).getTitle());
//                }
//            }
//
//        }
//
//
//
//    }



   /*  BroadcastReceiver br=new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle parameters=getIntent().getExtras();
        //  currentPosition=parameters.getLongExtra("currentPosition");
        if(getIntent().getAction()=="seekbar") {
            currentPosition = parameters.getLong("currentPosition");
            totalDuration = parameters.getLong("duration");
            // songProgressBar.setMax((int)totalDuration);
            //songProgressBar.setProgress((int)currentPosition);
            Log.e("duration", totalDuration + "");
            Log.e("position", currentPosition + "");
        }

        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
};
*/

    @Override
    protected void onResume() {
        super.onResume();
        //registerReceiver(br,new IntentFilter("Broadcast"));
        registerReceiver(broadcastReceiver, new IntentFilter(
                MediaPlayerService.BROADCAST_ACTION));
        registerReceiver(songNameReceiver,new IntentFilter("updateSongNameUI"));
        mBroadcastIsRegistered=true;


    }

    @Override
    protected void onPause() {
        super.onPause();
       // unregisterReceiver(br);
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(songNameReceiver);
        mBroadcastIsRegistered=false;
    }

    @Override
    public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
        if (fromUser) {
            int seekPosition = sb.getProgress();
            intent.putExtra("seekpos", seekPosition);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }






}
