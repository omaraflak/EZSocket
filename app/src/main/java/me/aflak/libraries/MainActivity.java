package me.aflak.libraries;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import me.aflak.ezsocket.EZSocket;

public class MainActivity extends AppCompatActivity {
    public final static String TAG = "EZSocket";
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.text);

        EZSocket socket = new EZSocket("192.168.0.8", 1234, new EZSocket.EZSocketCallback() {
            @Override
            public void onConnect(EZSocket socket) {
                print("Connected");
            }

            @Override
            public void onDisconnect(EZSocket socket, String message) {
                print("Disconnected: "+message);
            }

            @Override
            public void onConnectError(final EZSocket socket, String message) {
                print("Error: "+message);
                print("New attempt in 5 sec...");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                socket.connect();
                            }
                        }, 5000);
                    }
                });
            }
        });

        // server is sending: socket.emit("message", "Omar Aflak", 18);

        socket.on("message", new EZSocket.Listener() {
            @Override
            public void onCall(Object... obj) {
                String msg = (String) obj[0];
                Integer n = (Integer) obj[1];
                print(msg+" : "+String.valueOf(n));
            }
        });

        socket.connect();
    }

    public void print(final String message){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(message+"\n");
            }
        });
    }
}
