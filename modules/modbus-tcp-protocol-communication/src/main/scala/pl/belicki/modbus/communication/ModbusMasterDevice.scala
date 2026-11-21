package pl.belicki.modbus.communication

import org.apache.pekko.actor.Actor.emptyBehavior
import org.apache.pekko.actor.{Actor, ActorLogging, ActorRef, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import pl.belicki.modbus.communication.diagnostic.MDCs
import pl.belicki.modbus.communication.msg.WithRemote
import pl.belicki.modbus.communication.util.ByteStringUtils.ByteStringHelper
import pl.belicki.modbus.models.ModbusTCPADU
import pl.belicki.modbus.models.function.ModbusFunction
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder

import java.net.{InetAddress, InetSocketAddress}

class ModbusMasterDevice extends Actor with DiagnosticActorLogging {
  import org.apache.pekko.io.Tcp._

  override def mdc(currentMessage: Any): MDC =
    WithRemote.mdc(currentMessage)

  class ModbusUnit(unitId: Int) extends Actor with DiagnosticActorLogging {

    override def mdc(currentMessage: Any): MDC =
      ModbusMasterDevice.this.mdc(currentMessage) + ("unitId" -> f"U:$unitId%03d")

    override def receive: Receive = emptyBehavior
  }

  override def receive: Receive = WithRemote {
    case modbusRequest: ModbusFunction#Request =>


    case PeerClosed =>
      log.info("Connection with remote closed.")
      context.stop(self)
  }

  def
}
