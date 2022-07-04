package it.polimi.middlewareB.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.JobCompletedMessage;
import it.polimi.middlewareB.messages.JobTaskMessage;
import it.polimi.middlewareB.messages.KafkaConfigurationMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class JobSupervisorActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(JobTaskMessage.class, this::startJobTask)
				.match(JobCompletedMessage.class, this::publishCompletedJob)
				.match(KafkaConfigurationMessage.class, this::setKafkaProducer)
				.build();
	}


	private void startJobTask(JobTaskMessage msg) {
		workerActor.tell(msg, self());
	}

	private void publishCompletedJob(JobCompletedMessage msg){
		System.out.println(msg.getNotificationMessage());

		kafkaProducer.send(new ProducerRecord<>("completedJobs", msg.getKey(), msg.getNotificationMessage()));
	}

	private void setKafkaProducer(KafkaConfigurationMessage msg){
		final Properties producerProps = new Properties();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, msg.getBootstrap());
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProducer = new KafkaProducer<>(producerProps);
	}
	@Override
	public void preStart() throws Exception {
		super.preStart();
		workerActor = getContext().actorOf(Props.create(JobWorkerActor.class));
	}

	@Override
	public SupervisorStrategy supervisorStrategy() {return strategy;}



	public static Props props() {
		return Props.create(JobSupervisorActor.class);
	}

	private static SupervisorStrategy strategy =
			//it means: restart indefinitely, allowing infinite time to complete the job
		new OneForOneStrategy(-1, scala.concurrent.duration.Duration.Inf(), false,
					DeciderBuilder.match(JobExecutionException.class,
					e -> SupervisorStrategy.restart())
					.build());
	private ActorRef workerActor;

	//private static Timeout MAX_TIMEOUT = Timeout.create(Duration.ofMillis(Integer.MAX_VALUE));
	private KafkaProducer<String, String> kafkaProducer;

}
