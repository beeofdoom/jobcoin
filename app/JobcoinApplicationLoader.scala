

import akka.actor.Props
import com.gemini.jobcoin.clients.JobcoinClient
import com.gemini.jobcoin.services.JobcoinMixer
import play.api.libs.ws.ahc.{AhcWSClient, AhcWSComponents}
import play.api.routing.{Router, SimpleRouter, SimpleRouterImpl}
import play.api._
import play.api.ApplicationLoader.Context
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceApplicationLoader, GuiceableModule}
import play.api.routing.sird._
import play.filters.HttpFiltersComponents

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.ExecutionContext
import com.gemini.jobcoin.controllers.JobCoinController
import com.gemini.jobcoin.services.JobcoinActor
import router.Routes


class JobcoinApplicationLoader extends ApplicationLoader {
  def load(context: Context) = {

    val app = new MyComponents(context)
    implicit val ec: ExecutionContext = app.executionContext
    app.application.actorSystem.scheduler.scheduleWithFixedDelay(30 seconds, 30 seconds, app.jobCoinActor, "startMix")
    app.application

  }
}

class MyComponents(context: Context) extends BuiltInComponentsFromContext(context) with HttpFiltersComponents with AhcWSComponents with SimpleRouter {
  implicit val prefix: String = "/"

  //override def routes =
  implicit val ec = executionContext
  val client = new JobcoinClient(ws = wsClient, config = context.initialConfiguration.underlying)
  val mixer = new JobcoinMixer(client)
  val jobCoinActor = actorSystem.actorOf(Props(new JobcoinActor(mixer)), "JobcoinActor")

  val controller = new JobCoinController(controllerComponents,mixer)
  //val router = Router.empty
  val router = new Routes(httpErrorHandler, controller, prefix)
  val routes = router.routes
  /*val router = Router.from{
    case POST("/rest/v1/addAddresses") =>
      controller.addAddresses()
  }*/

  //override def routes: Router.Routes = router.routes

}

