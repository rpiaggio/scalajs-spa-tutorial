package spatutorial.client.services

import spatutorial.shared.Api
import cats.effect._
import cats.implicits._
import autowire._
import boopickle.Default._
import diode.data._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}
import japgolly.scalajs.react.Callback
import crystal._

import monocle.macros.Lenses

import scala.language.higherKinds
import scala.language.implicitConversions

object AppState {
  implicit private val timerIO = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])

  val rootModel = Model[IO, RootModel](RootModel(Empty, Empty))

  val motdView = rootModel.view[Pot[String]](RootModel.motd)


  trait LogAlgebra[F[_]] {
    def log(msg: String): F[Unit]
  }

  implicit object LogAlgebraIO extends LogAlgebra[IO] {
    def log(msg: String): IO[Unit] = IO(println(msg))
  }

  trait MotdAlgebra[F[_]] {
    def updateMotd: SyncIO[Unit]
  }

  class MotdAlgebraInterpreter[F[_] : Effect](lens: SignallingLens[F, Pot[String]]) extends MotdAlgebra[F] {
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
        _ <- lens.set(Ready(motd))
      } yield ()

    def updateMotd: SyncIO[Unit] =
      Effect[F].runAsync(updateMotdRef) {
        _ => IO.unit // Not Sure how to treat here
      }
  }

  implicit object MotdAlgebraIO extends MotdAlgebraInterpreter[IO](motdView.lens)

  implicit def syncIO2Callback[A](s: SyncIO[A]): Callback  = Callback {
    s.unsafeRunSync()
  }

  implicit def io2Callback[A](io: IO[A]): Callback  = Callback {
    io.unsafeRunAsyncAndForget()
  }
}
