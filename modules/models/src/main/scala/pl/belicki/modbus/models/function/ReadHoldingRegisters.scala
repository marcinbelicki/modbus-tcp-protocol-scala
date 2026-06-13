package pl.belicki.modbus.models.function

object ReadHoldingRegisters extends ModbusFunction(0x03) with ReadRegisters {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  override type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)
}
