package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC

class ModbusUnit extends Actor with DiagnosticActorLogging {


  override def mdc(currentMessage: Any): MDC =
    currentMessage match {
      case ModbusF
    }


  override def receive: Receive = {
    case
  }
}
