package pl.belicki.modbus.models.function

object ReadWriteMultipleRegisters extends ModbusFunction(0x17) {

  case class Request(
      readAddress: Int,
      readQuantity: Int,
      writeAddress: Int,
      writeValue: Array[Byte]
  ) extends super.Request

}
