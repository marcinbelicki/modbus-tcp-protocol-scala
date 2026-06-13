package pl.belicki.modbus.models.function

object ReadCoils extends ModbusFunction(0x01) with ReadBits {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)
}
