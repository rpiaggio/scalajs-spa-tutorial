package spatutorial.client.services

import cats.effect.{ContextShift, IO, Timer}
import crystal.Model
import diode.ActionResult._
import diode.RootModelRW
import diode.data._
import monocle.Iso
import spatutorial.client.services.AppState.RootModel
import spatutorial.shared._
import spatutorial.client.services.Algebras._
import utest._

import scala.concurrent.ExecutionContext.global

object SPACircuitTests extends TestSuite {
  implicit private val timerIO: Timer[IO] = cats.effect.IO.timer(global)
  implicit private val csIO: ContextShift[IO] = IO.contextShift(global)

  def tests = TestSuite {
    'TodoHandler - {
      val model =
        Model[IO, Pot[Todos]](
          Ready(Todos(Seq(
            TodoItem("1", 0, "Test1", TodoLow, completed = false),
            TodoItem("2", 0, "Test2", TodoLow, completed = false),
            TodoItem("3", 0, "Test3", TodoHigh, completed = true)
          )))
        )

      val newTodos = Seq(
        TodoItem("3", 0, "Test3", TodoHigh, completed = true)
      )

      def build = {
        val view = model.view(Iso.id[Pot[Todos]].asLens)
        (view, view.algebra[TodosAlgebra])
      }

/*      'UpdateAllTodos - {
        val (view, alg) = build
        for {
          _ <- alg.updateAllTodos(newTodos)
          result <- view.get
        } yield {
          assert(result == Ready(Todos(newTodos)))
        }
      }

      'UpdateTodoAdd - {
        val (view, alg) = build
        for {
          _ <- alg.updateTodo(TodoItem("4", 0, "Test4", TodoNormal, completed = false))
          result <- view.get
        } yield {
          assert(result.get.items.size == 4)
          assert(result.get.items(3).id == "4")
        }
      }

      'UpdateTodo - {
        val h = build
        val result = h.updateTodo(TodoItem("1", 0, "Test111", TodoNormal, completed = false)))
        result match {
          case ModelUpdateEffect(newValue, effects) =>
            assert(newValue.get.items.size == 3)
            assert(newValue.get.items.head.content == "Test111")
            assert(effects.size == 1)
          case _ =>
            assert(false)
        }
      }

      'DeleteTodo - {
        val h = build
        val result = h.deleteTodo(model.get.items.head))
        result match {
          case ModelUpdateEffect(newValue, effects) =>
            assert(newValue.get.items.size == 2)
            assert(newValue.get.items.head.content == "Test2")
            assert(effects.size == 1)
          case _ =>
            assert(false)
        }
      }
    }

    'MotdHandler - {
      val model: Pot[String] = Ready("Message of the Day!")
      def build = new MotdHandler(new RootModelRW(model))

      'UpdateMotd - {
        val h = build
        var result = h.handle(UpdateMotd())
        result match {
          case ModelUpdateEffect(newValue, effects) =>
            assert(newValue.isPending)
            assert(effects.size == 1)
          case _ =>
            assert(false)
        }
        result = h.handle(UpdateMotd(Ready("New message")))
        result match {
          case ModelUpdate(newValue) =>
            assert(newValue.isReady)
            assert(newValue.get == "New message")
          case _ =>
            assert(false)
        }
      } */
    }
  }
}
