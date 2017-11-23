/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.nio.ByteBuffer;
import server.model.ClientNfo;

public class ClientController {
    private final ByteBuffer ERROR_MSG = ByteBuffer.wrap("#ERROR".getBytes()); 
    private final ByteBuffer NEW_ERROR_MSG = ByteBuffer.wrap("#GAMENOTENDED".getBytes());
    private final ByteBuffer GUESS_ERROR_MSG = ByteBuffer.wrap("#GAMENOTSTARTED".getBytes());
    
    private final ClientNfo cHandle = new ClientNfo();
    
    public ByteBuffer newWord(String newW){
        if(!cHandle.gamStarted()){
            return cHandle.newGame(newW);
        }
        return NEW_ERROR_MSG;
    }
    
    public ByteBuffer givScore(){
        return cHandle.givScore();
    }
    
    public ByteBuffer guess1(String guess) {
        if(!cHandle.gamStarted()){
            return GUESS_ERROR_MSG;
        }
        if(guess.length()==1){
            return cHandle.guessLetter(guess);
        }
        if(guess.length()>1) {
            return cHandle.guessWord(guess);
        }
        return ERROR_MSG;
    }
    
    public ByteBuffer guess2() {
        if(cHandle.gamStarted() && (cHandle.completeWord() || cHandle.zeroGuessLeft())){
            return cHandle.endGame();
        }
        return null;
    }
    
    public ByteBuffer error() {
        return ERROR_MSG;
    }
}
