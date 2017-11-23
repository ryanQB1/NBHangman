package server.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.concurrent.ForkJoinPool;
import java.util.Queue;
import server.controller.ClientController;

public class ClientHandler implements Runnable{
    
    private final MainServer server;
    private final SocketChannel clientChannel;
    private final ByteBuffer msgFromClient = ByteBuffer.allocateDirect(8192);
    private final Queue<String> recvdMessages = new ArrayDeque<>();
    private final String MSG_DELIM = "#";
    private final ClientController cContr = new ClientController();
    
    private SelectionKey clientkey;
    
    public ClientHandler(MainServer server, SocketChannel clientChannel) {
        this.clientChannel = clientChannel;
        this.server = server;
    }
    
    void sendMsg(ByteBuffer msg) throws IOException {
        clientChannel.write(msg);
        if (msg.hasRemaining()) {
            System.err.println("Unexpected large message!");
        }
    }
    
    void recvMsg(SelectionKey key) throws IOException {
        msgFromClient.clear();
        int numOfReadBytes;
        numOfReadBytes = clientChannel.read(msgFromClient);
        if (numOfReadBytes == -1) {
            throw new IOException("Client has closed connection.");
        }
        String recvdString = extractMessageFromBuffer();
        recvdMessages.add(recvdString);
        clientkey = key;
        ForkJoinPool.commonPool().execute(this);
    }
    
    private String extractMessageFromBuffer() {
        msgFromClient.flip();
        byte[] bytes = new byte[msgFromClient.remaining()];
        msgFromClient.get(bytes);
        return new String(bytes);
    }
    
    void disconnectClient() throws IOException {
        clientChannel.close();
    }
    
    @Override
    public void run() {
        String msg;
        while ((msg = recvdMessages.peek())!=null) {
            recvdMessages.remove();
            if(msg.startsWith("#")){
                switch(msg){
                    case "#NEW":
                        server.orderWrite(clientkey,cContr.newWord(server.newWord()),true);
                        break;
                    case "#SCORE":
                        server.orderWrite(clientkey,cContr.givScore(),true);
                        break;
                    case "#DISCONNNECT":
                        try{
                            server.removeClient(clientkey);
                        }catch(IOException e){
                        }
                        break;
                    default:
                        server.orderWrite(clientkey,cContr.error(),true);
                }
                continue;
            }
            String[] msg2 = msg.split(MSG_DELIM);
            if(!msg2[0].equals("GUESS")) server.orderWrite(clientkey,cContr.error(),true);
            server.orderWrite(clientkey,cContr.guess1(msg2[1]),false);
            server.orderWrite(clientkey, cContr.guess2(), true);
        }
    }
    
    
}
