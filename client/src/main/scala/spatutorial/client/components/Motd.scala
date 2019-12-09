package spatutorial.client.components

import cats.effect.IO
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.components.Bootstrap.{Button, _}
import crystal._
import crystal.react.implicits._
import spatutorial.client.services.Algebras._
import diode.react.ReactPot._
import spatutorial.client.services.AppState.MotdFocus
import spatutorial.client.services.{LogAlgebra, MotdAlgebra}

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
object Motd {
  val component = ScalaComponent.builder[View[IO, MotdFocus]]("Motd")
    .render_P { p =>
      Panel("Message of the day")(
        p.flow { motdFocusOpt =>
          val motd = Pot.fromOption(motdFocusOpt).map(_.motd).flatten
          <.div(
            motd.renderPending(_ > 500, _ => <.p("Loading...")),
            motd.renderFailed(ex => <.p("Failed to load")),
            motd.render(m => <.p(m))
          )
        },

        MotdWhen(p.zoom(MotdFocus.motdInstant)),

        Button(
          p.algebra[MotdAlgebra].updateMotd(),
          CommonStyle.danger)(Icon.refresh, " Update"),

        Button(
          p.get.flatMap(motd => p.algebra[LogAlgebra].log(s"You logged [$motd]!")),
          CommonStyle.danger)(Icon.refresh, " Log")
      )
    }
    .componentDidMount(scope =>
      //       update only if Motd is empty
      scope.props.algebra[MotdAlgebra].updateMotd()
        .when(scope.props.get.map(_.motd.isEmpty))
    )
    .build

  def apply(props: View[IO, MotdFocus]) = component(props)
}
