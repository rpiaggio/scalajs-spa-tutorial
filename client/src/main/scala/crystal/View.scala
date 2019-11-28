package crystal

import cats.effect.{ConcurrentEffect, SyncIO, Timer}
import fs2.concurrent.SignallingRef

import scala.language.higherKinds

class View[F[_] : ConcurrentEffect : Timer, A](fixedLens: FixedLens[F, A], initialValue: A) {
  private val ref = SignallingRef.in[SyncIO, F, A](initialValue).unsafeRunSync()

  val flow = Flow.flow(ref.discrete)

  // TODO Flow with pipe. But it can't be a def. We should use the same flow always in the same view.
  // Should it be a parameter of the View? A method .viewWithPipe that creates another flow?
  // Anyway, probably not all Views will have flows.... We can have a .withFlow([pipe]) maybe?

  def get: F[A] = fixedLens.get

  val lens = new SignallingLens[F, A] {
    def set(value: A): F[Unit] = {
      fixedLens.set(value)
      ref.set(value)
    }
  }

  def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra
}
