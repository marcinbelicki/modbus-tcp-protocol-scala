package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, ActorRef, DiagnosticActorLogging, PoisonPill}
import org.apache.pekko.event.Logging.MDC
import pl.belicki.modbus.communication.diagnostic.MDCs
import pl.belicki.modbus.communication.msg.WithRemote
import pl.belicki.modbus.communication.util.ByteStringUtils.{ByteStringHelper, MessageToByteString}
import pl.belicki.modbus.models.ModbusTCPADU
import pl.belicki.modbus.models.ModbusTCPADU.DecodingFailureWithHeader
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder

import java.net.InetSocketAddress

class ModbusConnectionHandler(
    remoteAddress: InetSocketAddress,
    modbusRequestDecoder: ModbusRequestDecoder,
    masterDevice: ActorRef
) extends Actor
    with DiagnosticActorLogging
    with MessageToByteString {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    Map(MDCs.remote(remoteAddress))

  override def receive: Receive = {
    case Received(data) =>
      log.info(b"Received data: [$data].")
      context.sender()

      ModbusTCPADU.decode(modbusRequestDecoder)(data.asByteBuffer) match {
        case ModbusTCPADU.Succeeded(decoded) =>
          log.info(s"Decoded data properly: $decoded.")
          masterDevice forward WithRemote(remoteAddress, decoded)
        case ModbusTCPADU.DecodingFailure(error) =>
          log.warning(
            s"Could not decode given data properly. Can't respond to the sender because the header couldn't be decoded as well. The error: $error."
          )
          log.warning("Closing the connection.")
          self ! PoisonPill
          sender() ! PoisonPill
        case DecodingFailureWithHeader(header, error) =>
          log.warning(s"Could not decode given data properly. Responding with $error.")
          sender() ! WriteOrClose(ModbusTCPADU(header, error))
      }

    case PeerClosed =>
      log.info("Connection with remote closed.")
      context.stop(self)
  }

}
