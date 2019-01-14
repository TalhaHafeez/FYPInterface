package com.example.rabiaqayyum.fypinterface;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Sohail299 on 2018-07-24.
 */

public class CatagoryAdapterClass extends BaseAdapter{

    private static LayoutInflater inflate =null;
    private Context context;
    private ArrayList<String> name= new ArrayList<String>();
    private ArrayList<String> img= new ArrayList<String>();
    private ArrayList<String> url= new ArrayList<String>();
    private ArrayList<String> artist= new ArrayList<String>();
    public CatagoryAdapterClass(Context context, ArrayList<String> name, ArrayList<String> img, ArrayList<String> url, ArrayList<String> artist) {

        this.name=name;
        this.img=img;
        this.url=url;
        this.artist=artist;
        this.context=context;
        inflate=LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return name.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {


        view= inflate.inflate(R.layout.custom_class,null);
        TextView tv= (TextView) view.findViewById(R.id.song_name);
        ImageView iv = (ImageView) view.findViewById(R.id.song_dp);
        TextView tvartist= (TextView) view.findViewById(R.id.tvart);
        tv.setText(name.get(i));
        tvartist.setText(artist.get(i));
        Picasso.get().load(img.get(i)).into(iv);


        // Picasso.with(context).load(dp.get(i)).into(iv);
        tv.setText(name.get(i));
        return view;
    }
}