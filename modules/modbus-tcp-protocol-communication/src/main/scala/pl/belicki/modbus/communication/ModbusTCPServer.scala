package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, DiagnosticActorLogging, Props}
import org.apache.pekko.io.{IO, Tcp}
import org.apache.pekko.io.Tcp._
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder

import java.net.InetSocketAddress

class ModbusTCPServer(inetSocketAddress: InetSocketAddress) extends Actor with ActorLogging {
  import context.system

  override def preStart(): Unit = {
    IO(Tcp) ! Bind(self, inetSocketAddress)
    super.preStart()
  }

  override def receive: Receive = {
    case bound: Bound =>
      log.info(s"Bound to ${bound.localAddress}.")

    case CommandFailed(bind: Bind) =>
      log.error(s"Binding to ${bind.localAddress} failed. Stopping the actor.")
      context.stop(self)

    case connected: Connected =>
      val handler =
        context.actorOf(Props(new ModbusConnectionHandler(connected.remoteAddress, ModbusRequestDecoder.ALL_FUNCTIONS)))
      log.info(s"Connected to: ${connected.remoteAddress}")
      val connection = sender()
      connection ! Register(handler)
  }
}
