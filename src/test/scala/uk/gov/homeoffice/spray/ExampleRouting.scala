package uk.gov.homeoffice.spray

import org.apache.pekko.http.scaladsl.model.MediaTypes._
import org.json4s.JsonAST.{JObject, JString}

object ExampleRouting extends ExampleRouting

trait ExampleRouting extends Routing {
 val route =
   pathPrefix("example") {
     pathEndOrSingleSlash {
       get {
         //respondWithMediaType(`application/json`) {
           complete {
             JObject("status" -> JString("Congratulations"))
           }
         //}
       }
     } ~
     post {
       path("submission") {
         complete("todo")
       }
     }
   }
}
