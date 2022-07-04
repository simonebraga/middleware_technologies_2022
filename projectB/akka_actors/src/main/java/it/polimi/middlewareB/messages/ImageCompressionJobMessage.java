package it.polimi.middlewareB.messages;

public class ImageCompressionJobMessage {


    public ImageCompressionJobMessage(String key, String inputFile, String outputFile, int compressionRatio, int duration) {
        this.key = key;
		this.compressionRatio = compressionRatio;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
        this.duration = duration;
	}

    public String getKey() { return key;
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

    public int getDuration(){
        return duration;
    }

    private final String key;
    private final int compressionRatio;
    private final String inputFile;
    private final String outputFile;
    private final int duration;
    
}

