package pl.belicki.modbus.communication

import org.apache.pekko.actor.{ActorSystem, Props}
import org.apache.pekko.testkit.TestKit
import org.scalatest.wordspec.AnyWordSpecLike

import java.net.InetSocketAddress

class SimpleHandlerTest extends TestKit(ActorSystem("SimpleHandlerTest")) with AnyWordSpecLike {

  system.actorOf(Props(new ModbusTCPServer(new InetSocketAddress("localhost", 7999))))

  while (true) ()

}
