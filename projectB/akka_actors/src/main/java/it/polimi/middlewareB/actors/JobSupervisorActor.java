package it.polimi.middlewareB.actors;

import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.util.Timeout;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.JobCompletedMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;

import java.time.Duration;

public class JobSupervisorActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TextFormattingJobMessage.class, this::startTextFormattingJob)
				.match(DocumentConversionJobMessage.class, this::startDocumentConversionJob)
				.match(ImageCompressionJobMessage.class, this::startImageCompressionJob)
				.match(JobCompletedMessage.class, this::publishCompletedJob)
				.build();
	}

	private void startImageCompressionJob(ImageCompressionJobMessage msg) {
		workerActor.tell(msg, self());
	}

	private void startDocumentConversionJob(DocumentConversionJobMessage msg) {
		workerActor.tell(msg, self());
	}

	private void startTextFormattingJob(TextFormattingJobMessage msg) {
		workerActor.tell(msg, self());
	}

	private void publishCompletedJob(JobCompletedMessage msg){
		System.out.println(msg.getNotificationMessage());
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
			//it means: restart after 50 retries, allowing infinite time to complete the job
			new OneForOneStrategy(50, ClusterStarter.MAX_DURATION,
					DeciderBuilder.match(JobExecutionException.class,
					e -> SupervisorStrategy.restart())
					.build());
	private ActorRef workerActor;

	private static Timeout MAX_TIMEOUT = Timeout.create(Duration.ofMillis(Integer.MAX_VALUE));

}
