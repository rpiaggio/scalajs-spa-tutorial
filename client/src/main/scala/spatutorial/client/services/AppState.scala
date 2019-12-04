package spatutorial.client.services

import cats.effect._
import diode.data._

import scala.concurrent.ExecutionContext.global
import crystal._

import monocle.macros.Lenses

object AppState {
  implicit private val timerIO: Timer[IO] = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])

  val rootModel = Model[IO, RootModel](RootModel(Empty, Empty))

  val todosView: View[IO, Pot[Todos]] = rootModel.view(RootModel.todos)

  val motdView: View[IO, Pot[String]] = rootModel.view(RootModel.motd)
}
