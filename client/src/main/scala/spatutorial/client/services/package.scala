package spatutorial.client

import cats.effect.Async

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import scala.language.higherKinds

package object services {
  def asyncCall[F[_] : Async, A](invocation: => Future[A])(implicit ec: ExecutionContext): F[A] =
    Async[F].async { cb =>
      invocation.onComplete {
        case Success(value) => cb(Right(value))
        case Failure(t) => cb(Left(t))
      }
    }
}
