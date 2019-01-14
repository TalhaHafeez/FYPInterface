package com.example.rabiaqayyum.fypinterface.emotionDetection;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.example.rabiaqayyum.fypinterface.FeedBack;

import com.opencsv.CSVWriter;

import com.example.rabiaqayyum.fypinterface.SongCatagory;
import com.example.rabiaqayyum.fypinterface.songsList;

public class emotionDetectionClass
{
    String emotionLabel;
    float emotionOutput;
    String formattedDate;
    Bitmap inputImg;

    private Executor executor = Executors.newSingleThreadExecutor();

    TensorflowImageClassifier classifier;
    FileWriter mFileWriter;

    ClassificationModel res;

    Context context;
   // cloudStorage cs;

    CropFace cf;
    private static final int MAX_FACE = 1   ;
    Bitmap grayImage;
    Bitmap resizedImage;

    public emotionDetectionClass(Context context,byte[] byteArray) throws Exception {
        this.context=context;
        initTensorFlowAndLoadModel();

        this.inputImg = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        callDetection();

    }
    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("start classifier","this will start classifier" );
                    classifier = TensorflowImageClassifier.create(
                            context.getAssets(),
                            "modeltflite.lite",
                            "labels.txt",
                            48);
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }
    public void callDetection() throws Exception {
        if(inputImg==null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setTitle("Detection failed!!");
            builder.setMessage("try again");
            builder.setNegativeButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //the user clicked on Cancel
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else
        {
            detectFace(inputImg);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void detectFace(Bitmap detectFaceImage) throws Exception {

       /* BitmapFactory.Options bitmapFatoryOptions=new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig=Bitmap.Config.RGB_565;
        detectFaceImage=BitmapFactory.decodeResource(getResources(), detectFaceImage,bitmapFatoryOptions);*/
        Bitmap converted = detectFaceImage.copy(Bitmap.Config.RGB_565, false);
        int w = converted.getWidth();
        int h = converted.getHeight();
        if (w % 2 == 1) {
            w++;
            converted = Bitmap.createScaledBitmap(converted,
                    converted.getWidth()+1, converted.getHeight(), false);
        }
        if (h % 2 == 1) {
            h++;
            converted = Bitmap.createScaledBitmap(converted,
                    converted.getWidth(), converted.getHeight()+1, false);
        }
        android.media.FaceDetector fdet_ = new android.media.FaceDetector(converted.getWidth(), converted.getHeight(), MAX_FACE);

        android.media.FaceDetector.Face[] fullResults = new android.media.FaceDetector.Face[MAX_FACE];
        int facecount=fdet_.findFaces(converted, fullResults);

        Log.e("face count",""+facecount);
        ArrayList<FaceResult> faces_=new ArrayList<>();
        for (int i = 0; i < MAX_FACE; i++) {
            Log.e("detect face","entered for loop" );

            FaceDetector.Face face = fullResults[i];
            if (face != null) {
                Log.e("detect face","if" );
                PointF mid = new PointF();
                face.getMidPoint(mid);

                float eyesDis = face.eyesDistance();
                float confidence = face.confidence();
                float pose = face.pose(FaceDetector.Face.EULER_Y);

                Log.e("detect face","rectangle" );
                Rect rect = new Rect(
                        (int) (mid.x - eyesDis * 1.20f),
                        (int) (mid.y - eyesDis * 0.55f),
                        (int) (mid.x + eyesDis * 1.20f),
                        (int) (mid.y + eyesDis * 1.85f));
                if (rect.height() * rect.width() > 15 * 15) {
                    Log.e("detect face","rectangle made");
                    FaceResult faceResult = new FaceResult();
                    faceResult.setFace(0, mid, eyesDis, confidence, pose, System.currentTimeMillis());
                    faces_.add(faceResult);

                    Bitmap cropedFace = cf.cropFace(faceResult, converted, 0);
                    if (cropedFace != null) {
                        Log.e("detect face","pass to preprocessing" );
                        preprocessing(cropedFace);
                    }

                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(false);
                    builder.setTitle("Face Detection Failed!!! Bring Camera closer to the face");
                    // builder.setMessage(");
                    builder.setNegativeButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //the user clicked on Cancel
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(false);
                builder.setTitle("Face not detected!");
                // builder.setMessage(");
                builder.setNegativeButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //the user clicked on Cancel
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        }
    }
    public  void preprocessing(Bitmap croppedFace) throws Exception {
        //  byte[] byteArray = getIntent().getByteArrayExtra("image");
        //Bitmap inputImg = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        grayImage = getGrayImage(croppedFace);
        resizedImage = getResizedImage(grayImage);
        getEmotion();
    }
    public Bitmap getGrayImage(Bitmap bitmap)
    {
        int width, height;
        height = bitmap.getHeight();
        width = bitmap.getWidth();

        Bitmap bitmapGray = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmapGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bitmap, 0, 0, paint);
        return bitmapGray;
    }
    public Bitmap getResizedImage(Bitmap bitmap)
    {
        Bitmap resizedImg=Bitmap.createScaledBitmap(bitmap, 48, 48, true);
        return resizedImg;
    }
    public void getEmotion() throws Exception {
        int imagePixel[];
        imagePixel = new int[resizedImage.getWidth() * resizedImage.getHeight()];
        resizedImage.getPixels(imagePixel, 0, resizedImage.getWidth(), 0, 0, resizedImage.getWidth(), resizedImage.getHeight());

        Log.e("image pixel",imagePixel.toString());


        try {
            res = classifier.recognize(imagePixel);
        }catch (NullPointerException e)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setTitle("Try again!!");
            // builder.setMessage(");
            builder.setNegativeButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //the user clicked on Cancel
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        Log.e("emotion output "," "+res.getEmotionOutput());
        Log.e("emotion output L"," "+res.getLabel());
        // emotionLabel.setText(res.getLabel());
        emotionLabel=res.getLabel();
        emotionOutput=res.getEmotionOutput();
        if(emotionLabel.equalsIgnoreCase("disgust"))
        {
            emotionLabel="Angry";
        }
        try {
            addToCSV();
        } catch (IOException e) {
            e.printStackTrace();
        }
        passToGetSongsList();
    }
    public void passToGetSongsList()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(emotionLabel+" Playlists");
        builder.setMessage("Your emotion detected is "+emotionLabel+". Select your playlist:");
        builder.setPositiveButton("Offline", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent=new Intent(context,songsList.class);
                intent.putExtra("playlistName",emotionLabel);
                context.startActivity(intent);
            }
        });
        builder.setNegativeButton("Online", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //DO TASK
                Intent intent=new Intent(context,SongCatagory.class);
                intent.putExtra("playlistName",emotionLabel);
                context.startActivity(intent);
            }
        });
        builder.setNeutralButton("Feedback", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent=new Intent(context, FeedBack.class);
                intent.putExtra("image",resizedImage);
                intent.putExtra("detectedEmotion",emotionLabel);
                context.startActivity(intent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        if(!isInternetOn())
            dialog.getButton(AlertDialog.BUTTON2).setEnabled(false); //BUTTON1 is positive button
    }
    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = new NetworkInfo[0];
        if (cm != null) {
            netInfo = cm.getAllNetworkInfo();
        }
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
    public final boolean isInternetOn()
    {
        ConnectivityManager connec = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED )
        {
            return true;
        }
        else if ( connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                ||  connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED  )
        {
            return false;
        }
        return false;
    }
    public void addToCSV() throws IOException {
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        String fileName = "EmotionBasedMusicPlayer/EmotionScoreCSV.csv";
        String filePath = baseDir + File.separator + fileName;
        File f = new File(filePath );
        CSVWriter writer;
        if(f.exists() && !f.isDirectory()){
            mFileWriter = new FileWriter(filePath , true);
            writer = new CSVWriter(mFileWriter);
        }
        else {
            writer = new CSVWriter(new FileWriter(filePath));
            String data[]={"EmotionLabel","EmotionScore","Time"};
            writer.writeNext(data);
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formattedDate = df.format(calendar.getTime());
        String data[]={emotionLabel,emotionOutput+"",formattedDate};
        writer.writeNext(data);
        writer.close();
    }
}
