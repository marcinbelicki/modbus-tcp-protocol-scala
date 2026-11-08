package pl.belicki.modbus.models.function.decoder

import pl.belicki.modbus.models.ExceptionCode
import pl.belicki.modbus.models.function._
import pl.belicki.modbus.models.function.decoder.ModbusRequestDecoder.ERROR_CODE_ADDITION
import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport

import java.nio.ByteBuffer

class ModbusRequestDecoder(
    protected val supportedFunctions: List[ModbusFunction]
) extends ModbusMessageDecoder[ModbusError, ModbusFunction#Request] {

  override protected def noFunctionCodeError: ModbusError = ModbusError(ExceptionCode.ILLEGAL_FUNCTION, ERROR_CODE_ADDITION)

  override protected def notSupportedFunctionError(functionCode: Byte): ModbusError =
    ModbusError(ExceptionCode.ILLEGAL_FUNCTION, (functionCode + ERROR_CODE_ADDITION).toByte)

  override protected def decodeMessage(modbusFunction: ModbusFunction, byteBuffer: ByteBuffer): Result = modbusFunction.decodeRequest(byteBuffer)
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
