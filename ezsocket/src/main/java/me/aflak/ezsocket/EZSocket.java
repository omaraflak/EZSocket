package me.aflak.ezsocket;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Omar on 13/05/2016.
 */
public class EZSocket {
    private Socket socket;
    private SocketAddress address;
    private EZSocketCallback callback;
    private EZSocketDisconnectCallback disconnect_callback;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private List<Pair<String, Listener>> events;

    public EZSocket(String address, int port){
        this.socket = new Socket();
        this.address = new InetSocketAddress(address, port);
        this.callback = null;
        this.events = new ArrayList<>();
    }

    public EZSocket(String address, int port, EZSocketCallback callback){
        this.socket = new Socket();
        this.address = new InetSocketAddress(address, port);
        this.callback = callback;
        this.events = new ArrayList<>();
    }

    public EZSocket(Socket socket, EZSocketDisconnectCallback disconnect_callback) throws IOException {
        this.socket = socket;
        this.disconnect_callback = disconnect_callback;
        this.in = new ObjectInputStream(socket.getInputStream());
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.events = new ArrayList<>();

        new Listen().start();
    }

    public void connect(){
        new Connect(-1).start();
    }

    public void connect(int timeout){
        new Connect(timeout).start();
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            if(callback!=null)
                callback.onDisconnect(EZSocket.this, e.getMessage());
            if(disconnect_callback!=null)
                disconnect_callback.onDisconnect(EZSocket.this, e.getMessage());
        }
    }

    private class Listen extends Thread implements Runnable{
        public void run(){
            Object o[];

            try {
                while(true) {
                    o = (Object[]) in.readObject();

                    for (Pair<String, Listener> e : events){
                        if(o[0].equals(e.first)){
                            e.second.onCall(Arrays.copyOfRange(o, 1, o.length));
                            break;
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                disconnect();
                if(callback!=null)
                    callback.onDisconnect(EZSocket.this, e.getMessage());
                if(disconnect_callback!=null)
                    disconnect_callback.onDisconnect(EZSocket.this, e.getMessage());
            }
        }
    }

    private class Connect extends  Thread implements Runnable{
        private int timeout;

        public Connect(int timeout){
            this.timeout = timeout;
        }

        public void run(){
            try{
                if(timeout==-1) {
                    socket.connect(address);
                }
                else {
                    socket.connect(address, timeout);
                }
            } catch (IOException e){
                if(callback!=null)
                    callback.onConnectError(EZSocket.this, e.getMessage());
            }

            try {
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                if(callback!=null)
                    callback.onConnect(EZSocket.this);

                new Listen().start();
            } catch (IOException e) {
                disconnect();
                if(callback!=null)
                    callback.onDisconnect(EZSocket.this, e.getMessage());
                if(disconnect_callback!=null)
                    disconnect_callback.onDisconnect(EZSocket.this, e.getMessage());
            }
        }
    }

    public void on(String event, Listener listener){
        events.add(new Pair<>(event, listener));
    }

    public void off(String event){
        for(Pair<String, Listener> e : events){
            if(event.equals(e.first)){
                events.remove(e);
                return;
            }
        }
    }

    public void emit(String event, Object ...obj){
        try {
            Object o[] = new Object[obj.length+1];
            o[0]=event;
            System.arraycopy(obj, 0, o, 1, obj.length);

            out.writeObject(o);
        } catch (IOException e) {
            disconnect();
            if(callback!=null)
                callback.onDisconnect(EZSocket.this, e.getMessage());
            if(disconnect_callback!=null)
                disconnect_callback.onDisconnect(EZSocket.this, e.getMessage());
        }
    }

    public void setEZSocketCallback(EZSocketCallback callback){
        this.callback = callback;
    }

    public void removeEZSocketCallback(){
        this.callback = null;
    }

    public interface EZSocketCallback{
        void onConnect(EZSocket socket);
        void onDisconnect(EZSocket socket, String message);
        void onConnectError(EZSocket socket, String message);
    }

    public interface EZSocketDisconnectCallback{
        void onDisconnect(EZSocket socket, String message);
    }

    private static class Pair<Obj, Obj2>{
        public Obj first;
        public Obj2 second;

        public Pair(Obj obj, Obj2 obj2){
            this.first = obj;
            this.second = obj2;
        }
    }

    public interface Listener {
        void onCall(Object ...obj);
    }
}
