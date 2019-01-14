package com.example.rabiaqayyum.fypinterface.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.rabiaqayyum.fypinterface.Database.SongContract.SongsTable;
//import com.example.rabiaqayyum.fypinterface.Database.SongContract.FavouritesTable;
import com.example.rabiaqayyum.fypinterface.Database.SongContract.PlaylistsTable;
import com.example.rabiaqayyum.fypinterface.Database.SongContract.PlaylistsSongs_AssocTable;

import java.util.ArrayList;

public class SongDatabaseHelper extends SQLiteOpenHelper
{

    private final String query_song_id="SELECT "+SongsTable.COLUMN_Song_ID+" FROM Songs WHERE song_name=?";
    private final String query_playlist_id="SELECT "+PlaylistsTable.COLUMN_Playlist_ID+" FROM Playlists WHERE playlist_name=?";



    private final String query_getHappySongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
                                              +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_happy+"'";

    private final String query_getSadSongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_sad+"'";

    private final String query_getFearSongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_fear+"'";

    private final String query_getAngrySongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_angry+"'";

    private final String query_getSurpiseSongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_surprise+"'";

    private final String query_getNeutralSongs="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_neutral+"'";



    private final String query_song_names_with_playlist="SELECT "+SongsTable.COLUMN_Song_Path+" FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"=?";


    private final String query_getHappyFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_happy+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";

    private final String query_getSadFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_sad+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";

    private final String query_getAngryFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_angry+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";

    private final String query_getNeutralFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_neutral+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";

    private final String query_getSurpiseFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_surprise+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";

    private final String query_getFearFav="SELECT * FROM "+SongsTable.TABLE_NAME+" JOIN "+PlaylistsSongs_AssocTable.TABLE_NAME+" ON "+ SongsTable.TABLE_NAME+"."+SongsTable.COLUMN_Song_ID+"="+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Song_ID+" JOIN "
            +PlaylistsTable.TABLE_NAME+" ON "+PlaylistsSongs_AssocTable.TABLE_NAME+"."+PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+"="+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_ID+" WHERE "+PlaylistsTable.TABLE_NAME+"."+PlaylistsTable.COLUMN_Playlist_Name+"="+"'"+PlaylistsTable.playlist_fear+"' AND "+SongsTable.COLUMN_Favourite_id+"="+"1";


    private static final String DATABASE_NAME = "CustomSongs.db";

    private static final int DATABASE_VERSION = 1;


    private static final String query_getFavStatus= "";

    public SongDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    Context context;
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        String SQL_CREATE_PLAYLISTS_TABLE =  "CREATE TABLE " + PlaylistsTable.TABLE_NAME + " ("

                + PlaylistsTable.COLUMN_Playlist_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PlaylistsTable.COLUMN_Playlist_Name + " TEXT);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYLISTS_TABLE);



        String SQL_CREATE_SONGS_TABLE =  "CREATE TABLE " + SongsTable.TABLE_NAME + " ("
                + SongsTable.COLUMN_Song_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SongsTable.COLUMN_Song_Name + " TEXT NOT NULL, "
                + SongsTable.COLUMN_Song_Path + " TEXT NOT NULL , "
                + SongsTable.COLUMN_Favourite_id+" INTEGER, "
                + SongsTable.COLUMN_Song_duration + " FLOAT);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_SONGS_TABLE);



        String SQL_CREATE_PLAYLISTSSONGS_ASSOC_TABLE =  "CREATE TABLE " + PlaylistsSongs_AssocTable.TABLE_NAME + " ("

                + PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+ " TEXT, "
                + PlaylistsSongs_AssocTable.COLUMN_Song_ID + " TEXT, "
                + " FOREIGN KEY ("+ PlaylistsSongs_AssocTable.COLUMN_Song_ID+") REFERENCES "+ SongsTable.TABLE_NAME +"("+ SongsTable.COLUMN_Song_ID+")ON UPDATE CASCADE ON DELETE CASCADE, "
                + " FOREIGN KEY ("+ PlaylistsSongs_AssocTable.COLUMN_Playlist_ID+") REFERENCES "+ PlaylistsTable.TABLE_NAME +"("+ PlaylistsTable.COLUMN_Playlist_ID+")ON UPDATE CASCADE ON DELETE CASCADE);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_PLAYLISTSSONGS_ASSOC_TABLE);

    }


    public boolean insertSong(String song_name,String song_path,float song_duration,int favourite_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(SongContract.SongsTable.COLUMN_Song_Name,song_name);
        values.put(SongContract.SongsTable.COLUMN_Song_Path,song_path);
        values.put(SongsTable.COLUMN_Song_duration,song_duration);
        values.put(SongContract.SongsTable.COLUMN_Favourite_id,favourite_id);

        Log.e("insertion",values.toString());
        db.insert(SongContract.SongsTable.TABLE_NAME,null,values);

        return true;
    }
    public boolean insertPlaylist()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_happy);//1
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_angry);//2
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_sad);//3
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_fear);//4
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_neutral);//5
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        values.put(SongContract.PlaylistsTable.COLUMN_Playlist_Name,SongContract.PlaylistsTable.playlist_surprise);//6
        db.insert(PlaylistsTable.TABLE_NAME,null,values);

        return true;
    }

    public boolean updateSongs(int id,int favourite_value)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(SongsTable.COLUMN_Favourite_id,favourite_value);
        String where= ""+SongsTable.COLUMN_Song_ID+" = ? ";
        String[] whereArgs = new String[] {String.valueOf(id)};
        db.update(SongsTable.TABLE_NAME,values,where,whereArgs);
        return  true;

    }


    public boolean checkFavStatus(int id){
        String songId = String.valueOf(id);
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            Cursor c = db.rawQuery("SELECT "+SongsTable.COLUMN_Favourite_id+ " FROM "+SongsTable.TABLE_NAME+" WHERE "+SongsTable.COLUMN_Song_ID +"=?", new String[]{songId});
            c.moveToFirst();
            if(c.getInt(0)==1){
                return true;
            }
            return false;

        }
        catch (Exception e){
            e.printStackTrace();
        }

