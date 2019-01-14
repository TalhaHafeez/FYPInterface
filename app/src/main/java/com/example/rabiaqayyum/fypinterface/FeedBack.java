package com.example.rabiaqayyum.fypinterface;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import static android.content.ContentValues.TAG;
import static com.example.rabiaqayyum.fypinterface.BuildConfig.DEBUG;


public class FeedBack extends AppCompatActivity implements TextWatcher, View.OnClickListener {

    TextView detectedEmotion;
    private Toolbar toolbar;
    EditText emotion;
    TextInputLayout emotion_input_layout;
    Button submit;


    Bitmap image;
    String emotionLabel;

    cloudStorage cs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        toolbar = (Toolbar) findViewById(R.id.toolbarFeedback);
        setSupportActionBar(toolbar);
        emotion_input_layout=(TextInputLayout) findViewById(R.id.input_layout_emotion);
        detectedEmotion=(TextView)findViewById(R.id.dEmotion);
        emotion=(EditText) findViewById(R.id.emotion);
        submit=(Button)findViewById(R.id.submit);

        getSupportActionBar().setTitle("Send Feedback");
        Intent getIntent= getIntent();
        Bundle bundle=getIntent.getExtras();
        image= (Bitmap) bundle.get("image");
        emotionLabel= (String) bundle.get("detectedEmotion");
        detectedEmotion.setText("Your emotion Detected was "+emotionLabel);

        emotion.addTextChangedListener(this);
        submit.setOnClickListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        String correctEmotion=emotion.getText().toString();
        String detectemotion=detectedEmotion.getText().toString();
        if (detectemotion.contains(correctEmotion))
        {
            emotion_input_layout.setError("Emotion is already detected correct!");

        }
        else
        {
            emotion_input_layout.setErrorEnabled(false);
        }
    }

    @Override
    public void onClick(View view) {
        if(emotion.getText().toString().isEmpty())
        {
            emotion_input_layout.setError("Please enter a correct emotion");
        }
       /* else if (!emotion.getText().toString().equalsIgnoreCase("happy")||
                !emotion.getText().toString().equalsIgnoreCase("sad")||
                !emotion.getText().toString().equalsIgnoreCase("angry")||
                !emotion.getText().toString().equalsIgnoreCase("fear")||
                !emotion.getText().toString().equalsIgnoreCase("surprise")||
                !emotion.getText().toString().equalsIgnoreCase("neutral"))
        {
            emotion_input_layout.setError("please enter your correct emotion this emotion does not exsist in system");
        }*/
        else
        {
            String emotionText=emotion.getText().toString();
            emotion_input_layout.setErrorEnabled(false);
            cs=new cloudStorage(this,emotionText);
            new AsyncTask(){

                @Override
                protected Object doInBackground(Object[] params) {
                    try {

                        cs.uploadFile("ebmp", image);

                    } catch (Exception e) {
                        if(DEBUG) Log.d(TAG, "Exception: "+e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("Thank you for feedback!!");
            // builder.setMessage(");
            builder.setNeutralButton(this.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent=new Intent(FeedBack.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
}
