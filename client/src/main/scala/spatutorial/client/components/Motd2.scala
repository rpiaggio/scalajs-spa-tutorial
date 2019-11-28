package spatutorial.client.components

import cats.effect.IO
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap.{Button, _}
import crystal._
import spatutorial.client.services.AppState.{MotdAlgebra, _}
import diode.react.ReactPot._

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
object Motd2 {
  val Motd = ScalaComponent.builder[View[IO, Pot[String]]]("Motd")
    .render_P { p =>

      println(s"THIS IS MOTD2 RENDERING: ${p.get}")

      Panel(Panel.Props("Message of the day"),

        "THIS IS MOTD2 RENDERING",

        p.flow { motdOpt =>

          println(s"THIS FLOW RENDERING $motdOpt")

          val motd = Pot.fromOption(motdOpt).flatten
          <.div(
            motd.renderPending(_ > 500, _ => <.p("Loading...")),
            motd.renderFailed(ex => <.p("Failed to load")),
            motd.render(m => <.p(m))
          )
        },

        Button(Button.Props(
          p.algebra[MotdAlgebra].updateMotd
          , CommonStyle.danger), Icon.refresh, " Update"),

          Button(Button.Props(
            p.get.flatMap(motd => p.algebra[LogAlgebra].log(s"You logged [$motd]!"))
          , CommonStyle.danger), Icon.refresh, " Log")
      )
    }
    .componentDidMount(scope =>
      //       update only if Motd is empty
      scope.props.get.flatMap { motdPot =>
        if(motdPot.isEmpty)
          scope.props.algebra[MotdAlgebra].updateMotd.to[IO]
        else
          IO.unit
      }
    )
    .build

  def apply(props: View[IO, Pot[String]]) = Motd(props)
}
