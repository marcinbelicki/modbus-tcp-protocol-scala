package pl.belicki.modbus.models.function

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function.ModbusRequestDecoder.ERROR_CODE_ADDITION
import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport
import pl.belicki.modbus.models.util.SpacedHex

import java.nio.ByteBuffer

class ModbusRequestDecoder(
    supportedFunctions: List[ModbusFunction]
) {
  private val functionByCode: Map[Byte, ModbusFunction] = supportedFunctions.groupBy(_.code)
    .map {
      case (byte, List(function)) => (byte, function)
      case (byte, functions)      =>
        throw new IllegalStateException(f"Too many functions: $functions for code: $byte%02X")
    }


  final def decodeHexRequest(hex: String): Either[ModbusError, ModbusFunction#Request]     = decode(SpacedHex.parseHex(hex))
  final def decode(bytes: Array[Byte]): Either[ModbusError, ModbusFunction#Request] = decode(ByteBuffer.wrap(bytes))

  def decode(byteBuffer: ByteBuffer): Either[ModbusError, ModbusFunction#Request] = {
    val functionCode = byteBuffer.get()
    for {
      modbusFunction <- functionByCode.get(functionCode).toRight(
        ModbusError(ExceptionCode.ILLEGAL_FUNCTION, (functionCode + ERROR_CODE_ADDITION).toByte)
      )
      request <- modbusFunction.decodeRequest(byteBuffer)
    } yield request
  }
}

object ModbusRequestDecoder {
  val ERROR_CODE_ADDITION: Byte = 0x80.toByte

  def apply(supportedFunctions: ModbusFunction*) = new ModbusRequestDecoder(supportedFunctions.toList)

  val ALL_FUNCTIONS: ModbusRequestDecoder = apply(
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
