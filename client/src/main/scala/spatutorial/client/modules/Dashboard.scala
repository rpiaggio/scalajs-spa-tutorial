package spatutorial.client.modules

import diode.data.Pot
import japgolly.scalajs.react._
import japgolly.scalajs.react.extra.router.RouterCtl
import japgolly.scalajs.react.vdom.html_<^._
import spatutorial.client.SPAMain.{Loc, TodoLoc}
import spatutorial.client.components._
import crystal._
import cats.effect._
import react.common.ReactProps

import scala.util.Random

final case class Dashboard(
 router: RouterCtl[Loc],
 view: View[IO, Pot[String]]
) extends ReactProps {
  @inline def render: VdomElement = Dashboard.component(this)
}

object Dashboard {
  type Props = Dashboard

//  case class State(motdWrapper: ReactConnectProxy[Pot[String]])

  // create dummy data for the chart
  val cp = Chart.ChartProps(
    "Test chart",
    Chart.BarChart,
    ChartData(
      Random.alphanumeric.map(_.toUpper.toString).distinct.take(10),
      Seq(ChartDataset(Iterator.continually(Random.nextDouble() * 10).take(10).toSeq, "Data1"))
    )
  )

  // create the React component for Dashboard
  private val component = ScalaComponent.builder[Props]("Dashboard")
    // create and store the connect proxy in state for later use
//    .initialStateFromProps(props => State(props.proxy.connect(m => m)))
    .renderPS { (_, props, state) =>
      <.div(
        // header, MessageOfTheDay and chart components
        <.h2("Dashboard"),

        Motd(props.view),

//        state.motdWrapper(Motd(_)),

        Chart(cp),
        // create a link to the To Do view
        <.div(props.router.link(TodoLoc)("Check your todos!"))
      )
    }
    .build
}
