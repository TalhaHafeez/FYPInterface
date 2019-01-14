package com.example.rabiaqayyum.fypinterface;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,MediaPlayer.OnPreparedListener,
                                                           MediaPlayer.OnErrorListener,MediaPlayer.OnInfoListener, MediaPlayer.OnSeekCompleteListener,
                                                           MediaPlayer.OnBufferingUpdateListener,AudioManager.OnAudioFocusChangeListener{

    public MediaPlayer mediaPlayer;
    int seekPos;
    Song song;
    Boolean flag=false;
    Boolean flagShuffle=false;


    public static final String ACTION_PLAY = "com.example.rabiaqayyum.fypinterface.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.rabiaqayyum.fypinterface.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.example.rabiaqayyum.fypinterface.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.example.rabiaqayyum.fypinterface.ACTION_NEXT";
    public static final String ACTION_STOP = "com.example.rabiaqayyum.fypinterface.ACTION_STOP";
    public static final String ACTION_FORWARD="com.example.rabiaqayym.fypinterface.ACTION_FORWARD";
    public static final String ACTION_REPEAT="com.example.rabiaqayym.fypinterface.ACTION_REPEAT";
    public static final String ACTION_NOREPEAT="com.example.rabiaqayym.fypinterface.ACTION_NOREPEAT";
    public static final String ACTION_REVERSE="com.example.rabiaqayym.fypinterface.ACTION_REVERSE";
    public static final String ACTION_SHUFFLE="com.example.rabiaqayym.fypinterface.ACTION_SHUFFLE";
    public static final String ACTION_SHUFFLE_STOP="com.example.rabiaqayym.fypinterface.ACTION_SHUFFLE_STOP";
    public static final String ACTION_NAME_Change="com.example.rabiaqayym.fypinterface.songNameChange";


    private int resumePosition;

    // ---Variables for seekbar processing---
    int seekPosition = 0;
    String sntSeekPos;
    int intSeekPos;
    int mediaPosition;
    int mediaMax;
    //Intent intent;
    private final Handler handler = new Handler();
    Intent intent;
    private static int songEnded;
    public static final String BROADCAST_ACTION = "com.example.rabiaqayym.fypinterface.seekprogress";
    public static final String BROADCAST_SONGName = "updateSongNameUI";


    Intent bufferIntent;
    Intent seekIntent;

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    private static final int NOTIFICATION_ID = 101;


    private ArrayList<Song> songList;
    private int songIndex=-1;
    private Song activeSong;

    private final IBinder iBinder = new LocalBinder();

    private AudioManager audioManager;

    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;


    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        seekIntent = new Intent(BROADCAST_ACTION);
        // Manage incoming phone calls during playback.
        // Pause MediaPlayer on incoming call,
        // Resume on hangup.

        //mediaPlayer.setOnSeekCompleteListener(this);
        callStateListener();
        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();

        intent = new Intent(BROADCAST_SONGName);
        //register_keepP laying();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {

            String x=intent.getStringExtra("currentSongPosition");
            String uri=intent.getStringExtra("uri");
            String loop= intent.getStringExtra("loop");
            if(loop != null) {
                if (loop.equals("repeat"))
                {
                    mediaPlayer.setLooping(true);
                }
                else if (loop.equals("Stoprepeat"))
                {

                    mediaPlayer.setLooping(false);
                }
            }
            if(x != null) {
                Log.e("info7", "XXXXXXXXXXX " + x);
                seekPosition = Integer.parseInt(x);
            }
            if(uri != null) {

                Uri myUri = Uri.parse(uri);
                mediaPlayer.reset();
                try {
                    mediaPlayer.setDataSource(getApplication(),myUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mediaPlayer.start();
            }
            //Load data from SharedPreferences
            StorageUtility storage = new StorageUtility(getApplicationContext());
            songList = storage.loadAudio();
            songIndex = storage.loadAudioIndex();

            Log.e("song Index check",songIndex+"");
            if (songIndex >= 0 && songIndex < songList.size()) {
                //index is in a valid range
               // songIndex=songIndex-1;
                Log.e("active song id",songList.get(songIndex).getID()+"");
                activeSong = songList.get(songIndex);
            } else {
                songIndex=0;
                activeSong=songList.get(songIndex);
            }

        } catch (NullPointerException e) {
            stopSelf();
        }

      //  --Set up receiver for seekbar change ---
                registerReceiver(broadcastReceiver, new IntentFilter(
                        songPlayer.BROADCAST_SEEKBAR));
                registerReceiver(repeatReceiver,new IntentFilter("repeatSong"));
                registerReceiver(onlineStop,new IntentFilter("OnlineStop"));



        //Request audio focus
        if (requestAudioFocus() == false) {
            //Could not gain focus
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent);

        // --- Set up seekbar handler ---
        setupHandler();
        return START_STICKY;




    }

    BroadcastReceiver repeatReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            flag=intent.getBooleanExtra("repeat",false);
        }
    };

    private void setupHandler() {
        handler.removeCallbacks(sendUpdatesToUI);
        handler.postDelayed(sendUpdatesToUI, 1000); // 1 second
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            // // Log.d(TAG, "entered sendUpdatesToUI");

            LogMediaPosition();

            handler.postDelayed(this, 1000); // 2 seconds

        }
    };

    private void LogMediaPosition() {
        // // Log.d(TAG, "entered LogMediaPosition");
        if (mediaPlayer.isPlaying()) {
            mediaPosition = mediaPlayer.getCurrentPosition();
            // if (mediaPosition < 1) {
            // Toast.makeText(this, "Buffering...", Toast.LENGTH_SHORT).show();
            // }
            mediaMax = mediaPlayer.getDuration();
            //seekIntent.putExtra("time", new Date().toLocaleString());
            seekIntent.putExtra("counter", String.valueOf(mediaPosition));
            seekIntent.putExtra("mediamax", String.valueOf(mediaMax));
            seekIntent.putExtra("song_ended", String.valueOf(songEnded));
            sendBroadcast(seekIntent);
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };


    private BroadcastReceiver nextSongReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateSeekPos(intent);
        }
    };

    // Update seek position from Activity
    public void updateSeekPos(Intent intent) {
         seekPos = intent.getIntExtra("seekpos",0);
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(sendUpdatesToUI);
            mediaPlayer.seekTo(seekPos);
            setupHandler();
        }

    }

    public void updateSongName(Intent intent) {
        seekPos = intent.getIntExtra("seekpos",0);
        if (mediaPlayer.isPlaying()) {
            handler.removeCallbacks(sendUpdatesToUI);
            mediaPlayer.seekTo(seekPos);
            setupHandler();
        }

    }



    public class LocalBinder extends Binder
    {
        public MediaPlayerService getService()
        {
            return MediaPlayerService.this;
        }
    }

    public void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
        else if (actionString.equalsIgnoreCase(ACTION_REPEAT)) {
            mediaPlayer.setLooping(true);
        }
        else if (actionString.equalsIgnoreCase(ACTION_NOREPEAT)) {
            transportControls.setRepeatMode(0);
        }
        else if (actionString.equalsIgnoreCase(ACTION_SHUFFLE)) {
            transportControls.setShuffleMode(seekPosition);
        }
        else if (actionString.equalsIgnoreCase(ACTION_SHUFFLE_STOP)) {
            transportControls.setShuffleMode(0);
        }
        else if (actionString.equalsIgnoreCase(ACTION_FORWARD)) {
            transportControls.seekTo(mediaPlayer.getCurrentPosition()+5000);
        }
        else if (actionString.equalsIgnoreCase(ACTION_REVERSE)){
            transportControls.seekTo(mediaPlayer.getCurrentPosition()-5000);
        }
    }


    private void buildNotification(PlaybackStatus playbackStatus) {

        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */


       /* Intent buttonIntent = new Intent(this, buttonReceiver.class);
        buttonIntent.putExtra("notificationId",NOTIFICATION_ID);

//Create the PendingIntent
        PendingIntent btPendingIntent = PendingIntent.getBroadcast(this, 0, buttonIntent,0);*/


        Intent resultIntent = new Intent(this, songPlayer.class);
       // resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(songPlayer.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|	Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);




        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.playingmusic); //replace with your own image

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mediaSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.notification))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(activeSong.getTitle())
                .setContentTitle(activeSong.getTitle())
                .setContentInfo(activeSong.getTitle())
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        if(!mediaPlayer.isPlaying())
        {
             notificationBuilder.addAction(android.R.drawable.ic_delete, "Dismiss", playbackAction(6));
        }
                Notification notification=notificationBuilder.build();
                startForeground(12,notification);
    }
    public class buttonReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notificationId = intent.getIntExtra("notificationId", 0);
            removeNotification();
        }
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 4:
                playbackAction.setAction(ACTION_FORWARD);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 5:
                playbackAction.setAction(ACTION_REVERSE);
                return PendingIntent.getService(this,actionNumber,playbackAction,0);
            case 6:
                if(!mediaPlayer.isPlaying())
                {
                    Log.e("is playing",mediaPlayer.isPlaying()+"");
                    playbackAction.setAction(ACTION_STOP);
                    //removeNotification();
                    return  PendingIntent.getService(this, actionNumber,playbackAction,0);
                }
                else
                {
                    playbackAction.setAction(ACTION_PLAY);
                    return  PendingIntent.getService(this, 0,playbackAction,0);
                }

            default:
                break;
        }
        return null;
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return; //mediaSessionManager exists

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        transportControls = mediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        //Set mediaSession's MetaData
        updateMetaData();

        // Attach Callback to receive MediaSession updates
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();

            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();

            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();

                skipToNext();
                updateMetaData();
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();

                skipToPrevious();
                updateMetaData();

            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
                seekTo(position);

            }


        });
    }
    private BroadcastReceiver onlineStop = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            removeNotification();
            stopSelf();
        }
    };


    public void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
        stopSelf();
        stopForeground(true);
       // onDestroy();
       // stopForeground(true);
    }
   /* public boolean isplaying()
    {
        if(mediaPlayer.isPlaying()==true)
        {
            return true;
        }
        else
        {
            return false;
        }
    }*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAudioFocusChange(int i) {

        //Invoked when the audio focus of the system is updated.
        switch (i) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mediaPlayer == null) initMediaPlayer();
               // else if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mediaPlayer.isPlaying()) mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
               // if (mediaPlayer.isPlaying()) mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        //Invoked when playback of a media source has completed.

        if (flag==false)
        {
            skipToNext();
            updateMetaData();
            intent.putExtra("songIndex",songIndex);
            sendBroadcast(intent);

        }
        else if(flag==true)
        {
            mediaPlayer.setLooping(true);
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        //i is that what is the error
        switch (i) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + i1);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + i1);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        playMedia();
        buildNotification(PlaybackStatus.PLAYING);
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

        if (!mediaPlayer.isPlaying()){
            playMedia();
            Toast.makeText(this,
                    "SeekComplete", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean requestAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocus(this);
    }


    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    /**
     * Handle PhoneState changes
     */
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (mediaPlayer != null) {
                            pauseMedia();
                           // ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                //resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public boolean onUnbind(Intent intent) {
       // mediaSession.release();
        //removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopMedia();
            mediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);


        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(repeatReceiver);
        unregisterReceiver(onlineStop);

        handler.removeCallbacks(sendUpdatesToUI);

        //clear cached playlist
        new StorageUtility(getApplicationContext()).clearCachedAudioPlaylist();
    }

    private void register_playNewAudio() {
        //Register playNewMedia receiver
        IntentFilter filter = new IntentFilter(songsList.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }
    private void initMediaPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mediaPlayer.reset();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location

            Log.e("path",activeSong.getSongUrl());
            mediaPlayer.setDataSource(activeSong.getSongUrl());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mediaPlayer.prepareAsync();
    }

    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(seekPosition);
            mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();

            buildNotification(PlaybackStatus.PAUSED);
        }
    }
   private void seekTo(long position)
   {
       if(position<mediaPlayer.getDuration()-5000&&position>mediaPlayer.getCurrentPosition()-5000)
       {
           mediaPlayer.seekTo((int)position);
       }
   }


    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();

            buildNotification(PlaybackStatus.PLAYING);

        }
    }

    private void skipToNext() {

        if (songIndex == songList.size() - 1) {
            //if last in playlist
            songIndex = 0;
            activeSong = songList.get(songIndex);
        } else {
            //get next in playlist
            activeSong = songList.get(++songIndex);
        }

       /* Intent intent=new Intent("updateSongNameUI");
        intent.putExtra("songIndex",activeSong);
        sendBroadcast(intent);*/

        //Update stored index
        new StorageUtility(getApplicationContext()).storeAudioIndex(songIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {

        if (songIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            songIndex = songList.size() - 1;
            activeSong = songList.get(songIndex);
        } else {
            //get previous in playlist
            activeSong = songList.get(--songIndex);
        }

       /* Intent intent=new Intent("updateSongNameUI");
        intent.putExtra("songIndex",activeSong);
        sendBroadcast(intent);*/
        //Update stored index
        new StorageUtility(getApplicationContext()).storeAudioIndex(songIndex);

        stopMedia();
        //reset mediaPlayer
        mediaPlayer.reset();
        initMediaPlayer();
    }

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //Get the new media index form SharedPreferences
            songList = new StorageUtility(getApplicationContext()).loadAudio();
            songIndex = new StorageUtility(getApplicationContext()).loadAudioIndex();
            if (songIndex != -1 && songIndex < songList.size()) {
                //index is in a valid range
                activeSong = songList.get(songIndex);
            } else {
                songIndex=0;
                activeSong=songList.get(songIndex);
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia();
            mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };
/*
   private BroadcastReceiver keepPlaying=new BroadcastReceiver() {
       @Override
       public void onReceive(Context context, Intent intent) {
           if(mediaPlayer.isPlaying())
           {
               buildNotification(PlaybackStatus.PLAYING);
           }
           else
           {
               buildNotification(PlaybackStatus.PAUSED);
           }

       }
   };*/



        private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                R.drawable.playingmusic); //replace with medias albumArt
        // Update the current metadata
                mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeSong.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeSong.getTitle())
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeSong.getTitle())
                        .build());


    }


}