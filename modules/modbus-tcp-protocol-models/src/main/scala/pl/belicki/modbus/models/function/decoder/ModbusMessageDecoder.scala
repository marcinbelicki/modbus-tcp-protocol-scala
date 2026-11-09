package pl.belicki.modbus.models.function.decoder

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.{ModbusError, ModbusFunction}
import pl.belicki.modbus.models.util.SpacedHex

import java.nio.ByteBuffer

trait ModbusMessageDecoder[E, M <: ModbusFunction#Message] {

  type Result = Either[E, M]

  protected def supportedFunctions: List[ModbusFunction]

  private lazy val functionByCode: Map[Byte, Either[E, ModbusFunction]] = supportedFunctions.groupBy(_.code)
    .map {
      case (byte, List(function)) => (byte, Right(function))
      case (byte, functions)      =>
        throw new IllegalStateException(f"Too many functions: $functions for code: $byte%02X")
    }
    .withDefault(functionCode => Left(notSupportedFunctionError(functionCode)))

  final def decodeHex(hex: String): Result     = decode(SpacedHex.parseHex(hex))
  final def decode(bytes: Array[Byte]): Result = decode(ByteBuffer.wrap(bytes))

  protected def noFunctionCodeError: E
  protected def notSupportedFunctionError(functionCode: Byte): E
  protected def decodeMessage(modbusFunction: ModbusFunction, byteBuffer: ByteBuffer): Result
  def dataTooLong: E
  def dataTooShort: E

  def decode(byteBuffer: ByteBuffer): Result =
    for {
      functionCode   <- Either.cond(byteBuffer.hasRemaining, byteBuffer.get(), noFunctionCodeError)
      modbusFunction <- functionByCode(functionCode)
      message        <- decodeMessage(modbusFunction, byteBuffer)
    } yield message
}
