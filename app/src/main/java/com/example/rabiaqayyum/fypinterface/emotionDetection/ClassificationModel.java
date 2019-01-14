package com.example.rabiaqayyum.fypinterface.emotionDetection;

public class ClassificationModel
{
    float emotionOutput;
    String label;
    public ClassificationModel()
    {
        emotionOutput= -1;
        label="";
    }
    public void setEmotionOutput(float emotionOutput)
    {
        this.emotionOutput=emotionOutput;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getEmotionOutput() {
        return emotionOutput;
    }

    public String getLabel() {
        return label;
    }
}
