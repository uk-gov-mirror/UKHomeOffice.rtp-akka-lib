package uk.gov.homeoffice.spray

import scala.concurrent.duration._
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.testkit._
import org.specs2.mutable.SpecificationLike
import uk.gov.homeoffice.json.JsonFormats
import org.apache.pekko.testkit.TestDuration._

trait RouteSpecification extends Specs2RouteTest with JsonFormats {
  this: SpecificationLike with RouteTest =>

  val actorRefFactory = system

  implicit def default(implicit system: ActorSystem) :RouteTestTimeout = RouteTestTimeout(5.seconds)
}
