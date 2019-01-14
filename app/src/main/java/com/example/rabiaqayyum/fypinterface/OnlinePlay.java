package com.example.rabiaqayyum.fypinterface;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OnlinePlay extends Activity implements OnClickListener, OnTouchListener, OnCompletionListener, OnBufferingUpdateListener {

    private ImageButton buttonPlayPause, repeat, next, prev, shuffle;
    private SeekBar seekBarProgress;
    private ImageView iv;
    private String name,img,url,artist,time,time2;
    private  Boolean started=true;
    private TextView tvtitle,tvartist,tvtimestart,timeend;
    private MediaPlayer mediaPlayer;
    private ImageView ivblurr;
    private NotificationPanel nPanel ;

    private Boolean isConnected,isShuffle= false;
    private int mediaFileLengthInMilliseconds; // this value contains the song duration in milliseconds. Look at getDuration() method in MediaPlayer class

    private final Handler handler = new Handler();

    private  Boolean isRepeat=true;

    public int index,preindex;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onlineplay);

        Intent broadcastIntent = new Intent("OnlineStop");
        sendBroadcast(broadcastIntent);


        final Intent i= getIntent();
//        name=i.getStringExtra("name");
//        img=i.getStringExtra("img");
//        url=i.getStringExtra("url");
//        artist=i.getStringExtra("artist");

        index = i.getIntExtra("index",0);
        preindex = index;

        name=SharedDataSongs.name.get(index);
        img=SharedDataSongs.img.get(index);
        url=SharedDataSongs.url.get(index);
        artist=SharedDataSongs.artist.get(index);

        tvtitle= (TextView) findViewById(R.id.textViewTitleonline);
        tvartist= (TextView) findViewById(R.id.textViewArtistonline);
        tvtimestart= (TextView) findViewById(R.id.textView6online);
        timeend= (TextView) findViewById(R.id.textView8online);
        ivblurr= (ImageView) findViewById(R.id.ivblurr);

        next = (ImageButton) findViewById(R.id.imageButton4online);
        prev = (ImageButton) findViewById(R.id.imageButton2online);
        shuffle = (ImageButton) findViewById(R.id.imageButton5online);


        buttonPlayPause = (ImageButton) findViewById(R.id.imageButton3online);
        buttonPlayPause = (ImageButton) findViewById(R.id.imageButton3online);
        repeat = (ImageButton) findViewById(R.id.imageButton1online);
        iv= (ImageView) findViewById(R.id.view2online);


        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isRepeat= prefs.getBoolean("repeat", false);

        if (isRepeat)
            repeat.setImageResource(R.drawable.ic_repeat);
        else if (!isRepeat)
            repeat.setImageResource(R.drawable.ic_repea_stop);

        if (!isConnectedToInternet())
        {
            Toast.makeText(this, "Internet Connection Error", Toast.LENGTH_SHORT).show();
            finish();
        }
        repeat.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRepeat)
                {
                    mediaPlayer.setLooping(false);
                    repeat.setImageResource(R.drawable.ic_repea_stop);
                    prefs.edit().putBoolean("repeat", false).apply();
                    isRepeat=false;
                }
                else {
                    mediaPlayer.setLooping(true);
                    repeat.setImageResource(R.drawable.ic_repeat);
                    prefs.edit().putBoolean("repeat", true).apply();
                    isRepeat=true;
                }
            }
        });


        buttonPlayPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    buttonPlayPause.setImageResource(R.drawable.ic_my_pause);
                } else {
                    mediaPlayer.pause();
                    buttonPlayPause.setImageResource(R.drawable.ic_my_play);
                    //  nPanel.notificationCancel();
                }
                primarySeekBarProgressUpdater();
            }
        });

        if (isConnectedToInternet()) {

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    initView();
                    //Do something after 100ms
                }
            }, 1000);

        }
        else
            Toast.makeText(this, "Internet Connection Issue", Toast.LENGTH_SHORT).show();




        next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //dialog.show();
                preindex = index;
                if(OnlinePlay.this.isShuffle){
                    Random rand = new Random();
                    int songCheck =  index;
                    while (SharedDataSongs.name.size()>1 && index==songCheck){
                        songCheck= rand.nextInt(SharedDataSongs.name.size());
                    }

                    if (songCheck < SharedDataSongs.name.size()) {
                        index = songCheck;
                    }

                }
                else {
                    if(index<SharedDataSongs.name.size()-1){

                        index++;
                    }
                    else{
                        index = 0;
                    }

                }

                name=SharedDataSongs.name.get(index);
                img=SharedDataSongs.img.get(index);
                url=SharedDataSongs.url.get(index);
                artist=SharedDataSongs.artist.get(index);

                if (isConnectedToInternet()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            OnlinePlay.this.mediaPlayer.stop();

                            initView();
                            //Do something after 100ms
                        }
                    }, 1000);
                }
                else {
                    Toast.makeText(OnlinePlay.this, "Internet Connection Issue", Toast.LENGTH_SHORT).show();
                }

                Toast.makeText(OnlinePlay.this,name+" is loading" , Toast.LENGTH_LONG).show();
               // dialog.show();

                //dialog.dismiss();
            }
        });


        prev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               // dialog.show();
                if(OnlinePlay.this.isShuffle) {

                    index =preindex;
                }
                else {
                    if (index < 1) {
                        index = 0;
                    } else {
                        index--;
                    }
                }
                name = SharedDataSongs.name.get(index);
                img = SharedDataSongs.img.get(index);
                url = SharedDataSongs.url.get(index);
                artist = SharedDataSongs.artist.get(index);

                if (isConnectedToInternet()) {
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            OnlinePlay.this.mediaPlayer.stop();

                            initView();
                            //Do something after 100ms
                        }
                    }, 1000);
                } else
                    Toast.makeText(OnlinePlay.this, "Internet Connection Issue", Toast.LENGTH_SHORT).show();


                Toast.makeText(OnlinePlay.this,name+" is loading" , Toast.LENGTH_LONG).show();


            }

        });

        shuffle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(OnlinePlay.this.isShuffle){
                    OnlinePlay.this.isShuffle = false;
                    shuffle.setImageResource(R.drawable.shuffle_stop);

                }
                else {
                    OnlinePlay.this.isShuffle = true;
                    shuffle.setImageResource(R.drawable.ic_shufeel);
                }

            }
        });


    }



    /**
     * This method initialise all the views in project
     */
    @SuppressLint({"DefaultLocale", "ClickableViewAccessibility"})
    private void initView() {

        tvartist.setText(artist);
        tvtitle.setText(name);


        Picasso.get()
                .load(img)
                .into(iv);

        seekBarProgress = (SeekBar) findViewById(R.id.seekBaronline);
        seekBarProgress.setMax(99); // It means 100% .0-99
        seekBarProgress.setOnTouchListener(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnCompletionListener(this);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            try {
                URL url = new URL(img);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                ivblurr.setImageBitmap(BlurBuilder.blur(getApplicationContext(),myBitmap));
            } catch (IOException e) {
                // Log exception

            }

        }
        try {
            mediaPlayer.setDataSource(url); // setup song from http://www.hrupin.com/wp-content/uploads/mp3/testsong_20_sec.mp3 URL to mediaplayer data source
            mediaPlayer.prepare(); // you must call this method after setup the datasource in setDataSource method. After calling prepare() the instance of MediaPlayer starts load data from URL to internal buffer.
        } catch (Exception e) {
            e.printStackTrace();
        }

        mediaFileLengthInMilliseconds = mediaPlayer.getDuration(); // gets the song length in milliseconds from URL

        int duration = mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition();
        time = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
        int durationend = (mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100;
        time2 = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(durationend),
                TimeUnit.MILLISECONDS.toSeconds(durationend) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationend))
        );
        if (!mediaPlayer.isPlaying()) {

            mediaPlayer.start();
            // nPanel = new NotificationPanel(OnlinePlay.this, name);
            buttonPlayPause.setImageResource(R.drawable.ic_my_pause);
        } else {
            mediaPlayer.pause();
            buttonPlayPause.setImageResource(R.drawable.ic_my_play);
        }
        primarySeekBarProgressUpdater();
    }

    /**
     * Method which updates the SeekBar primary progress by current song playing position
     */
    private void primarySeekBarProgressUpdater() {
        seekBarProgress.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / mediaFileLengthInMilliseconds) * 100)); // This math construction give a percentage of "was playing"/"song length"
