package server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Dictionary {
	private final List<String> words = Collections.synchronizedList(new ArrayList<>());
	
	public void addWord(String word) {
		words.add(word);
	}
	
	public String givWord() {
		Random randomizer = new Random();
		return words.get(randomizer.nextInt(words.size()));
	}
}
