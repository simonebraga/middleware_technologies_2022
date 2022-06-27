package it.polimi.middlewareB.messages;

public class DocumentConversionJobMessage {
	public DocumentConversionJobMessage(String inputFile, String outputFile, String targetExtension){
	this.inputFile = inputFile;
	this.outputFile = outputFile;
	this.targetExtension = targetExtension;
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

	private final String inputFile;
	private final String outputFile;
	private final String targetExtension;
}
