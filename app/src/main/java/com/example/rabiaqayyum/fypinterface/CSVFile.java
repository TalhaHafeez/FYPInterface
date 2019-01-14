package com.example.rabiaqayyum.fypinterface;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Environment.getExternalStorageDirectory;

public class CSVFile {

   public List<String[]> readFile() throws FileNotFoundException {
       List<String[]> nextLine=new ArrayList<>();
       Log.e("if exist","no");
    String[] data;
    String path=getExternalStorageDirectory().getAbsolutePath() + File.separator+"EmotionBasedMusicPlayer/EmotionScoreCSV.csv";
       File csvfile = new File(path);
       //File file = new File(csvfile);
       if (csvfile.exists())
       {
           Log.e("if exist","yes");
           BufferedReader br = new BufferedReader(new FileReader(csvfile));
           try
           {
               String csvLine;

               while ((csvLine = br.readLine()) != null)
               {
                   Log.e("if read","yes");
                   data=csvLine.split(",");
                   try
                   {
                       Log.e("data",data[0]+""+data[1]+""+data[2]);
                       nextLine.add(data);
                   }
                   catch (Exception e)
                   {
                       Log.e("Problem",e.toString());
                   }
               }
           }
           catch (IOException ex)
           {
               throw new RuntimeException("Error in reading CSV file: "+ex);
           }
       }
       else
       {
           Log.e("if exist","no");
       }

       return nextLine;
   }
}

