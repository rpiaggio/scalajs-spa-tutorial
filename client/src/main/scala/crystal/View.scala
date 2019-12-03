package crystal

import cats.effect.{ConcurrentEffect, SyncIO, Timer}
import cats.implicits._
import fs2.concurrent.SignallingRef
import monocle.Lens

import scala.language.higherKinds

class View[F[_] : ConcurrentEffect : Timer, A](fixedLens: FixedLens[F, A], initialValue: A) extends SignallingLens[F, A] {
  private val ref = SignallingRef.in[SyncIO, F, A](initialValue).unsafeRunSync()

  lazy val flow = Flow.flow(ref.discrete)

  // TODO Flow with pipe. But it can't be a def. We should use the same flow always in the same view.
  // Should it be a parameter of the View? A method .viewWithPipe that creates another flow?
  // Anyway, probably not all Views will have flows.... We can have a .withFlow([pipe]) maybe?

  override def get: F[A] = fixedLens.get

  override def set(value: A): F[Unit] =
    fixedLens.set(value) *> ref.set(value)

  override def modify(f: A => A): F[Unit] =
    get.flatMap(a => set(f(a)))

  def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra

  // Is this really useful? Most of the times we need an immediate View, not one within a (non-sync) effect.
  def zoom[B](otherLens: Lens[A, B]): F[View[F, B]] = {
    val newLens = fixedLens compose otherLens
    for {
      a <- newLens.get
    } yield {
      new View(fixedLens compose otherLens, a)
    }
  }

  def zoom[B](otherLens: Lens[A, B], withInitialValue: B): View[F, B] = {
    val newLens = fixedLens compose otherLens
    new View(fixedLens compose otherLens, withInitialValue)
  }
}


