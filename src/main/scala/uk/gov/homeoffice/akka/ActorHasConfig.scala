package uk.gov.homeoffice.akka

import org.apache.pekko.actor.Actor
import uk.gov.homeoffice.configuration.HasConfig
import com.typesafe.config.Config

trait ActorHasConfig extends HasConfig {
  this: Actor =>

  override implicit val config :Config = context.system.settings.config
}
