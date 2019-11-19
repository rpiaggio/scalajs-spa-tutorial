package spatutorial.client.services

import diode.data.Pot
import monocle.macros.Lenses

object Crystal {

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])
}
