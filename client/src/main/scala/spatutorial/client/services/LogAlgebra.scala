package spatutorial.client.services

import cats.effect.Sync

import scala.language.higherKinds

trait LogAlgebra[F[_]] {
  def log(msg: String): F[Unit]
}

class LogAlgebraInterpreter[F[_] : Sync] extends LogAlgebra[F] {
  def log(msg: String): F[Unit] = Sync[F].delay(println(msg))
}
