package it.polimi.middlewareB.messages;

public class ImageCompressionJobMessage {


    public ImageCompressionJobMessage(String inputFile, String outputFile, int compressionRatio) {
		this.compressionRatio = compressionRatio;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}

    public int getCompressionRatio() {
        return compressionRatio;
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    private final int compressionRatio;
    private final String inputFile;
    private final String outputFile;
    
}

