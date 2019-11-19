package spatutorial.client.services

import spatutorial.shared.Api
import cats.effect._
import cats.implicits._
import autowire._
import boopickle.Default._
import fs2.concurrent.SignallingRef

import diode.data._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}
import com.rpiaggio.crystal.Flow

import monocle._
import monocle.macros.Lenses

import scala.language.higherKinds

object Crystal {
  implicit private val timerIO = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])

  trait PowerLens[F[_], A] {
    def set(value: A): F[Unit]
  }

  class Slice[F[_] : ConcurrentEffect : Timer, +G[F[_]], M, A](private val model: M, _lens: Lens[M, A], _actions: PowerLens[F, A] => G[F]) {
    private val ref = SignallingRef.in[SyncIO, F, A](_lens.get(model)).unsafeRunSync()

    val flow = Flow.flow(ref.discrete)

    val lens = new PowerLens[F, A] {
      def set(value: A): F[Unit] = {
        _lens.set(value)(model)
        ref.set(value)
      }
    }

    val actions = _actions(lens)
  }

  case class Model[M](rootModel: M) {

    def slice[F[_] : ConcurrentEffect : Timer, G[F[_]], A](lens: Lens[M, A], actions: PowerLens[F, A] => G[F]): Slice[F, G, M, A] =
      new Slice(rootModel, lens, actions)
  }


  val rootModel = Model(RootModel(Empty, Empty))

  val motdSlice = rootModel.slice(RootModel.motd, {pl: PowerLens[IO, Pot[String]] => MotdAlgebraF[IO](pl)})


  trait MotdAlgebra[F[_]] {
    def updateMotd: SyncIO[Unit]
  }

  case class MotdAlgebraF[F[_] : Effect](lens: PowerLens[F, Pot[String]]) extends MotdAlgebra[F] {
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
        _ <- Effect[F].pure(lens.set(Ready(motd)))
      } yield ()

    def updateMotd: SyncIO[Unit] =
      Effect[F].runAsync(updateMotdRef) {
        _ => IO.unit // Not Sure how to treat here
      }
  }
}
