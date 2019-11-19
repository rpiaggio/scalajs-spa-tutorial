package spatutorial.client.components

import cats.effect.IO
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap._
import spatutorial.client.services.Crystal.{MotdAlgebra, Slice}
import diode.react.ReactPot._
import spatutorial.client.services.Crystal._

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
object Motd2 {
  val Motd = ScalaComponent.builder[Slice[IO, MotdAlgebra, Pot[String]]]("Motd")
    .render_P { p =>
      Panel(Panel.Props("Message of the day"),

        p.flow { motdOpt =>
          val motd = Pot.fromOption(motdOpt).flatten
          <.div(
            motd.renderPending(_ > 500, _ => <.p("Loading...")),
            motd.renderFailed(ex => <.p("Failed to load")),
            motd.render(m => <.p(m))
          )
        },

        Button(Button.Props(
          p.actions.updateMotd
          , CommonStyle.danger), Icon.refresh, " Update")
      )
    }
    .componentDidMount(scope =>
      //       update only if Motd is empty
      Callback.when(scope.props.get.isEmpty)(scope.props.actions.updateMotd)
    )
    .build

  def apply(props: Slice[IO, MotdAlgebra, Pot[String]]) = Motd(props)
}
