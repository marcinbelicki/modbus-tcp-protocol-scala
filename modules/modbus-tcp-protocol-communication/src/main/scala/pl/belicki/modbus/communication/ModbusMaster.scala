package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorLogging, ActorRef, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import org.apache.pekko.util.ByteString
import pl.belicki.modbus.communication.util.SpacedHexByteString.{ByteStringExt, ByteStringHelper}
import pl.belicki.modbus.models.ModbusTCPADU
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder

import java.net.{InetAddress, InetSocketAddress}

class ModbusMaster(
    inetSocketAddress: InetSocketAddress,
    modbusRequestDecoder: ModbusRequestDecoder,
    unitByIdentifier: Map[Int, ActorRef]
) extends Actor with DiagnosticActorLogging {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    Map("remote" -> s"R:${inetSocketAddress.getHostString}:${inetSocketAddress.getPort}|")

  override def receive: Receive = {
    case Received(data) =>
      log.info(b"Received data: [$data].")

      ModbusTCPADU.decode(modbusRequestDecoder)(data.asByteBuffer) match {
        case Right(decoded) =>
          log.info(s"Decoded data properly: $decoded.")
          import decoded.header.unitIdentifier
          unitByIdentifier.get(unitIdentifier) match {
            case Some(unit) => unit ! decoded
            case None       =>
              log.warning(s"Could not find unit with id $unitIdentifier")
          }
        case Left(value) =>
          log.warning(s"Could not decode given data properly. Responding with $value.")
      }

    case PeerClosed =>
      log.info("Connection with remote closed.")
      context.stop(self)
  }
}
