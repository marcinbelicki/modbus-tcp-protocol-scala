package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, Props}
import org.apache.pekko.io.{IO, Tcp}
import org.apache.pekko.io.Tcp._

import java.net.InetSocketAddress

class ModbusTCPServer extends Actor with ActorLogging {
  import context.system
  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 7999))

  override def receive: Receive = {
    case bound: Bound =>
      log.info(s"Bound to ${bound.localAddress}.")
    case CommandFailed(bind: Bind) =>
      log.warning(s"Binding to ${bind.localAddress} failed. Stopping the actor.")
      context.stop(self)
    case connected: Connected =>
      val handler = context.actorOf(Props[EchoHandler]())
      log.info(s"Connected to: ${connected.remoteAddress}")
      val connection = sender()
      connection ! Register(handler)

  }
}
