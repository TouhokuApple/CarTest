package com.example.testcontroller;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

class SocketClientTask extends AsyncTask<String, Void, Void>{
    public static final String CMD_OPEN = "!open";
    public static final String CMD_CLOSE = "!close";
    private static final Object LOCK = new Object();

    public static final String DEFAULT_ADDRESS = "192.168.11.9";
    public static final int DEFAULT_PORT = 12345;
    public static String address = DEFAULT_ADDRESS;
    public static int port = DEFAULT_PORT;
    private static Socket sock = null;

    private static boolean isConnected() {
        return sock != null && sock.isConnected();
    }

    private final Activity activity;
    public SocketClientTask(Activity activity) {
        this.activity = activity;
        Log.d("debug","init");
    }
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
    @Override
    protected Void doInBackground(String... args) {
        synchronized (LOCK) {
            String cmd = args[0];
            if (CMD_OPEN.equals(cmd)) {
                try {
                    if (isConnected()) {
                        sock.close();
                    }
                    InetAddress address = InetAddress.getByName(SocketClientTask.address);
                    sock = new Socket(address, port);
                    Log.d(getClass().getName(), "Connect :" + sock.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (CMD_CLOSE.equals(cmd)) {
                try {
                    if (sock != null) {
                        sock.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String str = cmd;
                byte[] ascii = str.getBytes(Charset.forName("US-ASCII")); // 必ず ASCII コードにエンコードすること
                byte[] message = new byte[ascii.length + 1];
                System.arraycopy(ascii, 0, message, 0, ascii.length);
                message[ascii.length] = '\0';                             // 必ず NULL 文字を追加すること
                try {
                    if (!isConnected()) {
                        InetAddress address = InetAddress.getByName(SocketClientTask.address);
                        sock = new Socket(address, port);
                        Log.d(getClass().getName(), "connet");
                    }
                    OutputStream out = sock.getOutputStream();
                    out.write(message);
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void result) { }
}