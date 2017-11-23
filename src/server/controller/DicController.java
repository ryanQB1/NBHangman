/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import server.model.Dictionary;

public class DicController {
    
    private final Dictionary dic = new Dictionary();
    
    public DicController(){
        makeDic();
    }
    
    public String newWord() {
        return dic.givWord();
    }
    
    private void makeDic() {
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader("words.txt"));
			String sCurrentLine;

			while ((sCurrentLine = reader.readLine()) != null) {
				dic.addWord(sCurrentLine);
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}finally {
			try {
				if(reader!=null) {
					reader.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
}