//        c.moveToFirst();
//        if(c.getInt(0)==1){
//            return true;
//        }
            return false;

    }


    public Cursor getFavouriteSongs(String playlistName)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res=null;
        if(playlistName.equals(PlaylistsTable.playlist_happy))
        {
            res=db.rawQuery(query_getHappyFav,null);
            return res;
        }
        else if(playlistName.equals(PlaylistsTable.playlist_sad))
        {
            res=db.rawQuery(query_getSadFav,null);
            return res;
        }
        else if(playlistName.equals(PlaylistsTable.playlist_angry))
        {
            res=db.rawQuery(query_getAngryFav,null);
            return res;
        }
        else if(playlistName.equals(PlaylistsTable.playlist_neutral))
        {
            res=db.rawQuery(query_getNeutralFav,null);
            return res;
        }
        else if(playlistName.equals(PlaylistsTable.playlist_fear))
        {
            res=db.rawQuery(query_getFearFav,null);
            return res;
        }
        else if(playlistName.equals(PlaylistsTable.playlist_surprise))
        {
            res=db.rawQuery(query_getSurpiseFav,null);
            return res;
        }
        else
        {
            Log.e("fav","error ");
            return res;
        }

    }

    public ArrayList<String> getSongPath(String playlistName)
    {
        ArrayList<String> paths=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( query_song_names_with_playlist, new String[]{String.valueOf(playlistName)} );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            paths.add(res.getString(res.getColumnIndex(SongsTable.COLUMN_Song_Path)));
            res.moveToNext();
        }

        return paths;
    }

    public boolean insertPlaylistsSongs_Assoc(String song_name,String playlist_name)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor getSongId=db.rawQuery(query_song_id,new String[]{String.valueOf(song_name)});
        int song_name_index=getSongId.getColumnIndex(SongContract.SongsTable.COLUMN_Song_ID);

        getSongId.moveToNext();

        String song_id=getSongId.getString(song_name_index);

        Cursor getPlaylistId=db.rawQuery(query_playlist_id,new String[]{String.valueOf(playlist_name)});
        int playlist_name_index=getPlaylistId.getColumnIndex(SongContract.PlaylistsTable.COLUMN_Playlist_ID);

        getPlaylistId.moveToNext();


        String playlist_id=getPlaylistId.getString(playlist_name_index);

        SQLiteDatabase db2 = this.getWritableDatabase();

        ContentValues values2 = new ContentValues();
        values2.put(PlaylistsSongs_AssocTable.COLUMN_Playlist_ID, playlist_id);
        values2.put(PlaylistsSongs_AssocTable.COLUMN_Song_ID , song_id);
        db2.insert(PlaylistsSongs_AssocTable.TABLE_NAME,null,values2);
        return true;
    }
    public ArrayList<String> getPlaylists()
    {
        ArrayList<String> playlists=new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+PlaylistsTable.TABLE_NAME, null );
        res.moveToFirst();

        while(res.isAfterLast() == false){
            playlists.add(res.getString(res.getColumnIndex(PlaylistsTable.COLUMN_Playlist_Name)));
            res.moveToNext();
        }
        return playlists;
    }

    public int deleteData()
    {
        SQLiteDatabase db2 = this.getWritableDatabase();
        int rows_deleted=db2.delete(SongsTable.TABLE_NAME,null,null);
        if (rows_deleted>0)
        {
            db2.notifyAll();
        }
        return rows_deleted;
    }
    public int delete(Integer id)
    {
        SQLiteDatabase db1=this.getReadableDatabase();
        SQLiteDatabase db = this.getWritableDatabase();

        int rows= db.delete(SongsTable.TABLE_NAME,
                SongsTable.COLUMN_Song_ID+" = ? ",
                new String[] { Integer.toString(id) });
        /*if(rows!=0)
        {
            context.getContentResolver().notify();
        }*/
        Log.e("deleted rows",""+rows);
        return rows;
    }
    public Cursor getAllSongs(String Playlistname)
    {
        ArrayList<String> array_list = new ArrayList<String>();


        String PlaylistName=String.valueOf(Playlistname);

        Log.e("playlistname db",PlaylistName.toString());
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res=null;
        if(PlaylistsTable.playlist_happy.equals(PlaylistName))
        {
           res=db.rawQuery(query_getHappySongs,null);

        }
        else if(PlaylistsTable.playlist_sad.equals(PlaylistName))
        {
        res=db.rawQuery(query_getSadSongs,null);
        /*res.moveToNext();

        while(res.isAfterLast() == false){
            int SongIndex = res.getColumnIndex(SongsTable.COLUMN_Song_ID);
            array_list.add(res.getString(SongIndex));
            res.moveToNext();
        }
        return array_list;*/

        }
        else if(PlaylistsTable.playlist_fear.equals(PlaylistName))
        {
            res=db.rawQuery(query_getFearSongs,null);
            /*res.moveToNext();

            while(res.isAfterLast() == false){
                int SongIndex = res.getColumnIndex(SongsTable.COLUMN_Song_ID);
                array_list.add(res.getString(SongIndex));
                res.moveToNext();
            }
            return array_list;*/
        }
        else if(PlaylistsTable.playlist_angry.equals(PlaylistName))
        {
            res=db.rawQuery(query_getAngrySongs,null);
           /* res.moveToNext();

            while(res.isAfterLast() == false){
                int SongIndex = res.getColumnIndex(SongsTable.COLUMN_Song_ID);
                array_list.add(res.getString(SongIndex));
                res.moveToNext();
            }
            return array_list;*/
        }
        else if(PlaylistsTable.playlist_surprise.equals(PlaylistName))
        {
            res=db.rawQuery(query_getSurpiseSongs,null);
           /* res.moveToNext();

            while(res.isAfterLast() == false){
                int SongIndex = res.getColumnIndex(SongsTable.COLUMN_Song_ID);
                array_list.add(res.getString(SongIndex));
                res.moveToNext();
            }
            return array_list;*/
        }
        else if(PlaylistsTable.playlist_neutral.equals(PlaylistName))
        {
            res=db.rawQuery(query_getNeutralSongs,null);
           /* res.moveToNext();

            while(res.isAfterLast() == false){
                int SongIndex = res.getColumnIndex(SongsTable.COLUMN_Song_ID);
                array_list.add(res.getString(SongIndex));
                res.moveToNext();
            }
            return array_list;*/
        }
        else {
            //return array_list;
            Log.e("error","no playlist specified");
        }
        return res;
    }
    public Cursor getSingleSong(String song_path)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("SELECT * FROM "+SongsTable.TABLE_NAME+" WHERE "+SongsTable.COLUMN_Song_Path+"="+song_path+";",null);
        return res;
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1)
    {

    }
}
