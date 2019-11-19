package spatutorial.client.services

import diode.data.Pot

object Crystal {

  @Lenses
  case class RootModel(todos: Pot[Todos], motd: Pot[String])
}
