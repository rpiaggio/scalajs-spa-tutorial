package spatutorial.client.services

import spatutorial.shared.Api
import cats.effect._
import cats.implicits._
import autowire._
import boopickle.Default._
import cats.Functor
import fs2.concurrent.SignallingRef
import diode.data._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.util.{Failure, Success}
import com.rpiaggio.crystal.Flow
import japgolly.scalajs.react.Callback

import monocle._
import monocle.macros.Lenses

import scala.language.higherKinds
import scala.language.implicitConversions

object Crystal {
  implicit private val timerIO = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])

  trait SignallingLens[F[_], A] {
    def set(value: A): F[Unit]
  }

  trait FixedLens[A] {
    def get(): A

    def set(a: A): Unit

    def modifyF[F[_]: Functor](f: A => F[A]): F[Unit]

    def modify(f: A => A): Unit
  }

  def lensToFixedLens[M, A](lens: Lens[M, A], model: M): FixedLens[A] = new FixedLens[A] {
    override def get = lens.get(model)
    override def set(a: A) = lens.set(a)(model)
    override def modifyF[F[_] : Functor](f: A => F[A]) = lens.modifyF(f)(model).map(_ => ())
    override def modify(f: A => A) = lens.modify(f)(model)
  }

  class View[F[_] : ConcurrentEffect : Timer, +G[F[_]], A](fixedLens: FixedLens[A], _actions: SignallingLens[F, A] => G[F]) {
    private val ref = SignallingRef.in[SyncIO, F, A](fixedLens.get).unsafeRunSync()

    val flow = Flow.flow(ref.discrete)

    // TODO Flow with pipe. But it can't be a def. We should use the same flow always in the same slice.
    // Should it be a parameter of the Slice? A method .sliceWithPipe that creates another flow?
    // Anyway, not all Slices will have flows.... We can have a .withFlow([pipe]) maybe?

    def get: A = fixedLens.get

    val lens = new SignallingLens[F, A] {
      def set(value: A): F[Unit] = {
        fixedLens.set(value)

        println(s"SETTING REF TO [$value]")

        ref.set(value)
      }
    }

    val actions = _actions(lens)

    def algebra[H[_[_]]](implicit algebra: H[F]): H[F] = algebra
  }

  case class Model[M](rootModel: M) {

    def view[F[_] : ConcurrentEffect : Timer, G[F[_]], A](lens: Lens[M, A], actions: SignallingLens[F, A] => G[F]): View[F, G, A] =
      new View(lensToFixedLens(lens, rootModel), actions)
  }


  val rootModel = Model(RootModel(Empty, Empty))

  val motdView = rootModel.view(RootModel.motd, pl => MotdAlgebraF[IO](pl))


  trait MotdAlgebra[F[_]] {
    def updateMotd: SyncIO[Unit]
  }

  trait LogAlgebra[F[_]] {
    def log(msg: String): F[Unit]
  }

  implicit object IOAlgebra extends LogAlgebra[IO] {
    def log(msg: String): IO[Unit] = IO(println(msg))
  }


  case class MotdAlgebraF[F[_] : Effect](lens: SignallingLens[F, Pot[String]]) extends MotdAlgebra[F] {
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
        _ = println(s"MOTD QUERIED! $motd")
        _ <- lens.set(Ready(motd))
      } yield ()

    def updateMotd: SyncIO[Unit] =
      Effect[F].runAsync(updateMotdRef) {
        _ =>

          println("DID RUN ASYNC")

          IO.unit // Not Sure how to treat here
      }
  }

  implicit def syncIO2Callback[A](s: SyncIO[A]): Callback  = Callback {

    println("IN IMPLICIT")

    s.unsafeRunSync()
  }

  implicit def io2Callback[A](io: IO[A]): Callback  = Callback {
    io.unsafeRunAsyncAndForget()
  }
}
