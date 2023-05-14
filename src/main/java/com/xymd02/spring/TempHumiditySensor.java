package com.xymd02.spring;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.*;
import com.intelligt.modbus.jlibmodbus.master.ModbusMaster;
import com.intelligt.modbus.jlibmodbus.master.ModbusMasterFactory;
import com.intelligt.modbus.jlibmodbus.msg.request.ReadInputRegistersRequest;
import com.intelligt.modbus.jlibmodbus.msg.response.ReadInputRegistersResponse;
import com.intelligt.modbus.jlibmodbus.serial.*;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.utils.DataUtils;
import com.intelligt.modbus.jlibmodbus.utils.FrameEvent;
import com.intelligt.modbus.jlibmodbus.utils.FrameEventListener;

public class TempHumiditySensor extends BaseModbusRtuDevice {
    TempHumiditySensor(int readFunction, int writeFunction) {
        super(readFunction, writeFunction);
    }

    public void connect() {
        try {
            Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);
            SerialParameters serialParameters = new SerialParameters();

            serialParameters.setDevice("COM6");
            // these parameters are set by default
            serialParameters.setBaudRate(SerialPort.BaudRate.BAUD_RATE_115200);
            serialParameters.setDataBits(8);
            serialParameters.setParity(SerialPort.Parity.NONE);
            serialParameters.setStopBits(1);

            SerialUtils.setSerialPortFactory(new SerialPortFactoryLoopback(false));
            ModbusSlave slave = ModbusSlaveFactory.createModbusSlaveRTU(serialParameters);

            SerialUtils.setSerialPortFactory(new SerialPortFactoryLoopback(true));
            ModbusMaster master = ModbusMasterFactory.createModbusMasterRTU(serialParameters);

            master.setResponseTimeout(1000);
            slave.setServerAddress(1);
            slave.setBroadcastEnabled(true);
            slave.setReadTimeout(1000);

            FrameEventListener listener = new FrameEventListener() {
                @Override
                public void frameSentEvent(FrameEvent event) {
                    //System.out.println("frame sent " + DataUtils.toAscii(event.getBytes()));
                }

                @Override
                public void frameReceivedEvent(FrameEvent event) {
                    System.out.println("frame recv " + DataUtils.toAscii(event.getBytes()));
                }
            };

            slave.addListener(listener);
            master.addListener(listener);

            ModbusHoldingRegisters holdingRegisters = new ModbusHoldingRegisters(1000);

            for (int i = 0; i < holdingRegisters.getQuantity(); i++) {
                //fill
                holdingRegisters.set(i, i + 1);
            }

            //place the number PI at offset 0
            holdingRegisters.setFloat64At(0, Math.PI);

            slave.getDataHolder().setInputRegisters(holdingRegisters);

            slave.listen();

            master.connect();

            //prepare request
            ReadInputRegistersRequest request = new ReadInputRegistersRequest();
            request.setServerAddress(1);
            request.setStartAddress(1);
            request.setQuantity(4);
            ReadInputRegistersResponse response = (ReadInputRegistersResponse) request.getResponse();

            master.processRequest(request);
            //ModbusHoldingRegisters registers = response.getHoldingRegisters();
            //for (int r : registers) {
            //    System.out.println(r + ": HEH");
            //}
            //get float
            //System.out.println("PI is approximately equal to " + registers.getFloat64At(0));
            //System.out.println();

            master.disconnect();
            slave.shutdown();
        } catch (ModbusProtocolException e) {
            e.printStackTrace();
        } catch (ModbusIOException e) {
            e.printStackTrace();
        } catch (ModbusNumberException e) {
            e.printStackTrace();
        } catch (SerialPortException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {

    }
}
