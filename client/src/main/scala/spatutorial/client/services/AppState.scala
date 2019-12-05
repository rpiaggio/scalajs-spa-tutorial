package spatutorial.client.services

import java.time.Instant

import cats.effect._
import diode.data._

import scala.concurrent.ExecutionContext.global
import crystal._
import monocle.Lens
import monocle.macros.Lenses

object AppState {
  implicit private val timerIO: Timer[IO] = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  @Lenses
  case class RootModel(
                        todos: Pot[Todos],
                        motd: Pot[String],
                        motdInstant: Option[Instant],
                        progress: Int
                      )

  val rootModel = Model[IO, RootModel](RootModel(Empty, Empty, None, 0))

  val todosView: View[IO, Pot[Todos]] = rootModel.view(RootModel.todos)

  @Lenses
  case class MotdFocus(motd: Pot[String], motdInstant: Option[Instant])

  val motdFocusL: Lens[RootModel, MotdFocus] =
    Lens[RootModel, MotdFocus](m => MotdFocus(m.motd, m.motdInstant))(
      f => _.copy(motd = f.motd, motdInstant = f.motdInstant))

  val motdFocusView: View[IO, MotdFocus] = rootModel.view(motdFocusL)

  val progressView: View[IO, Int] = rootModel.view(RootModel.progress)
}
