package crystal

import cats.effect.{ConcurrentEffect, Timer}
import cats.implicits._
import fs2._
import monocle.Lens

import scala.language.higherKinds

class View[F[_] : ConcurrentEffect : Timer, A](fixedLens: FixedLens[F, A], stream: Stream[F, A]) extends SignallingLens[F, A] {
  lazy val flow = Flow.flow(stream)

  // TODO Flow with pipe. But it can't be a def. We should use the same flow always in the same view.
  // Should it be a parameter of the View? A method .viewWithPipe that creates another flow?
  // Anyway, probably not all Views will have flows.... We can have a .withFlow([pipe]) maybe?

  override def get: F[A] =
    fixedLens.get

  override def set(value: A): F[Unit] =
    fixedLens.set(value)

  override def modify(f: A => A): F[Unit] =
    get.flatMap(a => set(f(a)))

  // Useful for getting an algebra already in F[_].
  def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra

  def zoom[B](otherLens: Lens[A, B]): View[F, B] = {
    new View(fixedLens compose otherLens, stream.map(otherLens.get))
  }

  def map[B](f: A => B): ViewRO[F, B] = {
    new ViewRO(get.map(f), stream.map(f))
  }
}

class ViewRO[F[_] : ConcurrentEffect : Timer, A](val get: F[A], stream: Stream[F, A]) {
  lazy val flow = Flow.flow(stream)

  // Useful for getting an algebra already in F[_].
  def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra

  def map[B](f: A => B): ViewRO[F, B] = {
    new ViewRO(get.map(f), stream.map(f))
  }
}


