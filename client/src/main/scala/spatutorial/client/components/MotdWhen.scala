package spatutorial.client.components

import java.time.Instant

import cats.effect.IO
import crystal._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._

/**
 * This is a simple component demonstrating how to display async data coming from the server
 */
object MotdWhen {
  val component = ScalaComponent.builder[View[IO, Option[Instant]]]("MotdWhen")
    .render_P { p =>
      p.flow { instantOpt =>
        <.span(
          instantOpt.flatten.whenDefined(instant => <.p(s"Last updated: ${instant}"))
        )
      }
    }
    .build

  def apply(props: View[IO, Option[Instant]]) = component(props)
}
