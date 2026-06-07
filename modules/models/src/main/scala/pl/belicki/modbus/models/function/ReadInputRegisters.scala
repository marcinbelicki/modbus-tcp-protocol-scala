package pl.belicki.modbus.models.function

object ReadInputRegisters extends ModbusFunction(0x04) {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

}
