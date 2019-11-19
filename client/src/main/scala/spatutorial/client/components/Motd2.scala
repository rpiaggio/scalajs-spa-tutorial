package spatutorial.client.components

import cats.effect.IO
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.services.Crystal.{MotdAlgebra, Slice}
import diode.react.ReactPot._

/**
  * This is a simple component demonstrating how to display async data coming from the server
  */
object Motd2 {

//  implicit val ioTimer = cats.effect.IO.timer
//  implicit val ioCS: ContextShift[IO] = IO.contextShift(global)
//
//  val MotdFlow = Flow.flow(Global.InterpreterIO.Motd.motdStream)

  // create the React component for holding the Message of the Day
  val Motd = ScalaComponent.builder[Slice[IO, MotdAlgebra, Pot[String]]]("Motd")
    .render_P { p =>
      Panel(Panel.Props("Message of the day"),
        // render messages depending on the state of the Pot

        p.flow( motd =>
<.div(
//          motd.fold(<.p("Loading..."))(msg => <.p(msg))
//
          motd.get.renderPending(_ > 500, _ => <.p("Loading...")),
          motd.get.renderFailed(ex => <.p("Failed to load")),
          motd.get.render(m => <.p(m))
)
        ),


        Button(Button.Props(

          Callback{
            p.actions.updateMotd.unsafeRunSync()
          }
          , CommonStyle.danger), Icon.refresh, " Update")
      )
    }
//    .componentDidMount(scope =>
//       update only if Motd is empty
//      Callback.when(scope.props.value.isEmpty)(scope.props.dispatchCB(UpdateMotd()))
//    )
    .build

  def apply(props: Slice[IO, MotdAlgebra, Pot[String]]) = Motd(props)
}
