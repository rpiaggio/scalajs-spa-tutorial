package crystal

import scala.language.higherKinds

trait SignallingLens[F[_], A] {
  def get: F[A]
  def set(a: A): F[Unit]
  def modify(f: A => A): F[Unit]
}