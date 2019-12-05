package spatutorial.client

import fs2._
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import spatutorial.client.components.{GlobalStyles, Motd, Progress}
import spatutorial.client.logger._
import spatutorial.client.modules._
import spatutorial.client.services.AppState

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import CssSettings._
import cats.effect.{ContextShift, IO, Timer}
import crystal.ViewRO
import scalacss.ScalaCssReact._
import spatutorial.client.services.AppState.RootModel

import scala.concurrent.ExecutionContext.global
import scala.concurrent.duration._
import scala.language.postfixOps

@JSExportTopLevel("SPAMain")
object SPAMain {
  implicit private val timerIO: Timer[IO] = cats.effect.IO.timer(global)

  // Define the locations (pages) used in this application
  sealed trait Loc

  case object DashboardLoc extends Loc

  case object MotdLoc extends Loc

  case object TodoLoc extends Loc

  case object ProgressLoc extends Loc


  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  val MaxProgress = 10000
  def smoothFlow(init: Int): Stream[IO, Int] = {
    Stream.iterateEval(init * 1000)(i => IO(i + 1))
      .takeWhile(_ <= math.min((init + 1) * 1000, MaxProgress))
      .covary[IO]
      .metered(50 milliseconds)
  }

  val processProgressFlow = Stream.range(0, 10).covary[IO].metered(10 seconds)

  val progressFlow = processProgressFlow.switchMap(smoothFlow)//.evalTap(p => IO(println(p)))


  // configure the router
  val routerConfig = RouterConfigDsl[Loc].buildConfig { dsl =>
    import dsl._

    // wrap/connect components to the circuit
    (staticRoute(root, DashboardLoc) ~> renderR(ctl => Dashboard(ctl, AppState.motdFocusView))
      | staticRoute("#motd", MotdLoc) ~> render(Motd(AppState.motdFocusView))
      | staticRoute("#todo", TodoLoc) ~> render(Todo(AppState.todosView))
      | staticRoute("#progress", ProgressLoc) ~> render(Progress(MaxProgress, progressFlow))
      ).notFound(redirectToPage(DashboardLoc)(Redirect.Replace))
  }.renderWith(layout)

  // This uses another view over the same data as AppState.todosView, but just for demo purposes we define a new one.
  val todoCountView: ViewRO[IO, Option[Int]] =
    AppState.rootModel.view(RootModel.todos).map(_.map(_.items.count(!_.completed)).toOption)

  // base layout for all pages
  def layout(c: RouterCtl[Loc], r: Resolution[Loc]) = {
    <.div(
      // here we use plain Bootstrap class names as these are specific to the top level layout defined here
      <.nav(^.className := "navbar navbar-inverse navbar-fixed-top",
        <.div(^.className := "container",
          <.div(^.className := "navbar-header", <.span(^.className := "navbar-brand", "SPA Tutorial")),
          <.div(^.className := "collapse navbar-collapse",
            // connect menu to model, because it needs to update when the number of open todos changes
            MainMenu(c, r.page, todoCountView)
          )
        )
      ),
      // currently active module is shown in this container
      <.div(^.className := "container", r.render())
    )
  }

  @JSExport
  def main(args: Array[String]): Unit = {
    log.warn("Application starting")
    // send log messages also to the server
    log.enableServerLogging("/logging")
    log.info("This message goes to server as well")

    // create stylesheet
    GlobalStyles.addToDocument()
    // create the router
    val router = Router(BaseUrl.until_#, routerConfig)
    // tell React to render the router in the document body
    router().renderIntoDOM(dom.document.getElementById("root"))
  }
}
