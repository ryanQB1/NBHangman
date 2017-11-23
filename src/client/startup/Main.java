package client.startup;

import client.view.NonBlockingInterpreter;
/**
 *
 * @author Ryan
 */
public class Main {
    
    public static void main(String args[]){
        new NonBlockingInterpreter().start();
    }
    
}
