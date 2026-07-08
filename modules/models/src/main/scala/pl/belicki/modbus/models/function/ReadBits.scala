package pl.belicki.modbus.models.function

trait ReadBits extends ReadAddressQuantity {
  this: ModbusFunction =>

  final def validateQuantity(quantity: Int): Boolean = quantity <= 2000 && quantity >= 1

  protected def getAddress(request: REQ): Int
  protected def getQuantity(request: REQ): Int

  override def validateRequest(request: REQ): Either[String, REQ] =
    for {
      _ <- Either.cond(getAddress(request) <= 0xffff, (), "The address of the request must be less or equal to 0xffff.")
      _ <- Either.cond(validateQuantity(getQuantity(request)), (), "The quantity of the request is outside of accepted range: <1;2000>.")
    } yield request

}
