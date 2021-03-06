package com.zlscorp.ultragrav.communication.transfer;

public class GetPwmDutyCycleResponse extends AbstractResponse {
    private static final long serialVersionUID = 1L;

    public static final Byte RESPONSE_ID = (byte) 0xFE;
    public static final int MIN_RESPONSE_BYTES = 5;

    private double dutyCycle;

    @Override
    public Byte getResponseId() {
        return RESPONSE_ID;
    }

    @Override
    public int getMinimumResponseLength() {
        return MIN_RESPONSE_BYTES;
    }

    @Override
    public void fromResponseBytes(byte[] responseBytes) {

        // Received in range of 65534 - 1, then converted to range 0 - 100%.
        dutyCycle = 100.0 - ((asInt(responseBytes[2], responseBytes[3])) / 655.34);
    }

    public double getDutyCycle() {
        return dutyCycle;
    }

    public void setDutyCycle(int dutyCycle) {
        this.dutyCycle = dutyCycle;
    }

    @Override
    public String toString() {
        return "GetPwmDutyCycleResponse [dutyCycle=" + dutyCycle + "]";
    }
}
