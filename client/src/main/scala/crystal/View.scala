package crystal

import cats.effect.ConcurrentEffect
import cats.implicits._
import crystal.Flow.ReactFlowComponent
import fs2._
import monocle.Lens

import scala.language.higherKinds

// ConcurrentEffect is needed by Flow, to .runCancelable the stream.
sealed abstract class ViewBase[F[_] : ConcurrentEffect, A](stream: Stream[F, A]) {
  lazy val flow: ReactFlowComponent[A] = Flow.flow(stream)

  def get: F[A]

  // Useful for getting an algebra already in F[_].
  def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra

  def map[B](f: A => B): ViewRO[F, B] = {
    new ViewRO(get.map(f), stream.map(f))
  }
}

class View[F[_] : ConcurrentEffect, A](fixedLens: FixedLens[F, A], stream: Stream[F, A])
  extends ViewBase[F, A](stream) with FixedLens[F, A] {
  // TODO Flow with pipe. But it can't be a def. We should use the same flow always in the same view.
  // Should it be a parameter of the View? A method .viewWithPipe that creates another flow?
  // Anyway, probably not all Views will have flows.... We can have a .withFlow([pipe]) maybe?

  // Convenience delegates for FixedLens
  override def get: F[A] =
    fixedLens.get

  override def set(value: A): F[Unit] =
    fixedLens.set(value)

  override def modify(f: A => A): F[Unit] =
    fixedLens.modify(f)

  override protected[crystal] def compose[B](otherLens: Lens[A, B]): FixedLens[F, B] =
    throw new Exception("Views cannot be composed with other lenses. Try .zoom or .map instead.")

  def zoom[B](otherLens: Lens[A, B]): View[F, B] = {
    new View(fixedLens compose otherLens, stream.map(otherLens.get))
  }
}

class ViewRO[F[_] : ConcurrentEffect, A](val get: F[A], stream: Stream[F, A]) extends ViewBase[F, A](stream)


