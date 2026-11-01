package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, Props}
import org.apache.pekko.io.{IO, Tcp}
import org.apache.pekko.io.Tcp._

import java.net.InetSocketAddress

class ModbusTCPServer extends Actor {
  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 7999))

  override def receive: Receive = {
    case b @ Bound(_) =>
      context.parent ! b
    case CommandFailed(_: Bind) => context.stop(self)
    case _: Connected =>
      val handler = context.actorOf(Props[EchoHandler]())
      val connection = sender()
      connection ! Register(handler)

  }
}