//        tvtimestart.setText((mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100);
//        timeend.setText(mediaPlayer.getDuration()-mediaPlayer.getCurrentPosition());

//
//      tvtimestart.setText(time2);


        int durationend = (mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*100;
        int counter  = mediaPlayer.getCurrentPosition();
        time2 = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(counter),
                TimeUnit.MILLISECONDS.toSeconds(counter) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(counter))
        );

        tvtimestart.setText(time2);




        timeend.setText(time);
        if (mediaPlayer.isPlaying()) {

            // t.setText(time);
            Runnable notification = new Runnable() {
                public void run() {
                    primarySeekBarProgressUpdater();
                }
            };
            handler.postDelayed(notification, 1000);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imageButton3online) {
            /** ImageButton onClick event handler. Method which start/pause mediaplayer playing */

        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.seekBaronline) {
            /** Seekbar onTouch event handler. Method which seeks MediaPlayer to seekBar primary progress position*/
            if (mediaPlayer.isPlaying()) {
                SeekBar sb = (SeekBar) v;
                int playPositionInMillisecconds = (mediaFileLengthInMilliseconds / 100) * sb.getProgress();
                mediaPlayer.seekTo(playPositionInMillisecconds);
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.stop();
        finish();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        /** MediaPlayer onCompletion event handler. Method which calls then song playing is complete*/


        next.performClick();


    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /** Method which updates the SeekBar secondary progress by current song loading from URL position*/
        seekBarProgress.setSecondaryProgress(percent);
    }


    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            isConnected = true;
        } else {
            Toast.makeText(this, "Internet Connection not available!", Toast.LENGTH_SHORT).show();
            isConnected = false;
        }
        return isConnected;
    }
}
