package pl.belicki.modbus.models.function

object MaskWriteRegister extends ModbusFunction(0x16) {

  case class Request(
      address: Int,
      andMask: Short,
      orMask: Short
  ) extends super.Request

}
