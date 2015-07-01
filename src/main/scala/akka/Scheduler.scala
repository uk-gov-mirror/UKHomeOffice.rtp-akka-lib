package akka

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import akka.actor.{Actor, ActorLogging, ActorRef, Cancellable}
import uk.gov.homeoffice.configuration.ConfigFactorySupport

case object Scheduled

case object NotScheduled

case object Wakeup

trait Scheduler extends ActorLogging with ConfigFactorySupport {
  this: Actor =>

  private var cancellable: Cancellable = _

  val schedule: Cancellable

  def schedule(initialDelay: Duration = Duration.parse("0s"), interval: Duration, receiver: ActorRef = self, message: Any = Wakeup) =
    context.system.scheduler.schedule(initialDelay, interval, receiver, message)

  override def preStart(): Unit = cancellable = schedule

  override def postStop(): Unit = if (cancellable != null) cancellable.cancel()

  override protected[akka] def aroundReceive(receive: Actor.Receive, msg: Any): Unit = msg match {
    case Scheduled =>
      log.info(s"${sender()} asked if I am scheduled!")
      sender() ! (if (cancellable == null) NotScheduled else if (cancellable.isCancelled) NotScheduled else Scheduled)

    case _ =>
      receive.applyOrElse(msg, unhandled)
  }
}

trait NoSchedule {
  this: Scheduler =>

  override lazy val schedule: Cancellable = new Cancellable {
    def isCancelled = true

    def cancel() = true
  }
}