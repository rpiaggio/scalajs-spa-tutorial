package spatutorial.client.modules

import cats.effect.IO
import crystal.View
import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import react.common.ReactProps
import spatutorial.client.SPAMain.{DashboardLoc, Loc, TodoLoc}
import spatutorial.client.components.Bootstrap.CommonStyle
import spatutorial.client.components.Icon._
import spatutorial.client.components._
import spatutorial.client.services._
import scalacss.ScalaCssReact._
import spatutorial.client.services.Algebras.TodosAlgebra
import crystal.implicits._

case class MainMenu(
                     router: RouterCtl[Loc],
                     currentLoc: Loc,
                     view: View[IO, Pot[Todos]]
                   ) extends ReactProps {
  @inline def render: VdomElement = MainMenu.component(this)
}

object MainMenu {
  // shorthand for styles
  @inline private def bss = GlobalStyles.bootstrapStyles

  type Props = MainMenu

  private case class MenuItem(idx: Int, label: (Props) => VdomNode, icon: Icon, location: Loc)

  // build the Todo menu item, showing the number of open todos
  private def buildTodoMenu(props: Props): VdomElement =
    props.view.flow { todosPotOpt =>
      val todoCount = Pot.fromOption(todosPotOpt).flatten.map(_.items.count(!_.completed)).getOrElse(0)
      <.span(
        <.span("Todo "),
        <.span(bss.labelOpt(CommonStyle.danger), bss.labelAsBadge, todoCount).when(todoCount > 0)
      )
    }

  private val menuItems = Seq(
    MenuItem(1, _ => "Dashboard", Icon.dashboard, DashboardLoc),
    MenuItem(2, buildTodoMenu, Icon.check, TodoLoc)
  )

  protected class Backend($: BackendScope[Props, Unit]) {
    def mounted(props: Props) =
    // dispatch a message to refresh the todos
    props.view.get.flatMap { todosPot =>
      if(todosPot.isEmpty)
        props.view.algebra[TodosAlgebra].refreshTodos()
      else
        IO.unit
    }

    def render(props: Props) = {
      <.ul(bss.navbar)(
        // build a list of menu items
        menuItems.toVdomArray(item =>
          <.li(^.key := item.idx, (^.className := "active").when(props.currentLoc == item.location),
            props.router.link(item.location)(item.icon, " ", item.label(props))
          ))
      )
    }
  }

  val component = ScalaComponent.builder[Props]("MainMenu")
    .renderBackend[Backend]
    .componentDidMount(scope => scope.backend.mounted(scope.props))
    .build
}
