package it.polimi.middlewareB.actors;

import akka.Main;
import akka.actor.*;
import akka.japi.pf.DeciderBuilder;
import akka.pattern.Patterns;
import akka.util.Timeout;
import it.polimi.middlewareB.JobExecutionException;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;
import scala.concurrent.Await;
import scala.concurrent.Future;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeoutException;

public class JobSupervisorActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TextFormattingJobMessage.class, this::startTextFormattingJob)
				.match(DocumentConversionJobMessage.class, this::startDocumentConversionJob)
				.match(ImageCompressionJobMessage.class, this::startImageCompressionJob)
				.build();
	}

	private void startImageCompressionJob(ImageCompressionJobMessage msg) {
		//TODO implement synchronous requests
//		Future<Object> future = Patterns.ask(workerActor, msg, MAX_TIMEOUT);
//		try {
//			Await.result(future, MAX_TIMEOUT.duration());
//		} catch (TimeoutException e) {
//			System.err.println("TimeoutException!");
//		} catch (InterruptedException e) {
//			System.err.println("InterruptedException!");
//		}
		workerActor.tell(msg, self());
	}

	private void startDocumentConversionJob(DocumentConversionJobMessage msg) {
		workerActor.tell(msg, self());
	}

	private void startTextFormattingJob(TextFormattingJobMessage msg) {
		workerActor.tell(msg, self());
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
			new OneForOneStrategy(50, MainDispatcher.MAX_DURATION,
					DeciderBuilder.match(JobExecutionException.class,
					e -> SupervisorStrategy.restart())
					.build());
	private ActorRef workerActor;

	private static Timeout MAX_TIMEOUT = Timeout.create(Duration.ofMillis(Integer.MAX_VALUE));

}
