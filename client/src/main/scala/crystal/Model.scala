package crystal

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Timer}
import monocle.Lens

import scala.language.higherKinds

case class Model[F[_] : ConcurrentEffect : Timer, M](initialModel: M) {

  val modelRef: Ref[F, M] = Ref.unsafe[F, M](initialModel)

  // For the moment, views must be defined at the beginning of the app, same for composed ones.
  // This is because this way we can get the initial value immediately. (Getting a value is wrapped in an effect)
  // Of course, we should explore other options:
  // - Procure a View in an effect.
  // - If the effect can be run synchronously, run it and obtain the value.
  // - Or, does it make sense to use 2 effects: a Sync one for accessing the ref, and a general ConcurrentEffect ?
  //   In that case, we have to make sure the sync one is convertible to the general one.
  def view[A](lens: Lens[M, A]): View[F, A] = {
    val fixedLens = FixedLens.fromLensAndModelRef(lens, modelRef)
    new View(fixedLens, lens.get(initialModel))
  }
}
