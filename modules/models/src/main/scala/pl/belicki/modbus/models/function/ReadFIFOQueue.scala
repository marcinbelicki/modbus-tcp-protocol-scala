package pl.belicki.modbus.models.function

object ReadFIFOQueue extends ModbusFunction(0x18) {

  case class Request(
      address: Int
  ) extends super.Request

}
