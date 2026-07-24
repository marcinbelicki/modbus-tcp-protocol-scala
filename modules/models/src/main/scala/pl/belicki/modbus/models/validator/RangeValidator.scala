package pl.belicki.modbus.models.validator

class RangeValidator(
    min: Int,
    max: Int,
    paramName: String,
    numberFormat: String = "04X"
) {

  def validateBool(value: Int): Boolean = min <= value && value <= max

  def validate(value: Int): Either[String, Unit] =
    Either.cond(validateBool(value), (), f"The $paramName of the request: 0x$value%08X must be inside of the range <0x$min%$numberFormat;0x$max%$numberFormat>")
}
