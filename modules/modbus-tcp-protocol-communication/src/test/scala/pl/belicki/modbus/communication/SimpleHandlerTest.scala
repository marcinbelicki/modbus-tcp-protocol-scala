package pl.belicki.modbus.communication

import org.apache.pekko.actor.{ActorSystem, Props}
import org.apache.pekko.testkit.TestKit
import org.scalatest.wordspec.AnyWordSpecLike

class SimpleHandlerTest extends TestKit(ActorSystem("SimpleHandlerTest")) with AnyWordSpecLike {

  system.actorOf(Props[ModbusTCPServer]())

  while (true) ()

}
