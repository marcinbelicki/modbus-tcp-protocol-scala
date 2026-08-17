package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.validator.RangeValidator

trait ReadRegisters extends ReadAddressQuantity {
  this: ModbusFunction =>

  override lazy val quantityValidator: RangeValidator = new RangeValidator(0x0001, 0x007d, "quantity")

  override def validateByteCount(byteCount: Int): Either[String, Unit] =
    Either.cond(byteCount % 2 == 0, (), "The byte count must be an even number.")

}
