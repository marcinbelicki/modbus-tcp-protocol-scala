package pl.belicki.modbus.models.function.eit;

public enum ReadDeviceIdCode {
    Basic(0x01),
    Regular(0x02),
    Extended(0x03),
    Specific(0x04);
    
    private final byte code;

    ReadDeviceIdCode(int _code) {
        this.code = (byte) _code;
    }

    public final byte getCode() {
        return this.code;
    }

}
