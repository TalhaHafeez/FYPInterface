package com.example.rabiaqayyum.fypinterface;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Base64;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.StorageObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class cloudStorage
{
    Context context;
    private static Properties properties;
    private static Storage storage;
    String emotionLabel;
    /**
     * Uploads a file to a bucket. Filename and content type will be based on
     * the original file.
     *
     *
     * @throws Exception
     */
    public cloudStorage(Context context,String emotionLabel)
    {
        this.context=context;
        this.emotionLabel=emotionLabel;
    }
    public void uploadFile(String bucketName, Bitmap bitmap)
            throws Exception {

        Storage storage = getStorage();

        StorageObject object = new StorageObject();
        object.setBucket(bucketName);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitMapData = stream.toByteArray();
        String imageB64 = Base64.encodeToString(bitMapData, Base64.DEFAULT);

        InputStream stream1 = new ByteArrayInputStream(bitMapData);
        //InputStream inputStream=new Base64InputStream()
        try {
            //String contentType = URLConnection
                    //.guessContentTypeFromStream(stream1);
            String contentType="text/plain";
            InputStreamContent content = new InputStreamContent(contentType,
                    stream1);

            Storage.Objects.Insert insert = storage.objects().insert(
                    bucketName, null, content);
           // Random rand = new Random();
            //int number= rand.nextInt(3) + 1;
            int number= (int) (Math.random()*10000);
            if(emotionLabel.equalsIgnoreCase("happy"))
            {
                insert.setName("images/Happy/image"+number);
            }
            else if(emotionLabel.equalsIgnoreCase("sad"))
            {
                insert.setName("images/Sad/image"+number);
            }
            else if(emotionLabel.equalsIgnoreCase("fear"))
            {
                insert.setName("images/Fear/image"+number);
            }
            else if(emotionLabel.equalsIgnoreCase("angry"))
            {
                insert.setName("images/Angry/image"+number);
            }
            else if(emotionLabel.equalsIgnoreCase("surprise"))
            {
                insert.setName("images/Surpise/image"+number);
            }
            else if(emotionLabel.equalsIgnoreCase("neutral"))
            {
                insert.setName("images/Neutral/image"+number);
            }

            insert.execute();
        } finally {
            stream.close();
        }
    }
    private Storage getStorage() throws Exception {

        if (storage == null) {

            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<String>();
            scopes.add(StorageScopes.DEVSTORAGE_FULL_CONTROL);

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId("taz138@level-dragon-207806.iam.gserviceaccount.com")
                    .setServiceAccountPrivateKeyFromP12File(
                            getTempPkc12File())
                    .setServiceAccountScopes(scopes).build();

            storage = new Storage.Builder(httpTransport, jsonFactory,
                    credential).setApplicationName("MyApp")
                    .build();
        }

        return storage;
    }
    private File getTempPkc12File() throws IOException {
        // xxx.p12 export from google API console
        InputStream pkc12Stream = context.getAssets().open("projectGoogle.p12");
        File tempPkc12File = File.createTempFile("projectGoogle", "p12");
        OutputStream tempFileStream = new FileOutputStream(tempPkc12File);

        int read = 0;
        byte[] bytes = new byte[1024];
        while ((read = pkc12Stream.read(bytes)) != -1) {
            tempFileStream.write(bytes, 0, read);
        }
        return tempPkc12File;
    }
}