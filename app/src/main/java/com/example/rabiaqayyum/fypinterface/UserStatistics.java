package com.example.rabiaqayyum.fypinterface;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.List;


public class UserStatistics extends Fragment {

    private ListView listView;
    private ItemArrayAdapter itemArrayAdapter;
    Toolbar toolbar;
   // DrawerLayout drawerLayout;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_userstatistics, container, false);
        getActivity().setTitle("User Statistics");
        listView = (ListView) view.findViewById(R.id.listView);
        LayoutInflater myinflater = getLayoutInflater();
        ViewGroup myHeader = (ViewGroup)myinflater.inflate(R.layout.headerlayout, listView, false);
        listView.addHeaderView(myHeader, null, false);
        listView.setDivider(null);
        itemArrayAdapter = new ItemArrayAdapter(this.getContext(), R.layout.item_layout);
        Parcelable state = listView.onSaveInstanceState();
        listView.setAdapter(itemArrayAdapter);
        listView.onRestoreInstanceState(state);

        CSVFile csvFile = new CSVFile();
        List<String[]> scoreList = null;
        try {
            scoreList = csvFile.readFile();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        if(scoreList!=null)
        {
            Log.e("null","false");
            for(String[] scoreData:scoreList ) {
                itemArrayAdapter.add(scoreData);
            }
        }
        else
        {
            Log.e("null","true");
        }

        return view;

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


}