package uk.gov.homeoffice.spray

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import org.apache.pekko.actor._
import org.apache.pekko.stream.ActorMaterializer
import org.apache.pekko.pattern.ask
import org.apache.pekko.util.Timeout
import org.json4s.jackson.JsonMethods._
import grizzled.slf4j.Logging
import org.apache.pekko.http.scaladsl._
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.model.HttpEntity._
import org.apache.pekko.http.scaladsl.settings._
import uk.gov.homeoffice.configuration.HasConfig
import scala.annotation.tailrec

import scala.util.Try
import org.json4s._

/**
  * Boot your application with your required routings e.g.
  *
  * object ExampleBoot extends App with SprayBoot {
  *   implicit lazy val spraySystem = ActorSystem("name-of-provided-actor-system-for-spray")
  *
  *   bootRoutings(ExampleRouting1 ~ ExampleRouting2)
  * }
  *
  * "bootRoutings" defaults to using Spray defaults for the likes of failure handling.
  * In order to add customisations, provide "bootRoutings" a seconds argument list for required exception and/or rejection handling.
  * Note that the method bootHttpService actually boots the services of the routings and this can be switched off for testing by overridding and doing nothing.
  */
trait SprayBoot extends RouteConcatenation with HasConfig with Logging {
  this: App =>

  def bootRoutings(allRoutes: Seq[Routing])
      (implicit exceptionHandler: ExceptionHandler = ExceptionHandler.default(RoutingSettings(config)),
        rejectionHandler: RejectionHandler = RejectionHandler.default): Unit = {

    require(allRoutes.nonEmpty, "No routes declared")
    info(s"""Booting ${allRoutes.size} ${if (allRoutes.size > 1) "routes" else "route"}""")

    @scala.annotation.tailrec
    def combine(allRoutes :Seq[Routing], mergedRoute :Route) :Route = allRoutes match {
      case Nil => mergedRoute
      case head :: tail => combine(tail, mergedRoute ~ head.route)
    }

    val completeChain = combine(allRoutes.tail, allRoutes.head.route)

    val rejectionHandler = RejectionHandler
      .newBuilder()
      .handle {
        case MalformedRequestContentRejection(_,_) =>
          info(s"rejecting request: malformed request")
          complete(HttpResponse(
            status = BadRequest,
            entity = HttpEntity(ContentTypes.`application/json`, compact(render(JObject(
              "status" -> JString("ERROR"),
              "error" -> JString("CONTENT_TYPE_NOT_JSON")
            ))))
          ))
      }
      .handle {
        case AuthorizationFailedRejection =>
          info(s"rejecting request: authorisation failed")
          complete(HttpResponse(
            status=Forbidden,
            entity = HttpEntity(ContentTypes.`application/json`, compact(render(JObject(
              "status" -> JString("ERROR"),
              "error" -> JString("FORBIDDEN")
            ))))
          ))
      }
      .handleNotFound { ctx =>
          info(s"rejecting request: 404 not found: ${ctx.request.uri.toString()}")
          ctx.complete(HttpResponse(
            status = NotFound,
            entity = HttpEntity(ContentTypes.`application/json`, compact(render(JObject(
              "status" -> JString("ERROR"),
              "error" -> JString("NOT_FOUND")
            ))))
          ))
      }
      .result()

    val route = handleRejections(rejectionHandler) { completeChain }
    bootHttpService(route)
  }

  val globalActorSystem = ActorSystem("evw-integration-actor-system", config)
  sys.addShutdownHook({
    globalActorSystem.terminate()
  })

  def bootHttpService(route: Route): Unit = {

    import org.apache.pekko.http.scaladsl.server._
    implicit val ac :ActorSystem = globalActorSystem
    val interface = Try(config.getString("spray.can.server.host")).toOption.getOrElse("0.0.0.0")
    val port = Try(config.getInt("spray.can.server.port")).toOption.getOrElse(9100)
    Http().newServerAt(interface, port).bind(route)
    info(s"Bound to /$interface:$port")

  }

}
