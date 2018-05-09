package com.example.a510.myapplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class BluetoothSerialService {
    // UUID or GUID 생성 장소: http://www.guidgenerator.com/
    private static final UUID nAppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // Bluetooth SPP (Serial Port Profile) Service UUID: use this to communicate with SPP.

    // Member fields
    private final BluetoothAdapter bthAdapter;
    private ConnectThread thConnect;
    private StreamThread thStream;
    private int nState;
    private Context cxAct;
    public String sReadBuffer = "";  // Read를 위한 buffer, 외부에서 읽은 후 초기화해야 함

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public BluetoothSerialService(Context context, BluetoothAdapter adapter) {
        cxAct = context;
        bthAdapter = adapter;
        nState = STATE_NONE;
    }

    private synchronized void setState(int state) {
        nState = state;
    }

    public synchronized int getState() {
        return nState;
    }

    /**
     * Start the chat service. Specifically start ConnectThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (thConnect != null) {
            thConnect.cancel();
            thConnect = null;
        }
        // Cancel any thread currently running a connection
        if (thStream != null) {
            thStream.cancel();
            thStream = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread attempting to make a connection
        if (nState == STATE_CONNECTING) {
            if (thConnect != null) {
                thConnect.cancel();
                thConnect = null;
            }
        }
        // Cancel any thread currently running a connection
        if (thStream != null) {
            thStream.cancel();
            thStream = null;
        }
        // Start the thread to connect with the given device
        thConnect = new ConnectThread(device);
        thConnect.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the StreamThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void startStream(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (thConnect != null) {
            thConnect.cancel();
            thConnect = null;
        }
        // Cancel any thread currently running a connection
        if (thStream != null) {
            thStream.cancel();
            thStream = null;
        }
        // Start the thread to manage the connection and perform transmissions
        thStream = new StreamThread(socket);
        thStream.start();
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (thConnect != null) {
            thConnect.cancel();
            thConnect = null;
        }
        if (thStream != null) {
            thStream.cancel();
            thStream = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the StreamThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see StreamThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        StreamThread str;
        // Synchronize a copy of the StreamThread
        synchronized (this) {
            if (nState != STATE_CONNECTED) return;
            str = thStream;
        }
        // Perform the write unsynchronized
        str.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket bthSocket;
        private final BluetoothDevice bthDevice;

        public ConnectThread(BluetoothDevice device) {
            bthDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(nAppUUID);
            } catch (IOException e) {
            }
            bthSocket = tmp;
        }

        public void run() {
            // Always cancel discovery because it will slow down a connection
            bthAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                bthSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    bthSocket.close();
                } catch (IOException e2) {
                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSerialService.this) {
                thConnect = null;
            }

            // Start the StreamThread
            startStream(bthSocket, bthDevice);
        }

        public void cancel() {
            try {
                bthSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class StreamThread extends Thread {
        private final BluetoothSocket bthSocket;
        private final InputStream bthInStream;
        private final OutputStream bthOutStream;


        public StreamThread(BluetoothSocket socket) {
            bthSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            bthInStream = tmpIn;
            bthOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = bthInStream.read(buffer);
                    if (bytes > 0) {
                        String str = "";
                        for (int i = 0; i < bytes; i++)
                            str += (char)buffer[i];
                        sReadBuffer += str;
                    }
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                bthOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                bthSocket.close();
            } catch (IOException e) {
            }
        }
    }
}

