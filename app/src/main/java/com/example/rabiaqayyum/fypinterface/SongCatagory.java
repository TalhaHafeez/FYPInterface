package com.example.rabiaqayyum.fypinterface;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Iterator;

public class SongCatagory extends AppCompatActivity {

    DatabaseReference ref;
    ArrayList <String> name,url,img,artist;
    CatagoryAdapterClass adapter;
    private ProgressDialog dialog;
    ListView lv;
    private Boolean isConnected;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.songcatagory);
        ref=FirebaseDatabase.getInstance().getReference();
        lv= (ListView) findViewById(R.id.catagory_list);
        Intent i= getIntent();
        String mood= i.getStringExtra("playlistName");
         dialog = ProgressDialog.show(SongCatagory.this, "",
                "Loading. Please wait...", true);
         dialog.setCancelable(true);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                 if (isConnectedToInternet()) {
                     dialog.show();
                     Intent intent = new Intent(SongCatagory.this, OnlinePlay.class);
                     intent.putExtra("name", name.get(i));
                     intent.putExtra("img", img.get(i));
                     intent.putExtra("url", url.get(i));
                     intent.putExtra("artist", artist.get(i));
                     intent.putExtra("index",i);
                     startActivity(intent);
                 }
                 else
                 {
                     Toast.makeText(SongCatagory.this, "Please Check your internet", Toast.LENGTH_SHORT).show();
                 }
             }
         });
        name=new ArrayList<String>();
        url=new ArrayList<String>();
        img=new ArrayList<String>();
        artist=new ArrayList<String>();

        SharedDataSongs.name=new ArrayList<String>();
        SharedDataSongs.url=new ArrayList<String>();
        SharedDataSongs.img=new ArrayList<String>();
        SharedDataSongs.artist=new ArrayList<String>();


        ref.child("Mp3").child("Playlist").child(mood).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dialog.show();
                Iterator i= dataSnapshot.getChildren().iterator();
                int j=0;
                while(i.hasNext())
                {
                    String parentKey = (((DataSnapshot) i.next()).getKey());
                    String name1=dataSnapshot.child(parentKey).child("Title").getValue().toString();
                    String imgg=dataSnapshot.child(parentKey).child("Img").getValue().toString();
                    String urll=dataSnapshot.child(parentKey).child("Uri").getValue().toString();
                    String artistt=dataSnapshot.child(parentKey).child("Artist").getValue().toString();
                    name.add(j,name1);
                    img.add(j,imgg);
                    url.add(j,urll);
                    artist.add(j,artistt);

                    SharedDataSongs.name.add(j,name1);
                    SharedDataSongs.img.add(j,imgg);
                    SharedDataSongs.url.add(j,urll);
                    SharedDataSongs.artist.add(j,artistt);

                    j++;
                }
                adapter= new CatagoryAdapterClass(getApplicationContext(), name,img,url,artist);
                lv.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (dialog.isShowing())
                {
                    dialog.dismiss();
                }
            }
        });
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