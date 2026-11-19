package pl.belicki.modbus.communication

import org.apache.pekko.actor.{Actor, DiagnosticActorLogging}
import org.apache.pekko.event.Logging.MDC
import pl.belicki.modbus.models.function.ModbusFunction
import pl.belicki.modbus.models.function.ModbusFunction.M

class ModbusUnit extends Actor with DiagnosticActorLogging {


  override def mdc(currentMessage: Any): MDC =
    currentMessage match {
      case message: ModbusFunction#Message =>
        Map()
    }


  override def receive: Receive = {
    case
  }
}
