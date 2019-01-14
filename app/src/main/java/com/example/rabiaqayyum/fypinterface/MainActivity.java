package com.example.rabiaqayyum.fypinterface;

import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.example.rabiaqayyum.fypinterface.Database.SongDatabaseHelper;
import com.example.rabiaqayyum.fypinterface.emotionDetection.emotionDetectionClass;
import com.opencsv.CSVWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    CSVWriter writer;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    AddingRemovingSongs ars;
    emotionDetectionClass edc;
    FloatingActionButton fab;
    private Fragment fragment;
    private FrameLayout mapContent;
    public DrawerLayout drawerLayout;
    final int request_code=100;
    SongDatabaseHelper sdh;
    boolean isConnected = false;
    private boolean fragmentCheck = true;
    private NavigationView navigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*on the first run the app asks for all permissions*/
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            Intent intent = new Intent(this, SplashScreenActivity.class);
            startActivity(intent);
            finish();
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            editor.commit();
        }
        /*create the file for storing emotions and their score for the user*/
        File sdCard = Environment.getExternalStorageDirectory();
        Log.e("sdcard"," "+sdCard.getAbsolutePath());
        String dirUrl = "/EmotionBasedMusicPlayer";
        String direc = sdCard.getAbsolutePath() + dirUrl;
        File dir = new File(direc);
        if(!dir.exists()) {
            File dir1 = new File(sdCard.getAbsolutePath()+"/EmotionBasedMusicPlayer");
            dir1.mkdirs();
            String fileUrl="/EmotionScoreCSV.csv";
            String fileD=direc+fileUrl;
            File file=new File(fileD);
            try {
                file.createNewFile();
                writer = new CSVWriter(new FileWriter(fileD));
                String data[]={"EmotionLabel","EmotionScore","Time"};
                writer.writeNext(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /* Create the layout that will hold the TextView. */
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        /** Add a TextView and set the initial text. */
        TextView textView = new TextView(this);
        textView.setTextSize(50);
        textView.setText("Home");
        mainLayout.addView(textView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        /* Set the mainLayout as the content view */
        setContentView(R.layout.activity_main);
        sdh=new SongDatabaseHelper(this);
        fab= (FloatingActionButton) findViewById(R.id.fab5);
        mapContent = (FrameLayout) findViewById(R.id.mapContent);
        /*button to turn on the camera and take and store image*/
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,request_code);
            }
        });
        /*playlists are added the first time app is installed*/
        if(sdh.getPlaylists().isEmpty())
        {
            sdh.insertPlaylist();
        }
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*the drawer layout to add the navigation drawer and opening and closing of naviagtion view*/
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //check if the mobile is connected to internet so that autogenerated tab is shown
        isConnectedToInternet();
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Custom Playlist"));
        if(isConnected) {
            tabLayout.addTab(tabLayout.newTab().setText("Auto Generated Playlist"));
        }
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        final PagerAdapter adapter = new ViewPagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
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

    //when navigation item is clicked
    public boolean onNavigationItemSelected(MenuItem item) {
        displaySelectedScreen(item.getItemId());
        return true;
    }
    private void displaySelectedScreen(int itemId) {
        fragment = null;
        switch (itemId) {
            case R.id.home:
                /*show the same fragment as of home with title as home*/
                tabLayout.setVisibility(View.VISIBLE);
                mapContent.setVisibility(View.VISIBLE);
                fragment = new Fragment();
                toolbar.setTitle("FYPInterface");

                fragmentCheck = true;
                break;
            case R.id.help:
                mapContent.setVisibility(View.GONE);
                tabLayout.setVisibility(View.GONE);
                fragment = new Help();
                fragmentCheck = false;
                break;

            case R.id.stats:
                mapContent.setVisibility(View.GONE);
                tabLayout.setVisibility(View.GONE);
                fragment = new UserStatistics();
                fragmentCheck = false;
                break;
        }
        //make a fragment transaction to switch between fragments
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
           // ft.addToBackStack(null);
            ft.commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }
    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        int numTabs;
        public ViewPagerAdapter(FragmentManager manager,int numTabs)
        {
            super(manager);
            this.numTabs=numTabs;
        }
        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0:
                    FragmentOne f1=new FragmentOne();
                    return f1;
                case 1:
                    FragmentThree f3=new FragmentThree();
                    return f3;
                default:
                    return  null;
            }
        }
        @Override
        public int getCount()
        {
            return numTabs;
        }
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
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                edc = new emotionDetectionClass(this, byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (requestCode == 7 && resultCode == RESULT_OK) {
            String song[];
            Uri uri = data.getData();
            if (uri != null) {
                song = getRealPathFromURI(this, uri);
                for (int i = 0; i < song.length; i++) {
                    Log.e("path", song[i]);
                }
            } else {
                song = null;
            }
            ars.addToDatabaseAndListView(song[1], song[0], Float.parseFloat(song[2]));
        }
    }
   /* @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean back = false;
        if(keyCode == KeyEvent.KEYCODE_BACK){
            back = true;
           // backStack();
        }
        return back;
    }
*/
    @Override
    public void onBackPressed() {
        if(fragmentCheck){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            else
            {
                super.onBackPressed();
            }
        }
        else {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
            navigationView.getMenu().getItem(0).setChecked(true);
            displaySelectedScreen(R.id.home);
        }
    }
}
