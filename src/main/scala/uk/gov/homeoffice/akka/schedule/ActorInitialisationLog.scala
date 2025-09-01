package uk.gov.homeoffice.akka.schedule

import org.apache.pekko.actor.{Actor, ActorLogging}
import org.apache.pekko.serialization.Serialization._

trait ActorInitialisationLog {
  this: Actor with ActorLogging =>

  log.info(s"Actor configured for ${serializedActorPath(self)}")
}
