package pl.belicki.modbus.models.function

object ReadCoils extends ModbusFunction(0x01) with ReadBits {

  case class Request(
      address: Int,
      quantity: Int
  ) extends super.Request

  type REQ = Request

  override def toRequest(address: Int, quantity: Int): Request = Request(address, quantity)

  override def validateRequest(request: Request): Either[String, Request] =
    for {
      _ <- Either.cond(request.address <= 0xffff, (), "The address of the request must be less or equal to 0xffff.")
      _ <- Either.cond(validateQuantity(request.quantity), (), "The quantity of the request is outside of accepted range: <1;2000>.")
    } yield request
}
