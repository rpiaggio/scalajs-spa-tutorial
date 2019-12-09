package spatutorial.client.components

import cats.effect.IO
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import react.common.ReactProps
import scalacss.ScalaCssReact._
import spatutorial.client.components.Bootstrap.{Button, CommonStyle}
import spatutorial.shared._
import crystal.react.io.implicits._

final case class TodoList(
  items: Seq[TodoItem],
  stateChange: TodoItem => IO[Unit],
  editItem: TodoItem => IO[Unit],
  deleteItem: TodoItem => IO[Unit]
) extends ReactProps {
  @inline def render: VdomElement = TodoList.component(this)
}

object TodoList {
  type Props = TodoList

  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  val component = ScalaComponent.builder[Props]("TodoList")
    .render_P(p => {
      val style = bss.listGroup
      def renderItem(item: TodoItem) = {
        // convert priority into Bootstrap style
        val itemStyle = item.priority match {
          case TodoLow => style.itemOpt(CommonStyle.info)
          case TodoNormal => style.item
          case TodoHigh => style.itemOpt(CommonStyle.danger)
        }
        <.li(itemStyle,
          <.input.checkbox(^.checked := item.completed, ^.onChange --> p.stateChange(item.copy(completed = !item.completed))),
          <.span(" "),
          if (item.completed) <.s(item.content) else <.span(item.content),
          Button(p.editItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS))("Edit"),
          Button(p.deleteItem(item), addStyles = Seq(bss.pullRight, bss.buttonXS))("Delete")
        )
      }
      <.ul(style.listGroup)(p.items toTagMod renderItem)
    })
    .build
}
