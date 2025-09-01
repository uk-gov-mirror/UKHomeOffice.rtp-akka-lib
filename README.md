
# Akka - Reusable functionality
===============================

### 2025-08-29: The maintainers of this library have stopped
### using it and it will no longer be maintained going forwards.

Akka reusable functionality and Scala Spray functionality/template for general use.

Project built with the following (main) technologies:

- Scala 3

- SBT

- Pekko

- Pekko Http

- Specs2

Introduction
------------
Boot a microservice utilising functionality built on top of Spray.

Create an application and include "routes" to expose an API to access via HTTP.
Build up your own routes, noting that "service-statistics" route is automatically exposed for you and can be accessed as (for example):

```bash
http://localhost:9100/service-statistics
```
which would give you something like:
```javascript
{
  statistics: {
    uptime: "36663930295 nanoseconds"
    total-requests: "2"
    open-requests: "1"
    maximum-open-requests: "1"
    total-connections: "1"
    open-connections: "1"
    max-open-connections: "1"
    request-timeouts: "0"
  }
}
```

Example Usage
-------------
- Actor scheduling:
```scala
  class SchedulerSpec extends Specification {
    "Actor" should {
      "be scheduled to act as a poller" in new ActorSystemContext {
        val exampleSchedulerActor = system.actorOf(Props(new ExampleSchedulerActor), "exampleSchedulerActor")
        exampleSchedulerActor ! Scheduled
        expectMsg(Scheduled)
      }
  
    }
  }
  
  class ExampleSchedulerActor extends Actor with Scheduler {
    val schedule: Cancellable = schedule(initialDelay = 1 second, interval = 5 seconds, receiver = self, message = Wakeup)
  
    def receive = LoggingReceive {
      case Wakeup => println("Hello World!")
    }
  }
```

- Create some Spray routings - HTTP contract/gateway to your microservice:
```scala
  object ExampleRouting1 extends ExampleRouting1
  
  trait ExampleRouting1 extends Routing {
   val route =
     pathPrefix("example1") {
       pathEndOrSingleSlash {
         get {
           complete { JObject("status" -> JString("Congratulations 1")) }
         }
       }
     }
  }
  
  object ExampleRouting2 extends ExampleRouting2
    
  trait ExampleRouting2 extends Routing {
   val route =
     pathPrefix("example2") {
       pathEndOrSingleSlash {
         get {
           complete { JObject("status" -> JString("Congratulations 2")) }
         }
       }
     }
  }
```

- Create your application (App) utilitising your routings (as well as anything else e.g. booting/wiring Akka actors):
```scala
  object ExampleBoot extends App with SprayBoot with ExampleConfig {
    // You must provide an ActorSystem for Spray.
    implicit lazy val sprayActorSystem = ActorSystem("example-boot-actor-system")
  
    bootRoutings(ExampleRouting1 ~ ExampleRouting2 ~ ExampleRoutingError)(FailureHandling.exceptionHandler)
  }
```

Noting that a "configuration" such as application.conf must be provided e.g.
```scala
  spray.can.server {
    name = "example-spray-can"
    host = "0.0.0.0"
    port = 9100
    request-timeout = 1s
    service = "example-http-routing-service"
    remote-address-header = on
  }
```

To run ExampleBoot:
```bash
sbt test:run
```

