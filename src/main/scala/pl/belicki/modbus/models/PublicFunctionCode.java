package pl.belicki.modbus.models;

public enum PublicFunctionCode implements FunctionCode {
    ReadCoils(0x01),
    ReadDiscreteInputs(0x02),
    ReadHoldingRegisters(0x03),
    ReadInputRegisters(0x04),
    WriteSingleCoil(0x05),
    WriteSingleRegister(0x06),
    ReadExceptionStatus(0x07),
    WriteMultipleCoils(0x0F),
    WriteMultipleRegisters(0x10),
    ReadFileRecord(0x14),
    WriteFileRecord(0x15),
    MaskWriteRegister(0x16),
    ReadWriteMultipleRegisters(0x17),
    ReadFIFOQueue(0x18),
    EncapsulatedInterfaceTransport(0x2B);

    @Override
    public byte getCode() {
        return this.code;
    }

    private final byte code;

    PublicFunctionCode(int code) {
        this.code = (byte) code;
    }


}
