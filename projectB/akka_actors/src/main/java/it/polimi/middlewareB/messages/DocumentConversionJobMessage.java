package it.polimi.middlewareB.messages;

public class DocumentConversionJobMessage {
	public DocumentConversionJobMessage(String inputFile, String outputFile, String targetExtension, int duration){
	this.inputFile = inputFile;
	this.outputFile = outputFile;
	this.targetExtension = targetExtension;
	this.duration = duration;
    }

	public String getInputFile() {
		return inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public String getTargetExtension() {
		return targetExtension;
	}

	public int getDuration(){
		return duration;
	}
	private final String inputFile;
	private final String outputFile;
	private final String targetExtension;
	private final int duration;
}
