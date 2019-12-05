package spatutorial.client.services

import cats.effect.{Concurrent, Sync, Timer}
import crystal._
import fs2._

import scala.concurrent.duration._
import scala.language.higherKinds

trait ProgressAlgebra[F[_]] {
  def reset(): F[Unit]
  def increment(): F[Unit]
}

class ProgressAlgebraInterpreter[F[_] : Concurrent : Timer](lens: FixedLens[F, Int]) extends ProgressAlgebra[F] {
  val MaxProgressSeconds = 10
  val JumpMillis = 50

  def reset(): F[Unit] = lens.set(0)
  def increment(): F[Unit] = lens.modify(v => math.min(v + 1, MaxProgressSeconds - 1))

  protected def smoothFlow(init: Int): Stream[F, Int] = {
    Stream.iterateEval(init * 1000)(i => Sync[F].delay(i + JumpMillis))
      .takeWhile(_ <= (math.min(init + 1, MaxProgressSeconds) * 1000))
      .covary[F]
      .metered(JumpMillis.milliseconds)
  }

  def progressFlow(processProgressFlow: Stream[F, Int]): Stream[F, Int] =
    processProgressFlow.switchMap(smoothFlow)//.evalTap(p => Sync[F].delay(println(p)))
}
