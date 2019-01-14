package com.example.rabiaqayyum.fypinterface;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.rabiaqayyum.fypinterface.Database.SongContract;
import com.example.rabiaqayyum.fypinterface.Database.SongDatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class customListAdaptor extends RecyclerView.Adapter<customListAdaptor.SongViewHolder>
{
    private static ArrayList<Song> songs;
    private ArrayList<Song> favSongs;
    //private ArrayList<Images> theImages;
    SongDatabaseHelper sdh;
   // SharedPreference sharedPreference;
    Context songsListContext;

    private static String playlistName;
    private static ClickListener clicklistener = null;
    public Context context;


    public static class SongViewHolder extends RecyclerView.ViewHolder  {
        CardView cv;
        ImageView icon;
        TextView Itemname;
        Button menuButton;
        RelativeLayout main;

        CheckBox star;

        SongViewHolder(final View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.mylist);
            Itemname = (TextView) itemView.findViewById(R.id.Itemname);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            menuButton=(Button) itemView.findViewById(R.id.menubuton);
            main=(RelativeLayout)itemView.findViewById(R.id.main);
            star=(CheckBox) itemView.findViewById(R.id.star);
          //  icon.setImageResource(R.drawable.default_artwork);

           // star.setOnClickListener(this);
            main.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v) {
                    if(clicklistener !=null){
                        clicklistener.itemClicked(v,getAdapterPosition(),playlistName);
                    }
                }
            });

            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(star.isChecked()) {
                        clicklistener.favouriteButton(view,getAdapterPosition(), playlistName,false);
                    }
                    else
                    {
                        clicklistener.favouriteButton(view, getAdapterPosition(), playlistName,true);
                    }
                }
            });


        }


    }

    public void setClickListener(ClickListener clickListener){
        this.clicklistener = clickListener;
    }
    customListAdaptor(Context context,ArrayList<Song> songs,String playlistName)
    {
        this.songs=songs;
        this.playlistName=playlistName;
        this.songsListContext=context;
        sdh=new SongDatabaseHelper(context);
        favSongs=new ArrayList<>();
        //sharedPreference = new SharedPreference();

        //this.theImages=theImages;
    }

    @Override
    public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.songslist, parent, false);
        SongViewHolder pvh = new SongViewHolder(v);
        return pvh;
    }


    @Override
    public void onBindViewHolder(final SongViewHolder  holder, final int position) {

        context =holder.itemView.getContext();
        holder.Itemname.setText(songs.get(position).getTitle());
        holder.menuButton.setOnClickListener(new View.OnClickListener() {
          //  int position=holder.getLayoutPosition();
            @Override
            public void onClick(View view) {
                clicklistener.popUpClicked(view,position);
               // notifyDataSetChanged();

            }
        });

        if(checkFavouriteSong(songs.get(position)))
        {
            holder.star.setChecked(true);
        }
        else
        {
            holder.star.setChecked(false);
        }


       /* SharedPreferences sharedPreferences = context.getSharedPreferences("tgpref1",Context.MODE_PRIVATE);
        boolean tgpref = sharedPreferences.getBoolean("tgpref",true);


        if (tgpref){
            holder.star.setChecked(true);
        }else {
            holder.star.setChecked(false);
        }
*/



        //  Images image= theImages.get(position);

            byte[] image=songs.get(position).getImageResource();
            Bitmap songDp=BitmapFactory.decodeByteArray(image,0,image.length);
            holder.icon.setImageBitmap(songDp);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
    public boolean checkFavouriteSong(Song song)
    {
        boolean check=false;
        Cursor favourites=sdh.getFavouriteSongs(playlistName);

        if(favourites!=null&& favourites.moveToNext()==true) {
            favourites.moveToFirst();

            do {
                int songIdIndex = favourites.getColumnIndex(SongContract.SongsTable.COLUMN_Song_ID);
                int songNameIndex = favourites.getColumnIndex(SongContract.SongsTable.COLUMN_Song_Name);
                int songPathIndex = favourites.getColumnIndex(SongContract.SongsTable.COLUMN_Song_Path);
                int songDurationIndex = favourites.getColumnIndex(SongContract.SongsTable.COLUMN_Song_duration);

                int songId = favourites.getInt(songIdIndex);
                String songName = favourites.getString(songNameIndex);
                String songPath = favourites.getString(songPathIndex);
                float songDuration = favourites.getFloat(songDurationIndex);

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(songPath);
                byte[] coverBytes = retriever.getEmbeddedPicture();

                if (coverBytes==null)
                {
                    Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.playingmusic);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    coverBytes = stream.toByteArray();
                }
                favSongs.add(new Song(songId, songName, songPath, songDuration,coverBytes));
            } while (favourites.moveToNext());
        }


        if(favSongs!=null)
        {
            for(Song song1:favSongs)
            {
                if(song1.getID()==song.getID())
                {
                    check=true;
                    break;
                }
            }
        }
        return check;
    }
    /*
    TextView txtTitle;
    Activity context;

    private ArrayList<Song> songs;
    private ArrayList<Images> theImages;
    public customListAdaptor( Activity c, ArrayList<Song> theSongs,ArrayList<Images> theImages)
    {
        this.context=c;
        songs=theSongs;
        this.theImages=theImages;
    }


    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.songslist, null, true);

        txtTitle = (TextView) rowView.findViewById(R.id.Itemname);

        //get song using position
        Song currSong = songs.get(position);

        Images image= theImages.get(position);

        txtTitle.setText(currSong.getTitle());


        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        if(image.getSongImage()!=null) {
            Bitmap songCover = BitmapFactory.decodeByteArray(image.getSongImage(), 0, image.getSongImage().length);
            imageView.setImageBitmap(songCover);
        }
        else
        {
            imageView.setImageResource(R.drawable.music);
        }
        return rowView;
    }*/
}
