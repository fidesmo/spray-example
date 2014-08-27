package com.fidesmo.examples.spray

import akka.actor.Props
import spray.routing.HttpServiceActor
import spray.json._
import spray.http.StatusCodes

class WebServiceActor extends HttpServiceActor {
  import spray.httpx.SprayJsonSupport._
  import WebServiceActor._
  import Models._

  val route = path("description" / Segment) { serviceId => // This path is for the service description call
    if(serviceId == "mifare") {
      // Return the service description - this description is displayed on the user's phone
      complete(ServiceDescription("Test service using MIFARE Classic API"))
    } else if(serviceId == "mifare-pay") {
      // Return the service description - this description is displayed on the user's phone
      complete(ServiceDescription("Test service using MIFARE Classic API with payment", Some(ServicePrice(BigDecimal("99.00")))))
    } else if(serviceId == "transceive") {
      // Return the service description - this description is displayed on the user's phone
      complete(ServiceDescription("Test service using transceive API"))
    } else if(serviceId == "install") {
      // Return the service description - this description is displayed on the user's phone
      complete(ServiceDescription("Test service using ccm API"))
    } else if(serviceId == "fail") {
      // Return the service description - this description is displayed on the user's phone
      complete(ServiceDescription("Test service that will fail"))
    } else {
      complete(StatusCodes.NotFound)
    }
  } ~ path("service") { // This path is for the service delivery required call
    entity(as[ServiceDeliveryRequest]) { request =>
      val sessionId = request.sessionId
      if(request.serviceId == "mifare") {
        val deliveryActor = context.actorOf(MifareDeliveryActor.props(sessionId), sessionId.toString)
        // Start the delivery actor
        deliveryActor ! Start
        complete(StatusCodes.OK)
      } else if(request.serviceId == "mifare-pay") {
        // Verify that the price paid by the customer is the price of the service
        if(request.description.price == Some(ServicePrice(BigDecimal("99.00")))) {
          val deliveryActor = context.actorOf(MifareDeliveryActor.props(sessionId), sessionId.toString)
          // Start the delivery actor
          deliveryActor ! Start
          complete(StatusCodes.OK)
        } else {
          // The customer has not paid the right price for the service or
          // the price has changed
          complete(StatusCodes.PaymentRequired)
        }
      } else if(request.serviceId == "transceive") {
        val deliveryActor = context.actorOf(TransceiveDeliveryActor.props(sessionId), sessionId.toString)
        // Start the delivery actor
        deliveryActor ! Start
        complete(StatusCodes.OK)
      } else if(request.serviceId == "install") {
        val deliveryActor = context.actorOf(InstallAppletDeliveryActor.props(sessionId), sessionId.toString)
        // Start the delivery actor
        deliveryActor ! Start
        complete(StatusCodes.OK)
      } else if(request.serviceId == "fail") {
        val deliveryActor = context.actorOf(FailDeliveryActor.props(sessionId), sessionId.toString)
        // Start the delivery actor
        deliveryActor ! Start
        complete(StatusCodes.OK)
      } else {
        complete(StatusCodes.NotFound)
      }
    }
  } ~ path("delivery" / "generic" / JavaUUID) { sessionId =>
    val deliveryActor = context.actorSelection(sessionId.toString)
    entity(as[GenericResponse]) { response =>
      // Send response to delivery actor
      deliveryActor ! response
      complete(StatusCodes.OK)
    }
  } ~ path("delivery" / "getCard" / JavaUUID) { sessionId =>
    val deliveryActor = context.actorSelection(sessionId.toString)
    entity(as[GetCardResponse]) { response =>
      // Send response to delivery actor
      deliveryActor ! response
      complete(StatusCodes.OK)
    }
  } ~ path("delivery" / "read" / JavaUUID) { sessionId =>
    val deliveryActor = context.actorSelection(sessionId.toString)
    entity(as[ReadResponse]) { response =>
      // Send response to delivery actor
      deliveryActor ! response
      complete(StatusCodes.OK)
    }
  } ~ path("delivery" / "transceive" / JavaUUID) { sessionId =>
    val deliveryActor = context.actorSelection(sessionId.toString)
    entity(as[TransceiveResponse]) { response =>
      // Send response to delivery actor
      deliveryActor ! response
      complete(StatusCodes.OK)
    }
  }

  def receive = runRoute(route)

}

object WebServiceActor {
  case object Start
  def props(): Props = Props(classOf[WebServiceActor])
}
