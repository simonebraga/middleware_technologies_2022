package it.polimi.middlewareB.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.middlewareB.JSONJobTask;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.JobCompletedMessage;
import it.polimi.middlewareB.messages.JobStatisticsMessage;
import it.polimi.middlewareB.messages.JobTaskMessage;
import it.polimi.middlewareB.messages.KafkaConfigurationMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

public class JobSupervisorActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(JobTaskMessage.class, this::startJobTask)
				.match(JobCompletedMessage.class, this::publishCompletedJob)
				.build();
	}

	public JobSupervisorActor(String kafkaBootstrap, ActorRef retriesAnalysisActor, Map<String, Integer> jobDurations){
		System.out.println("JobSupervisorActor started!");
		final Properties producerProps = new Properties();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProducer = new KafkaProducer<>(producerProps);

		final Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrap);
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "supervisor");
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		kafkaConsumer = new KafkaConsumer<>(consumerProps);
		kafkaConsumer.subscribe(List.of("pendingJobs"));
		workerActor = getContext().actorOf(Props.create(JobWorkerActor.class));
		jacksonMapper = new ObjectMapper();

		this.retriesAnalysisActor = retriesAnalysisActor;
		this.jobDurations = jobDurations;

		pendingJobs = 0;

		idleUntilNewJob();
		System.err.println("Constructor has finished!");
	}
	private void startJobTask(JobTaskMessage msg) {
		workerActor.tell(msg, self());
	}

	private void idleUntilNewJob() {

		boolean breakCheck = false;

		while (true) {

			ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(500));

			if (records.count()!=0) {
				for (ConsumerRecord<String, String> record : records) {
					LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(record.timestamp()),
							TimeZone.getDefault().toZoneId());
					System.out.println(timestamp + " - key: " + record.key() + ", " + record.value());
					try {
						JSONJobTask nextJob = jacksonMapper.readValue(record.value(), JSONJobTask.class);
						if (jobDurations.get(nextJob.getName()) != null) {
							workerActor.tell(new JobTaskMessage(record.key(),
									nextJob.getName(),
									nextJob.getInput(),
									nextJob.getOutput(),
									nextJob.getParameter(),
									jobDurations.get(nextJob.getName())), self());
							pendingJobs++;
							breakCheck = true;
						} else {
							System.err.println("Non existing job requested");
							kafkaProducer.send(new ProducerRecord<>("completedJobs", record.key(), "NOT_EXISTING_JOB"));
						}
					} catch (JsonProcessingException e) {
						System.err.println("Unable to process job: " + record.key() + ", " + record.value());
						kafkaProducer.send(new ProducerRecord<>("completedJobs", record.key(), "EVENT_BAD_FORMED"));
					}
				}
				if (breakCheck) break;
			}
		}
	}

	private void publishCompletedJob(JobCompletedMessage msg){
		//System.out.println(msg.getNotificationMessage() + " (took " + msg.getnOfStarts() + " tries)");
		System.err.println("Hi! I'm in publishCompletedJobs!");
		retriesAnalysisActor.tell(new JobStatisticsMessage(msg.getName(), msg.getnOfStarts()), self());
		kafkaProducer.send(new ProducerRecord<>("completedJobs", msg.getKey(), msg.getNotificationMessage()));
		pendingJobs--;
		if (pendingJobs==0) {
			idleUntilNewJob();
		}
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

	}

	@Override
	public SupervisorStrategy supervisorStrategy() {return strategy;}



	public static Props props(String kafkaBootstrap, ActorRef retriesAnalysisActor, Map<String, Integer> jobDurations) {
		return Props.create(JobSupervisorActor.class, () -> new JobSupervisorActor(kafkaBootstrap, retriesAnalysisActor, jobDurations));
	}

	private static SupervisorStrategy strategy =
			//it means: restart indefinitely, allowing infinite time to complete the job
		new OneForOneStrategy(-1, scala.concurrent.duration.Duration.Inf(), false,
					DeciderBuilder.match(JobExecutionException.class,
					e -> SupervisorStrategy.restart())
					.build());
	private ActorRef workerActor;

	//private static Timeout MAX_TIMEOUT = Timeout.create(Duration.ofMillis(Integer.MAX_VALUE));
	//TODO can these be made static?
	private KafkaProducer<String, String> kafkaProducer;

	private int pendingJobs;
	private KafkaConsumer<String, String> kafkaConsumer;
	private ActorRef retriesAnalysisActor;
	private ObjectMapper jacksonMapper;
	private Map<String,Integer> jobDurations;

}
