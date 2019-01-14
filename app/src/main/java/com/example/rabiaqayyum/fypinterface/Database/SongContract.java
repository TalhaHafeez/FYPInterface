package com.example.rabiaqayyum.fypinterface.Database;

import android.net.Uri;
import android.provider.BaseColumns;

public class SongContract
{
    private SongContract()
    {

    }
    public static final class SongsTable implements BaseColumns {

        public final static String TABLE_NAME = "Songs";

        public final static String COLUMN_Song_ID = "song_id";

        public final static String COLUMN_Song_Path = "song_path";

        public final static String COLUMN_Song_duration="song_duration";
        public final static String COLUMN_Song_album="song_album";


        public final static String COLUMN_Song_Name ="song_name";


        public final static String COLUMN_Favourite_id = "favourite_id";

        public final static int nFavourite_id=0;
        public final static int yFavourite_id=1;

    }


    public static final class PlaylistsTable implements BaseColumns {

        /** Name of database table for devices */

        public final static String TABLE_NAME = "Playlists";

        public final static String COLUMN_Playlist_ID = "playlist_id";

        public final static String COLUMN_Playlist_Name = "playlist_name";

        public static final String playlist_happy="Happy";
        public static final String playlist_sad="Sad";
        public static final String playlist_neutral="Neutral";
        public static final String playlist_angry="Angry";
        public static final String playlist_surprise="Surprise";
        public static final String playlist_fear="Fear";


    }
    public static final class PlaylistsSongs_AssocTable implements BaseColumns {
        public final static String TABLE_NAME = "PlaylistsSongs_Assoc";

        public final static String COLUMN_Song_ID = "song_id";

        public final static String COLUMN_Playlist_ID = "playlist_id";

    }


}

