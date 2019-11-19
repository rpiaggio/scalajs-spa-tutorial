package spatutorial.client.services

import spatutorial.shared.Api
import cats.effect._
import cats.implicits._
import autowire._
import boopickle.Default._
import fs2._
import fs2.concurrent.SignallingRef

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.global

class Model[F[_] : Concurrent] {
  val motd: SignallingRef[F, String] = Model.init[F, String]("")
}

object Model {
  def init[F[_] : Concurrent, A](default: A): SignallingRef[F, A] =
    SignallingRef.in[SyncIO, F, A](default).unsafeRunSync()
}

trait MotdAlgebra[F[_]] {
  def updateMotd: SyncIO[Unit]
}

class MotdAlgebraF[F[_] : Effect : Model] extends MotdAlgebra[F] {
  implicit private val ec: ExecutionContext = global

  protected def queryMotd: F[String] =
    Async[F].async { cb =>
      AjaxClient[Api].welcomeMsg("User X").call().onComplete {
        case Success(value) => cb(Right(value))
        case Failure(t) => cb(Left(t))
      }
    }

  protected def updateMotdRef: F[Unit] =
    for {
      motd <- queryMotd
      _ <- implicitly[Model[F]].motd.set(motd)
    } yield ()

  def updateMotd: SyncIO[Unit] =
    Effect[F].runAsync(updateMotdRef) {
      _ => IO.unit // Not Sure how to treat here
    }

  def motdStream: Stream[F, String] =
    implicitly[Model[F]].motd.discrete
}


abstract class Interpreter[F[_] : Effect : Model] {
  val Motd = new MotdAlgebraF[F]
}

object Global {
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  implicit private val modelIO: Model[IO] = new Model[IO]

  object InterpreterIO extends Interpreter[IO]

}
