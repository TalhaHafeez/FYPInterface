package com.example.rabiaqayyum.fypinterface.emotionDetection;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TensorflowImageClassifier
{
    private Interpreter interpreter;
    private int inputSize;
    private List<String> labelList;

    float [] floatValues;
    final float THRESHOLD=0.1f;

    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;

    public TensorflowImageClassifier()
    {

    }
    public static TensorflowImageClassifier create(
            AssetManager assetManager,
            String modelPath,
            String labelFilePath,
            int inputSize) throws IOException
    {
        TensorflowImageClassifier classifier = new TensorflowImageClassifier();
        Log.e("model","start model reading ");
        classifier.interpreter = new Interpreter(classifier.loadModelFile(assetManager, modelPath));
        Log.e("label","start label reading ");
        classifier.labelList = classifier.loadLabelList(assetManager, labelFilePath);
        classifier.inputSize = inputSize;
        classifier.floatValues=new float[inputSize*inputSize*1];
        return classifier;
    }
    private MappedByteBuffer loadModelFile(AssetManager assetManager, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        Log.e("model done"," model reading done");
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    public ClassificationModel recognize(final int [] intValues)
    {
        Log.e("classifier","new class ");
        ClassificationModel mainEmotion=new ClassificationModel();
        ByteBuffer byteBuffer = convertBitmapToByteBuffer(intValues);
        float[][] result = new float[1][labelList.size()];
        interpreter.run(byteBuffer, result);
        Log.e("classifier","interpreter called ");
        ClassificationModel maxEmotion=new ClassificationModel();
        maxEmotion.setEmotionOutput(0);
        maxEmotion.setLabel("nothing");
        for (int i=0;i<labelList.size();++i)
        {
            float emotion=(result[0][i]);/* *100) / 127.0f;*/
            Log.e("emotion"," "+emotion);
            if(emotion>THRESHOLD && emotion>maxEmotion.getEmotionOutput())
            {
                maxEmotion.setEmotionOutput(emotion);
                maxEmotion.setLabel(labelList.get(i));
                Log.e("emotion name loop",maxEmotion.getLabel());
            }
        }
        mainEmotion.setEmotionOutput(maxEmotion.getEmotionOutput());
        mainEmotion.setLabel(maxEmotion.getLabel());
        return mainEmotion;
    }
    private ByteBuffer convertBitmapToByteBuffer(int [] intValues) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4*inputSize * inputSize * 1);

        byteBuffer.order(ByteOrder.nativeOrder());
        int pixel = 0;
        byteBuffer.rewind();
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputSize; j++) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat(((val & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return byteBuffer;
    }
    private List<String> loadLabelList(AssetManager assetManager, String labelPath) throws IOException {

        List<String> labelList = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelPath)));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        Log.e("label done"," label reading done");
        return labelList;
    }

}
