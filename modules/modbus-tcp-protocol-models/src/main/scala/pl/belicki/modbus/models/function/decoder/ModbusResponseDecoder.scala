package pl.belicki.modbus.models.function.decoder

import pl.belicki.modbus.models.function.eit.EncapsulatedInterfaceTransport
import pl.belicki.modbus.models.function._
import pl.belicki.modbus.models.util.SpacedHex.SpacedHex

import java.nio.ByteBuffer

class ModbusResponseDecoder(
    protected val supportedFunctions: List[ModbusFunction]
) extends ModbusMessageDecoder[String, ModbusFunction#Response] {

  override protected def noFunctionCodeError: String = f"The functionCode not found."

  override protected def notSupportedFunctionError(functionCode: Byte): String = f"The functionCode: 0x$functionCode%02X is not supported."

  override protected def decodeMessage(modbusFunction: ModbusFunction, byteBuffer: ByteBuffer): Result = modbusFunction.decodeResponse(byteBuffer)
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
