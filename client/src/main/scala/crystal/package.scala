import cats.effect.{IO, SyncIO}
import japgolly.scalajs.react.Callback
import scala.language.implicitConversions

package object crystal {
  implicit def syncIO2Callback[A](s: SyncIO[A]): Callback  = Callback {
    s.unsafeRunSync()
  }

  implicit def io2Callback[A](io: IO[A]): Callback  = Callback {
    io.unsafeRunAsyncAndForget()
  }
}
