package crystal

import scala.language.higherKinds

trait SignallingLens[F[_], A] {
  def set(value: A): F[Unit]
}