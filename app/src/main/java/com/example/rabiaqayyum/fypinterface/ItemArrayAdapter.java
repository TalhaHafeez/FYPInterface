package com.example.rabiaqayyum.fypinterface;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import pl.droidsonroids.gif.GifImageView;

public class ItemArrayAdapter extends ArrayAdapter {
    private ArrayList<String[]> scoreList = new ArrayList();

    static class ItemViewHolder
    {
        TextView name;
        TextView score;
        TextView time;
        GifImageView emotionGif;
    }

    public ItemArrayAdapter(Context context, int textViewResourceId)
    {
        super(context, textViewResourceId);
    }
    public void add(String[] nlist)
    {
        scoreList.add(nlist);
        Log.e("added","yes");
        super.add(nlist);
    }
    @Override
    public int getCount()
    {
        return this.scoreList.size();
    }

    @Override
    public String[] getItem(int index) {
        return this.scoreList.get(index);
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ItemViewHolder viewHolder;
        if (row == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.item_layout, parent, false);
            viewHolder = new ItemViewHolder();
            viewHolder.name = (TextView) row.findViewById(R.id.name);
            viewHolder.score = (TextView) row.findViewById(R.id.score);
            viewHolder.time=(TextView)row.findViewById(R.id.time);
            viewHolder.emotionGif=(GifImageView)row.findViewById(R.id.emotionGif);

            row.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ItemViewHolder)row.getTag();
        }
        Log.e("adapter","true");
        String[] stat = getItem(position);
        String name=stat[0];
        if(name.equalsIgnoreCase("\"happy\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.hapyy);
        }
        else if(name.equalsIgnoreCase("\"angry\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.angryy);
        }
        else if (name.equalsIgnoreCase("\"neutral\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.neutrall);
        }
        else if(name.equalsIgnoreCase("\"surprise\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.surprisedd);
        }
        else if(name.equalsIgnoreCase("\"fear\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.fearr);
        }
        else if(name.equalsIgnoreCase("\"sad\""))
        {
            viewHolder.emotionGif.setImageResource(R.drawable.sadd);
        }
        viewHolder.name.setText(stat[0]);
        viewHolder.score.setText(stat[1]);
        viewHolder.time.setText(stat[2]);
        return row;
    }
}