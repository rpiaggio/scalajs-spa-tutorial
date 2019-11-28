package spatutorial.client.services

import cats.effect.{Async, Effect, IO}
import cats.implicits._
import crystal._
import autowire._
import boopickle.Default._
import diode.data._
import spatutorial.client.services.AppState.motdView
import spatutorial.shared.Api

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}

import scala.language.higherKinds

object Algebras {
  trait LogAlgebra[F[_]] {
    def log(msg: String): F[Unit]
  }

  implicit object LogAlgebraIO extends LogAlgebra[IO] {
    def log(msg: String): IO[Unit] = IO(println(msg))
  }

  trait MotdAlgebra[F[_]] {
    def updateMotd: F[Unit]
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

    def updateMotd: F[Unit] =
      for {
        motd <- queryMotd
        _ <- lens.set(Ready(motd))
      } yield ()
  }

  implicit object MotdAlgebraIO extends MotdAlgebraInterpreter[IO](motdView)
}
