package uk.gov.homeoffice.spray

import org.apache.pekko.http.scaladsl.model.MediaTypes._
import org.apache.pekko.http.scaladsl.model.StatusCodes._
import org.json4s._
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ExampleRoutingSpec extends Specification with RouteSpecification {
  trait Context extends Scope with ExampleRouting

  "Example routing" should {
    "indicate when a required route is not recognised" in new Context {
      Get("/example/non-existing") ~> route ~> check {
        status must throwAn[Exception]
      }
    }

    "be available" in new Context {
      Get("/example") ~> route ~> check {
        status mustEqual OK
        contentType.mediaType mustEqual `application/json`
        // TODO: Consider reinstating by providing unmarshaller in Json4Support trait
        //(responseAs[JValue] \ "status").extract[String] mustEqual "Congratulations"
      }
    }
  }
}
