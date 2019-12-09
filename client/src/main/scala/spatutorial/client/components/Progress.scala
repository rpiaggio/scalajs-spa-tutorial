package spatutorial.client.components

import cats.effect.{ContextShift, IO, Timer}
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common.ReactProps
import crystal._
import crystal.react.io.implicits._
import crystal.react.Flow
import crystal.react.Flow.ReactFlowComponent

import spatutorial.client.components.Bootstrap.{Button, CommonStyle, Panel}
import spatutorial.client.services.ProgressAlgebraInterpreter
import scala.concurrent.ExecutionContext.global

final case class Progress(
                           view: View[IO, Int]
                         ) extends ReactProps {
  @inline def render: VdomElement = Progress.component(this)

  implicit private val timerIO: Timer[IO] = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  // Algebras can be requested directly, they don't need to go through the implicit mechanism.
  val algebra = new ProgressAlgebraInterpreter[IO](view)

  val total = algebra.MaxProgressSeconds * 1000

  val smoothProgress = algebra.progressFlow(view.stream)
}

object Progress {
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  type Props = Progress

  case class State(smoothFlow: ReactFlowComponent[Int])

  val component = ScalaComponent.builder[Props]("Progress")
    .initialStateFromProps(p => State(Flow.flow(p.smoothProgress)))
    .render { $ =>
      Panel("Progress")(
        $.state.smoothFlow { smoothProgressOpt =>
          <.progress(^.width := 400.px, ^.max := $.props.total, ^.value := smoothProgressOpt.getOrElse(0))
        },
        $.props.view.flow { progressOpt =>
          <.p(s"Progress: ${progressOpt.map(_ + 1).getOrElse(0)} seconds")
        },
        Button(
          $.props.algebra.increment(),
          CommonStyle.danger)(Icon.plus, " Increase"),
      )
    }
    .componentWillMount($ => $.props.algebra.reset())
    .build
}
