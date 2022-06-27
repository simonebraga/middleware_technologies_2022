package it.polimi.middlewareB.messages;

public class TextFormattingJobMessage {

    public TextFormattingJobMessage(String inputFile, String outputFile, String formattingRules) {
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        this.formattingRules = formattingRules;
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

    private final String inputFile;
    private final String outputFile;
    private final String formattingRules;
}
