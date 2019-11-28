package crystal

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Timer}
import monocle.Lens

import scala.language.higherKinds

case class Model[F[_] : ConcurrentEffect : Timer, M](initialModel: M) {

  val modelRef: Ref[F, M] = Ref.unsafe[F, M](initialModel)

  def view[A](lens: Lens[M, A]): View[F, A] =
    new View(FixedLens.fromLensAndModelRef(lens, modelRef), lens.get(initialModel))
}
