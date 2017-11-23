/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.nio.ByteBuffer;

public class ClientNfo {
    
    private final String MSG_DELIM = "#";
    private final String SPEC_DELIM = "%";
    
    private String chosenWord;
    private char[] currentWord;
    private int guessesLeft = 0;
    private boolean gameStarted = false;
    private int wins = 0;
    private int totGames = 0;
    
    public ByteBuffer newGame(String newW) {
        String toSend;
	if(guessesLeft!=0) toSend = "#ERROR";
        else{
            chosenWord = newW.toLowerCase();
            guessesLeft = chosenWord.length();
            currentWord = new char[chosenWord.length()];
            for(int a = 0; a < chosenWord.length() ; a++) {
            	currentWord[a] = '-';
            }
            gameStarted = true;
            toSend = new String(currentWord);
        }
        toSend = SPEC_DELIM + "NEW" + MSG_DELIM + toSend;
        return ByteBuffer.wrap(toSend.getBytes());
    }
        
    public ByteBuffer givScore() {
		return ByteBuffer.wrap((SPEC_DELIM + "SCORE" + MSG_DELIM + totGames + MSG_DELIM + wins).getBytes());
	}
        
    public boolean completeWord() {
                if(currentWord==null) return false;
		for(char f : currentWord) {
			if(f=='-') return false;
		}
		return true;
	}
        
    public ByteBuffer guessWord(String guessw) {
            String toSend;
            if(guessw.equals(chosenWord)) {
            	currentWord = chosenWord.toCharArray();
            	toSend =  "CORRECT" + MSG_DELIM + (chosenWord) + MSG_DELIM + guessesLeft;
            }else{
                guessesLeft--;
                toSend = "INCORRECT" + MSG_DELIM + (new String(currentWord)) + MSG_DELIM + guessesLeft;
            }
            toSend = SPEC_DELIM + "WORD" + MSG_DELIM + toSend;
            return ByteBuffer.wrap(toSend.getBytes());
	}
        
    public ByteBuffer guessLetter(String letter) {
            String toSend;
            boolean corr = false;
            char[] chosenWord1 = chosenWord.toCharArray();
            char lett = (letter.toLowerCase()).charAt(0);
            for(int i = 0; i < chosenWord1.length ; i++) {
            	if(chosenWord1[i]==lett) {
            		currentWord[i]=lett;
            		corr = true;
            	}
            }
            if(corr){
                toSend = "CORRECT" + MSG_DELIM + ( new String(currentWord)) + MSG_DELIM + guessesLeft;
            }
            else{
                guessesLeft--;
                toSend = "INCORRECT" + MSG_DELIM + (new String(currentWord)) + MSG_DELIM + guessesLeft;
            }
            toSend = SPEC_DELIM + "WORD" + MSG_DELIM + toSend;
            return ByteBuffer.wrap(toSend.getBytes());
	}
        
    public ByteBuffer endGame() {
		String toSend;
		totGames++;
		if(completeWord()) {
                    wins++;
                    toSend="W" + MSG_DELIM + chosenWord;
		}
		else {
                    toSend="L" + MSG_DELIM + chosenWord;
		}
		chosenWord = null;
		currentWord = null;
		guessesLeft = 0;
		gameStarted = false;
                toSend = SPEC_DELIM + "END" + MSG_DELIM + toSend;
                return ByteBuffer.wrap(toSend.getBytes());
	}
    
    public boolean gamStarted() {
        return gameStarted;
    }
    
    public boolean zeroGuessLeft() {
        return guessesLeft==0;
    }
}
