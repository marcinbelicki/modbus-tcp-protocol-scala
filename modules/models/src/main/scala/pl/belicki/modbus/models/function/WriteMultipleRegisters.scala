package pl.belicki.modbus.models.function

object WriteMultipleRegisters extends ModbusFunction(0x10) {

  case class Request(
      address: Int,
      quantity: Int,
      value: Array[Byte]
  ) extends super.Request

}
