package it.polimi.middlewareB.messages;

public class JobTaskMessage {
	public JobTaskMessage(String key, String name, String inputFile, String outputFile, String parameter, int duration){
		this.key = key;
		this.name = name;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.parameter = parameter;
		this.duration = duration;
    }

	public String getKey(){ return key; }
	public String getName() { return name; }
	public String getInputFile() { return inputFile; }

	public String getOutputFile() { return outputFile; }

	public String getParameter() { return parameter; }

	public int getDuration(){ return duration; }

	private final String key;
	private final String name;
	private final String inputFile;
	private final String outputFile;
	private final String parameter;
	private final int duration;
}
