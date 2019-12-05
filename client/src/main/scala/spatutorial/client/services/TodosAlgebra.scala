package spatutorial.client.services

import cats.effect.Effect
import cats.implicits._
import crystal._
import diode.data._
import spatutorial.shared.{Api, TodoItem}

import autowire._
import boopickle.Default._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.global
import scala.language.higherKinds

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