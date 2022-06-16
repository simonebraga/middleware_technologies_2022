package it.polimi.middlewareB;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Hello world!
 *
 */
public class CLIApp
{
    public static void main( String[] args )
    {

        System.out.println( "Hello World!\n" +
			    "In the future, it will be used to interact with the Kafka topics\n");

		login(args);

		while(true) {
			printMenu();
			System.out.print("choice> ");
			int choice = keyboard.nextInt();
			switch (choice) {
				case 1:
					System.out.println("For now, merely for debugging reasons, we send dummy messages to Kafka topic \"pending\"");;
					try {
						for (int i = 0; i < 50; i++) {
							kafkaSender.send(new ProducerRecord<>("pending", 1, "imageCompression" + i)).get();
							System.out.println("Message " + i + " sent");
							Thread.sleep(1000);
						}
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					} catch (ExecutionException e) {
						throw new RuntimeException(e);
					}
					break;
				case 2:
					break;
				case 0:
					destructor();
					return;
			}
		}

		//initialize actor
		// String serverAddr = "akka.tcp://Server@127.0.0.1:6123/user/serverActor";
		// ActorSelection server = getContext().actorSelection(serverAddr);
		// ActorRef capitalizer = sys.actorOf(CapitalizerActor.props(), "first-actor");

		//System.out.println("Insert a message to send to the capitalizer actor:");

		//String answer = keyboard.nextLine();
		//System.out.println("Entered: \"" + answer + "\", sending to actor...");

		// capitalizer.tell(new TextMessage(answer), ActorRef.noSender());

		// sys.terminate();
	
	//System.out.println("Program is terminated\n<Press any key to close>");

	//keyboard.nextLine();
	//keyboard.close();

    }

	private static void printMenu(){
		System.out.print("Enter a choice:\n" +
				" 1 - Send a new task (TODO)\n" +
				" 2 - Check completed task (TODO)\n" +
				" 0 - Exit\n");
	}

	private static void login(String [] args){
		System.out.print("This is the Middleware Project B CLI Application\n" +
				"Please enter your User ID.\n" +
				"login> ");
		userID = keyboard.nextInt();

		//This part acts as a constructor, even if with static classes it's imprecise to say so
		final String kafkaBootstrap = args.length > 0 ? args[0] : "127.0.0.1:9092";
		final int kafkaPort = args.length > 1 ? Integer.parseInt(args[1]) : 9999;

		final Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class.getName());
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		//kafkaProps.put("acks", "all");
		kafkaSender = new KafkaProducer<>(kafkaProps);

	}

	//This method should be called when the application stops, to gracefully tidy up open resources
	private static void destructor(){
		keyboard.close();
	}
	private static int userID = 0;
	private static Scanner keyboard = new Scanner(System.in);
	private static KafkaProducer<Integer, String> kafkaSender;
}
