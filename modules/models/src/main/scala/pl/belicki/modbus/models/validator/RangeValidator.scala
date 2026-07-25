package pl.belicki.modbus.models.validator

class RangeValidator(
    min: Int,
    max: Int,
    paramName: String,
    numberFormat: String = "04X"
) {

  def validateBool(value: Int): Boolean = min <= value && value <= max

  def validate(value: Int): Either[String, Unit] =
    Either.cond(
      validateBool(value),
      (),
      String.format(s"The %s of the request: 0x%08X must be inside of the range <0x%$numberFormat;0x%$numberFormat>", paramName, value, min, max)
    )
}
