package spatutorial.client.services

import java.time.Instant

import cats.effect.Effect
import cats.implicits._
import crystal._
import diode.data._
import spatutorial.client.services.AppState.MotdFocus
import spatutorial.shared.Api
import autowire._
import boopickle.Default._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global

import scala.language.higherKinds

trait MotdAlgebra[F[_]] {
  def updateMotd(): F[Unit]
}

class MotdAlgebraInterpreter[F[_] : Effect](lens: FixedLens[F, MotdFocus]) extends MotdAlgebra[F] {
  implicit protected val ec: ExecutionContext = global

  protected def queryMotd: F[String] =
    asyncCall(AjaxClient[Api].welcomeMsg("User X").call())

  def updateMotd(): F[Unit] =
    for {
      motd <- queryMotd
      _ <- lens.set(MotdFocus(Ready(motd), Instant.now.some))
    } yield ()
}
