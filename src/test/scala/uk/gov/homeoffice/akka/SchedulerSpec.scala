package uk.gov.homeoffice.akka

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import org.apache.pekko.actor.{Actor, Cancellable, Props}
import org.apache.pekko.event.LoggingReceive
import org.specs2.mutable.Specification
import uk.gov.homeoffice.akka.schedule.Protocol.{IsScheduled, NotScheduled, Scheduled}
import uk.gov.homeoffice.akka.schedule.{NoSchedule, Schedule, Scheduler}

class SchedulerSpec extends Specification with ActorSystemSpecification {
  "Actor" should {
    "be scheduled to act as a poller" in new ActorSystemContext {
      val exampleSchedulerActor = system actorOf Props(new ExampleSchedulerActor)
      exampleSchedulerActor ! IsScheduled
      expectMsgType[Scheduled]
    }
  }
}

class ExampleSchedulerActor extends Actor with Scheduler {
  val schedule: Cancellable = context.system.scheduler.schedule(initialDelay = 1 second, interval = 5 seconds, receiver = self, message = Schedule)

  def receive = LoggingReceive {
    case Schedule => println("Hello World!")
  }
}
