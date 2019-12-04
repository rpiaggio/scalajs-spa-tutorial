package spatutorial.client.components

import fs2._
import cats.effect.{ContextShift, IO}
import crystal.Flow.ReactFlowComponent
import crystal._
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common.ReactProps
import spatutorial.client.components.Bootstrap.Panel

import scala.concurrent.ExecutionContext.global

final case class Progress(
                           total: Int,
                           progress: Stream[IO, Int]
                         ) extends ReactProps {
  @inline def render: VdomElement = Progress.component(this)
}

object Progress {
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  type Props = Progress

  case class State(flow: ReactFlowComponent[Int])

  val component = ScalaComponent.builder[Props]("Progress")
    .initialStateFromProps(p => State(Flow.flow(p.progress)))
    .render { $ =>
      Panel("Progress")(
        $.state.flow { progressOpt =>
          <.progress(^.max := $.props.total, ^.value := progressOpt.getOrElse(0))
        }
      )
    }
    .build
}
