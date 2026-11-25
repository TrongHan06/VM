package com.example.vocabmaster.model.entity;

public class PronunciationResult {
    private final String statusMessage;
    private final String colorHex;
    private final String recognizedText;
    private final String targetWord;
    private final boolean isExact;

    public PronunciationResult(String statusMessage, String colorHex, String recognizedText, String targetWord, boolean isExact) {
        this.statusMessage = statusMessage;
        this.colorHex = colorHex;
        this.recognizedText = recognizedText;
        this.targetWord = targetWord;
        this.isExact = isExact;
    }

    public String getStatusMessage() { return statusMessage; }
    public String getColorHex() { return colorHex; }
    public String getRecognizedText() { return recognizedText; }
    public String getTargetWord() { return targetWord; }
    public boolean isExact() { return isExact; }
}
