package spatutorial.client.components

import cats.effect.IO
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap.{Button, _}
import spatutorial.client.services.Crystal.{MotdAlgebra, View}
import diode.react.ReactPot._
import spatutorial.client.services.Crystal._

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
object Motd2 {
  val Motd = ScalaComponent.builder[View[IO, MotdAlgebra, Pot[String]]]("Motd")
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
          p.actions.updateMotd // This isn't working, flow is not re-rendering :(
          , CommonStyle.danger), Icon.refresh, " Update"),

          Button(Button.Props(
          p.algebra[LogAlgebra].log("You logged!")
          , CommonStyle.danger), Icon.refresh, " Log")
      )
    }
    .componentDidMount(scope =>
      //       update only if Motd is empty
      Callback.when(scope.props.get.isEmpty)(scope.props.actions.updateMotd)
    )
    .build

  def apply(props: View[IO, MotdAlgebra, Pot[String]]) = Motd(props)
}
