package spatutorial.client.services

import cats.effect.{Async, Effect, IO}
import cats.implicits._
import crystal._
import autowire._
import boopickle.Default._
import diode.data._
import spatutorial.shared.{Api, TodoItem}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.global
import scala.language.higherKinds
import scala.util.{Failure, Success}

object Algebras {
  private def asyncCall[F[_] : Async, A](invocation: => Future[A])(implicit ec: ExecutionContext): F[A] =
    Async[F].async { cb =>
      invocation.onComplete {
        case Success(value) => cb(Right(value))
        case Failure(t) => cb(Left(t))
      }
    }

  trait LogAlgebra[F[_]] {
    def log(msg: String): F[Unit]
  }

  implicit object LogAlgebraIO extends LogAlgebra[IO] {
    def log(msg: String): IO[Unit] = IO(println(msg))
  }

  trait MotdAlgebra[F[_]] {
    def updateMotd(): F[Unit]
  }

  class MotdAlgebraInterpreter[F[_] : Effect](lens: FixedLens[F, Pot[String]]) extends MotdAlgebra[F] {
    implicit protected val ec: ExecutionContext = global

    protected def queryMotd: F[String] =
      asyncCall(AjaxClient[Api].welcomeMsg("User X").call())

    def updateMotd(): F[Unit] =
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

  class TodosAlgebraInterpreter[F[_] : Effect](lens: FixedLens[F, Pot[Todos]]) extends TodosAlgebra[F] {
    implicit protected val ec: ExecutionContext = global

    protected object Ajax {
      def getAllTodos(): F[Seq[TodoItem]] =
        asyncCall(AjaxClient[Api].getAllTodos().call())

      def updateTodo(item: TodoItem): F[Seq[TodoItem]] =
        asyncCall(AjaxClient[Api].updateTodo(item).call())

      def deleteTodo(itemId: String): F[Seq[TodoItem]] =
        asyncCall(AjaxClient[Api].deleteTodo(itemId).call())
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
