package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport
import pl.belicki.modbus.models.util.SpacedHex.SpacedHex

import java.nio.ByteBuffer

class ModbusResponseDecoder(
    supportedFunctions: List[ModbusFunction]
) {
  private val functionByCode: Map[Byte, ModbusFunction] = supportedFunctions.groupBy(_.code)
    .map {
      case (byte, List(function)) => (byte, function)
      case (byte, functions)      =>
        throw new IllegalStateException(f"Too many functions: $functions for code: 0x$byte%02X")
    }

  final def decodeHexResponse(hex: String): Either[String, ModbusFunction#Response] = decode(hex.spacedHex)
  final def decode(bytes: Array[Byte]): Either[String, ModbusFunction#Response]     = decode(ByteBuffer.wrap(bytes))

  def decode(byteBuffer: ByteBuffer): Either[String, ModbusFunction#Response] = {
    val functionCode = byteBuffer.get()
    for {
      modbusFunction <- functionByCode.get(functionCode).toRight(f"The functionCode: 0x$functionCode%02X is not supported")
      request        <- modbusFunction.decodeResponse(byteBuffer)
    } yield request
  }
}

object ModbusResponseDecoder {
  val ERROR_CODE_ADDITION: Byte = 0x80.toByte

  def apply(supportedFunctions: ModbusFunction*) = new ModbusResponseDecoder(supportedFunctions.toList)

  val ALL_FUNCTIONS: ModbusResponseDecoder = apply(
    ReadCoils,
    ReadDiscreteInputs,
    ReadHoldingRegisters,
    ReadInputRegisters,
    WriteSingleCoil,
    WriteSingleRegister,
    WriteMultipleCoils,
    WriteMultipleRegisters,
    ReadFileRecord,
    WriteFileRecord,
    MaskWriteRegister,
    ReadWriteMultipleRegisters,
    ReadFIFOQueue,
    EncapsulatedInterfaceTransport
  )
}
