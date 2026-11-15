package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import org.apache.pekko.util.ByteString
import pl.belicki.modbus.communication.util.SpacedHexByteString.{ByteStringHelper, ByteStringExt}

import java.net.{InetAddress, InetSocketAddress}

class ModbusMaster(inetSocketAddress: InetSocketAddress) extends Actor with DiagnosticActorLogging {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    Map("remote" -> s"R:${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}|")

  override def receive: Receive = {
    case Received(data) =>
      log.info(b"Received data: [$data].")
    case PeerClosed =>
      log.info("Connection with remote closed.")
      context.stop(self)
  }
}
