package it.polimi.middlewareB.actors;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.routing.ActorRefRoutee;
import akka.routing.RoundRobinRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import akka.util.Timeout;
import it.polimi.middlewareB.messages.DocumentConversionJobMessage;
import it.polimi.middlewareB.messages.ImageCompressionJobMessage;
import it.polimi.middlewareB.messages.TextFormattingJobMessage;

public class MainDispatcher extends AbstractActor {

    public static final int POOL_DIMENSION = 10;
	//TODO check if this is the advised method to provide an infinite duration
	public static final Duration MAX_DURATION = Duration.ofNanos(Long.MAX_VALUE);
	//public static final Timeout MAX_TIMEOUT = Timeout.create(MAX_DURATION);
    Router router;

    //list of possible Actors to which a message can be routed to
    //(routees are "Abstraction of a destination for messages routed via a Router.")
    List<Routee> routees = new ArrayList<Routee>();
    {
	for (int index = 0; index < POOL_DIMENSION; index++) {
	    ActorRef r = getContext().actorOf(Props.create(JobSupervisorActor.class));
	    
	    //register this class (MainDispatcher) as a watcher for each worker Actor
	    getContext().watch(r);
	    
	    //ActorRefRoutees are "Routee that sends the messages to an ActorRef."
	    routees.add(new ActorRefRoutee(r));
	}
	//create the router with the just constructed thread pool
	//and a routing policy
		router = new Router(new RoundRobinRoutingLogic(), routees);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(TextFormattingJobMessage.class,
						//when a message arrives, route it using the internal predefined policy
						message -> {router.route(message, getSender());})
				.match(DocumentConversionJobMessage.class,
						message -> {router.route(message, getSender());})
				.match(ImageCompressionJobMessage.class,
						message -> {router.route(message, getSender());})
				.match(Terminated.class,
						message -> {
							//remove crashed Actor
							router = router.removeRoutee(message.actor());
							//create a new one
							ActorRef r = getContext().actorOf(Props.create(JobSupervisorActor.class));
							getContext().watch(r);
							router = router.addRoutee(new ActorRefRoutee(r));
						})
				.build();
	}

}
