package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.validator.RangeValidator

trait ReadBits extends ReadAddressQuantity {
  this: ModbusFunction =>

  override lazy val quantityValidator: RangeValidator = new RangeValidator(0x0001, 0x07d0, "quantity")

  override def validateByteCount(byteCount: Int): Either[String, Unit] = Right(())

}
