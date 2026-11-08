package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import org.apache.pekko.util.ByteString
import pl.belicki.modbus.communication.util.SpacedHexByteString.ByteStringExt

import java.net.{InetAddress, InetSocketAddress}

class EchoHandler(inetSocketAddress: InetSocketAddress) extends Actor with DiagnosticActorLogging {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    Map("remote" -> s"R:${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}|")

  override def receive: Receive = {
    case Received(data) =>
      log.info(s"Received data: ${data.spacedHex}.")
      sender() ! Write(data)
      sender() ! Write(ByteString("ąąąąą\n"))
    case PeerClosed =>
      log.info(s"Stopping $self")
      context.stop(self)
  }
}
