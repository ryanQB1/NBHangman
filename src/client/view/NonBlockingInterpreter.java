package client.view;

import client.net.OutputHandler;
import client.net.ServerConnection;
import java.io.IOException;

import java.util.Scanner;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 *
 * @author Ryan
 */
public class NonBlockingInterpreter implements Runnable{
	
    private static final String PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private final ThreadSafeStdOut outMgr = new ThreadSafeStdOut();
    
    private boolean receivingCmds = false;
    private ServerConnection server;
    private boolean connected;
    
    public void start() {
        if (receivingCmds) {
            return;
        }
        receivingCmds = true;
        server = new ServerConnection();
        new Thread(this).start();
    }
    
    @Override
    public void run() {
        while (receivingCmds) {
            try {
                InputParser cmdLine = new InputParser(readNextLine());
                switch (cmdLine.getCmd()) {
                    case QUIT:
                        if(connected){
                            receivingCmds = false;
                            server.disconnect();
                        }else{
                            receivingCmds = false;
                        }
                        break;
                    case CONNECT:
                        if(!connected){
                        server.connect(cmdLine.getParam(0),
                                      Integer.parseInt(cmdLine.getParam(1)),
                                      new ConsoleOutput());
                        }else{
                            outMgr.println(PROMPT + "You are already connected (hopefully)");
                        }
                        break;
                    case GUESS:
                        if(connected){
                            server.makeGuess(cmdLine.getParam(0));
                        }else{
                            outMgr.println(PROMPT + "Connect to the server first!");
                        }
                        break;
                    case SCORE:
                        if(connected){
                            server.getScore();
                        }else{
                            outMgr.println(PROMPT+"Connect to the server first!");
                        }
                    	break;
                    case NEW:
                        if(connected){
                            server.getNewWord();
                        }else{
                            outMgr.println(PROMPT+"Connect to the server first!");
                        }
                    	break;
                    case NO_COMM:
                        outMgr.println(PROMPT + "Invalid Command");
                        break;
                }
            } catch (IOException | NumberFormatException e) {
                outMgr.println("Operation failed");
            }
        }
    }
    
    private String readNextLine() {
        outMgr.print(PROMPT);
        return console.nextLine();
    }
    
    private class ConsoleOutput implements OutputHandler {
    	private final Queue<String> messages = new ArrayDeque<>();
        
        private void stdPrint(String msg) {
            outMgr.println(msg);
            outMgr.print(PROMPT);
        }
        
        private void msgSplitter(String msg){
            String[] mess = msg.split("%");
            for(String ff : mess){
                if(!ff.isEmpty()){messages.add(ff);}
            }
        }
        
        @Override
        public void handleMsg(String msg){
            msgSplitter(msg);
            while((msg=messages.peek())!=null){
            messages.remove();
            if(msg.startsWith("#")){
                switch (msg) {
                    case "#ERROR":
                        stdPrint("Oops, a unknown error occured");
                        break;
                    case "#GAMENOTSTARTED":
                        stdPrint("Start the game first with 'new' please");
                        break;
                    case "#NULL":
                        stdPrint("Can't make an empty guess!");
                        break;
                    case "#GAMENOTENDED":
                        stdPrint("The game has not ended yet");
                        break;
                    default:
                        stdPrint("An unknown error occured");
                        break;
                }
                return;
            }
            String[] prepMsg = msg.split("#");
            switch(prepMsg[0]){
                case "END":
                    String finMsg = "";
                    if(prepMsg[1].equals("L")) finMsg = "Aww, you lost!";
                    if(prepMsg[1].equals("W")) finMsg = "Congratz, you won!";
                    stdPrint(finMsg + " The word was " + prepMsg[2]);
                    break;
                case "WORD":
                    stdPrint("Your guess was " + prepMsg[1] + " Word: " + prepMsg[2] + " You have " + prepMsg[3] + " tries left");
                    break;
                case "NEW":
                    stdPrint("Game Started:" + prepMsg[1] + "(" + prepMsg[1].length() + " letters)");
                    break;
                case "SCORE":
                    stdPrint("You have played " + prepMsg[1] + " games and you have won " + prepMsg[2] + " times");
                    break;
                default:
                    stdPrint("Unexpected Message recieved from server");
            }
            }
        }
        
        @Override
        public void connected(InetSocketAddress addr){
            stdPrint("Client has been connected to: " + addr.getHostName() + ":" + addr.getPort());
            connected = true;
        }
        
        @Override
        public void disconnected() {
            stdPrint("Client has been disconnected");
        }
    }
}