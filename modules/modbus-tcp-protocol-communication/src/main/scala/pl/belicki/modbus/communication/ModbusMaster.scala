package pl.belicki.modbus.communication

import org.apache.pekko.actor.Actor.emptyBehavior
import org.apache.pekko.actor.{Actor, ActorLogging, ActorRef, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import pl.belicki.modbus.communication.diagnostic.MDCs
import pl.belicki.modbus.communication.util.SpacedHexByteString.{ByteStringExt, ByteStringHelper}
import pl.belicki.modbus.models.ModbusTCPADU
import pl.belicki.modbus.models.function.ModbusFunction
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder

import java.net.{InetAddress, InetSocketAddress}

class ModbusMaster(
    inetSocketAddress: InetSocketAddress,
    modbusRequestDecoder: ModbusRequestDecoder,
    unitByIdentifier: Map[Int, ActorRef]
) extends Actor with DiagnosticActorLogging {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    Map(MDCs.address(inetSocketAddress))

  class ModbusUnit(unitId: Int) extends Actor with DiagnosticActorLogging {

    override def mdc(currentMessage: Any): MDC =
      ModbusMaster.this.mdc(currentMessage) ++ Map(
        "unitId" -> f"U:$unitId%03d"
      )

    override def receive: Receive = emptyBehavior
  }

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
