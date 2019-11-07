package spatutorial.client.components

import cats.effect.{ContextShift, IO}
import diode.react.ReactPot._
import diode.react._
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.services.{Global, UpdateMotd}
import com.rpiaggio.crystal.Flow

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This is a simple component demonstrating how to display async data coming from the server
  */
object Motd {

  implicit val ioTimer = cats.effect.IO.timer
  implicit val ioCS: ContextShift[IO] = IO.contextShift(global)

  val MotdFlow = Flow.flow(Global.InterpreterIO.Motd.motdStream)

  // create the React component for holding the Message of the Day
  val Motd = ScalaComponent.builder[Unit]("Motd")
    .render { _ =>
      Panel(Panel.Props("Message of the day"),
        // render messages depending on the state of the Pot

        MotdFlow( motd =>

          motd.fold(<.p("Loading..."))(msg => <.p(msg))

//        proxy().renderPending(_ > 500, _ => <.p("Loading...")),
//        proxy().renderFailed(ex => <.p("Failed to load")),
//        proxy().render(m => <.p(m)),

        ),


        Button(Button.Props(

          Callback{
            Global.InterpreterIO.Motd.updateMotd.unsafeRunSync()
          }
          , CommonStyle.danger), Icon.refresh, " Update")
      )
    }
//    .componentDidMount(scope =>
//       update only if Motd is empty
//      Callback.when(scope.props.value.isEmpty)(scope.props.dispatchCB(UpdateMotd()))
//    )
    .build

  def apply() = Motd()
}
