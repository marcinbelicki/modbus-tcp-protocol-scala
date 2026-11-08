package pl.belicki.modbus.communication

import org.apache.pekko.actor.{ActorSystem, Props}

import java.net.InetSocketAddress

object Aadfasdf extends App {

  val system = ActorSystem("AAA")


  system.actorOf(Props(new ModbusTCPServer(new InetSocketAddress("localhost", 7999))))

  while (true) ()

}
