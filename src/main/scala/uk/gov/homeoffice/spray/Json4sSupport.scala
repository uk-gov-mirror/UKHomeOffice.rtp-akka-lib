package uk.gov.homeoffice.spray

import org.json4s._
import org.json4s.native.JsonMethods._
import org.apache.pekko.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.apache.pekko.http.scaladsl.marshalling._
import org.apache.pekko.http.scaladsl.unmarshalling._

trait Json4sSupport {
  
  implicit val jObjectMarshaller :Marshaller[JObject, HttpResponse] =
    Marshaller.opaque(json4sToHttpResponse(_))

  implicit def json4sToHttpResponse(json :JObject) :HttpResponse = {
    HttpResponse(
      status = OK,
      entity = HttpEntity(ContentTypes.`application/json`, compact(render(json)))
    )
  }

}

