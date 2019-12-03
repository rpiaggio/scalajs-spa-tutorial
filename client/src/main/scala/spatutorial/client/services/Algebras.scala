package spatutorial.client.services

import cats.effect.{Async, Effect, IO}
import cats.implicits._
import crystal._
import autowire._
import boopickle.Default._
import diode.data._
import spatutorial.shared.{Api, TodoItem}

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
    def updateMotd(): F[Unit]
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

  implicit object MotdAlgebraIO extends MotdAlgebraInterpreter[IO](AppState.motdView)

  trait TodosAlgebra[F[_]] {
    def refreshTodos(): F[Unit]
    def updateAllTodos(todos: Seq[TodoItem]): F[Unit]
    def updateTodo(item: TodoItem): F[Unit]
    def deleteTodo(item: TodoItem): F[Unit]
  }

  class TodosAlgebraInterpreter[F[_] : Effect](lens: SignallingLens[F, Pot[Todos]]) extends TodosAlgebra[F] {
    implicit private val ec: ExecutionContext = global

    protected object Ajax {
      def getAllTodos(): F[Seq[TodoItem]] =
        Async[F].async { cb =>
          AjaxClient[Api].getAllTodos().call().onComplete {
            case Success(value) => cb(Right(value))
            case Failure(t) => cb(Left(t))
          }
        }

      def updateTodo(item: TodoItem): F[Seq[TodoItem]] =
        Async[F].async { cb =>
          AjaxClient[Api].updateTodo(item).call().onComplete {
            case Success(value) => cb(Right(value))
            case Failure(t) => cb(Left(t))
          }
        }

      def deleteTodo(itemId: String): F[Seq[TodoItem]] =
        Async[F].async { cb =>
          AjaxClient[Api].deleteTodo(itemId).call().onComplete {
            case Success(value) => cb(Right(value))
            case Failure(t) => cb(Left(t))
          }
        }
    }

    def refreshTodos(): F[Unit] =
      for {
        todos <- Ajax.getAllTodos()
        _ <- updateAllTodos(todos)
      } yield ()

    def updateAllTodos(todos: Seq[TodoItem]): F[Unit] =
      lens.set(Ready(Todos(todos)))

    def updateTodo(item: TodoItem): F[Unit] =
      for {
        todosPot <- lens.get
        updatedTodos = todosPot.map(_.updated(item))
        _ <- lens.set(updatedTodos)
        newTodos <- Ajax.updateTodo(item)
        _ <- updateAllTodos(newTodos)
      } yield ()

    def deleteTodo(item: TodoItem): F[Unit] =
      for {
        todosPot <- lens.get
        updatedTodos = todosPot.map(_.remove(item))
        _ <- lens.set(updatedTodos)
        newTodos <- Ajax.deleteTodo(item.id)
        _ <- updateAllTodos(newTodos)
      } yield ()
  }

  implicit object TodosAlgebraIO$ extends TodosAlgebraInterpreter[IO](AppState.todosView)
}
