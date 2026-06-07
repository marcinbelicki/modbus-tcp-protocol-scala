package pl.belicki.modbus.models.function

abstract class ModbusFunction(_code: Int) {
  val code: Byte = _code.toByte

  abstract class Request {
    val function: ModbusFunction = ModbusFunction.this
  }

  abstract class Response {
    val function: ModbusFunction = ModbusFunction.this
  }

  case class Error(exceptionCode: Byte) {
    val functionCode: Byte = Error.code
  }

  object Error {
    val code: Byte = (ModbusFunction.this.code + 0x80).toByte
  }

}
