package it.polimi.middlewareB.messages;

public class TextFormattingJobMessage {

    public TextFormattingJobMessage(String key, String inputFile, String outputFile, String formattingRules, int duration) {
        this.key = key;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.formattingRules = formattingRules;
        this.duration = duration;
    }

    public String getKey() {
        return key;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public String getFormattingRules() {
        return formattingRules;
    }

    public int getDuration(){
        return duration;
    }

    private final String key;
    private final String inputFile;
    private final String outputFile;
    private final String formattingRules;
    private final int duration;
}
