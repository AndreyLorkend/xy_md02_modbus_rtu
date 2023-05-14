package com.xymd02.spring;

public abstract class BaseModbusRtuDevice {
    private int readFunction;
    private int writeFunction;

    BaseModbusRtuDevice(int readFunction, int writeFunction) {
        this.readFunction = readFunction;
        this.writeFunction = writeFunction;
    }
    public int getReadFunction() {
        return readFunction;
    }

    public void setReadFunction(int readFunction) {
        this.readFunction = readFunction;
    }

    public int getWriteFunction() {
        return writeFunction;
    }

    public void setWriteFunction(int writeFunction) {
        this.writeFunction = writeFunction;
    }

    public abstract void connect();

    public abstract void disconnect();
}
