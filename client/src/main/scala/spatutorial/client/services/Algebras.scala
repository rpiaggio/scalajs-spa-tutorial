package spatutorial.client.services

import cats.effect.IO

object Algebras {
  implicit object LogAlgebraIO extends LogAlgebraInterpreter[IO]

  implicit object MotdAlgebraIO extends MotdAlgebraInterpreter[IO](AppState.motdFocusView)

  implicit object TodosAlgebraIO extends TodosAlgebraInterpreter[IO](AppState.todosView)
}
