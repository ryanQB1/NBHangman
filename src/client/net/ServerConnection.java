/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

/**
 *
 * @author Ryan
 */
public class ServerConnection implements Runnable{
    
    private static final String FATAL_COMMUNICATION_MSG = "Lost connection.";
    private static final String FATAL_DISCONNECT_MSG = "Could not disconnect, will leave ungracefully.";
    private static final String MSG_DELIM = "#";
    
    private final ByteBuffer msgFromServer = ByteBuffer.allocateDirect(8192);
    private final Queue<ByteBuffer> messagesToSend = new ArrayDeque<>();
    
    private OutputHandler listener;
    private InetSocketAddress serverAddress;
    private SocketChannel socketChannel;
    private Selector selector;
    private boolean connected;
    
    private volatile boolean timeToSend = false;
    
    public void connect(String host, int port, OutputHandler liste) {
        serverAddress = new InetSocketAddress(host, port);
        listener = liste;
        new Thread(this).start();
    }
    
    private void initSelector() throws IOException {
        selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);
    }
    
    private void initConnection() throws IOException {
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.connect(serverAddress);
        connected = true;
    }
    
    private void completeConnection(SelectionKey key) throws IOException {
        socketChannel.finishConnect();
        key.interestOps(SelectionKey.OP_READ);
        try {
            InetSocketAddress remoteAddress = (InetSocketAddress) socketChannel.getRemoteAddress();
                notifyConnectionDone(remoteAddress);
        } catch (IOException couldNotGetRemAddrUsingDefaultInstead) {
                notifyConnectionDone(serverAddress);
        }
    }
    
    public void disconnect() throws IOException {
        connected = false;
        sendMsg("#DISCONNECT");
    }
    
    private void doDisconnect() throws IOException {
        socketChannel.close();
        socketChannel.keyFor(selector).cancel();
        notifyDisconnectionDone();
    }
    
    public void makeGuess(String msg) {
        sendMsg("GUESS"+MSG_DELIM+msg);
    }
    
    public void getScore() {
        sendMsg("#SCORE");
    }
    
    public void getNewWord(){
        sendMsg("#NEW");
    }
    
    private void sendMsg(String toSend) { 
        synchronized (messagesToSend) {
            messagesToSend.add(ByteBuffer.wrap(toSend.getBytes()));
        }
        timeToSend = true;
        selector.wakeup();
    }
    
    private void sendToServer(SelectionKey key) throws IOException {
        ByteBuffer msg;
        synchronized (messagesToSend) {
            while ((msg = messagesToSend.peek()) != null) {
                socketChannel.write(msg);
                if (msg.hasRemaining()) {
                    return;
                }
                messagesToSend.remove();
            }
            key.interestOps(SelectionKey.OP_READ);
        }
    }
    
    private void recvFromServer(SelectionKey key) throws IOException {
        msgFromServer.clear();
        int numOfReadBytes = socketChannel.read(msgFromServer);
        if (numOfReadBytes == -1) {
            throw new IOException(FATAL_COMMUNICATION_MSG);
        }
        String recvdString = extractMessageFromBuffer();
        notifyMsgReceived(recvdString);
    }
    
    private String extractMessageFromBuffer() {
        msgFromServer.flip();
        byte[] bytes = new byte[msgFromServer.remaining()];
        msgFromServer.get(bytes);
        return new String(bytes);
    }
    
    private void notifyConnectionDone(InetSocketAddress connectedAddress) {
        Executor pool = ForkJoinPool.commonPool();//lambda expression used!
            pool.execute(() -> {
                listener.connected(connectedAddress);
        });
    }
    
    private void notifyDisconnectionDone() {
        Executor pool = ForkJoinPool.commonPool();
            pool.execute(() -> { //lambda expression used!
                listener.disconnected();
        });
    }
    
    private void notifyMsgReceived(String msg) {
        Executor pool = ForkJoinPool.commonPool();
            pool.execute(() -> {
                listener.handleMsg(msg);
        });
    }
    
    @Override
    public void run() {
        try {
            initConnection();
            initSelector();

            while (connected || !messagesToSend.isEmpty()) {
                if (timeToSend) {
                    socketChannel.keyFor(selector).interestOps(SelectionKey.OP_WRITE);
                    timeToSend = false;
                }

                selector.select();
                for (SelectionKey key : selector.selectedKeys()) {
                    selector.selectedKeys().remove(key);
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isConnectable()) {
                        completeConnection(key);
                    } else if (key.isReadable()) {
                        recvFromServer(key);
                    } else if (key.isWritable()) {
                        sendToServer(key);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(FATAL_COMMUNICATION_MSG);
        }
        try {
            doDisconnect();
        } catch (IOException ex) {
            System.err.println(FATAL_DISCONNECT_MSG);
        }
    }
}
