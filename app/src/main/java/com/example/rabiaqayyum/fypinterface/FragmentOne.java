package com.example.rabiaqayyum.fypinterface;

import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rabiaqayyum.fypinterface.emotionDetection.emotionDetectionClass;

import java.io.ByteArrayOutputStream;

import static android.app.Activity.RESULT_OK;


public class FragmentOne extends Fragment {

    static FragmentOne context;

   // Button play;
    CardView b2,b3,b4,b5,b6,b7;
    Camera mCamera;
    boolean mPreviewRunning = false;
    AddingRemovingSongs ars;
    emotionDetectionClass edc;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_fragment_one, container, false);
        context=this;
        b2= (CardView) view.findViewById(R.id.card_happy);
        b3= (CardView) view.findViewById(R.id.card_sad);
        b4= (CardView) view.findViewById(R.id.card_angry);
        b5= (CardView) view.findViewById(R.id.card_surprise);
        b6= (CardView) view.findViewById(R.id.card_fear);
        b7= (CardView) view.findViewById(R.id.card_neutral);


        //*************************************ON CLICK LISTENERS******************************************
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Happy");
                startActivity(myIntent);
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Sad");
                startActivity(myIntent);
            }
        });

        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Angry");
                startActivity(myIntent);
            }
        });

        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Surprise");
                startActivity(myIntent);
            }
        });

        b6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Fear");
                startActivity(myIntent);
            }
        });

        b7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getActivity(), songsList.class);
                myIntent.putExtra("playlistName","Neutral");
                startActivity(myIntent);
            }
        });
        return view;
    }
    private String[] getRealPathFromURI(Context context, Uri contentUri) {
        String[] song=new String[4];
        CursorLoader loader = new CursorLoader(context, contentUri, null, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int path_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        int name_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        int duration_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
        int artist_index=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        cursor.moveToFirst();
        //do {
            String songPath=cursor.getString(path_index);
            String songName=cursor.getString(name_index);
            String songDuration=cursor.getString(duration_index);
            String songArtist=cursor.getString(artist_index);

            song[0]=songPath;
            song[1]=songName;
            song[2]=songDuration;
            song[3]=songArtist;
       // }while (cursor.moveToNext());
        return song;

    }
    @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100&&resultCode==RESULT_OK)
        {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            if(imageBitmap==null)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(false);
                builder.setTitle("Detection failed!!");
                builder.setMessage("try again");
                builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //the user clicked on Cancel
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byte[] byteArray = bStream.toByteArray();
            try {
                edc=new emotionDetectionClass(getContext(),byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(requestCode==7 && resultCode==RESULT_OK)
        {
            String song[];
            Uri uri=data.getData();
            if(uri!=null)
            {
                song=getRealPathFromURI(getContext(),uri);
                for(int i=0;i<song.length;i++) {
                    Log.e("path", song[i]);
                }
            }
            else
            {
                song=null;
            }
            ars.addToDatabaseAndListView(song[1],song[0],Float.parseFloat(song[2]));

        }
    }
}
