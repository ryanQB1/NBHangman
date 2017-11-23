package client.view;

public enum Command {
	
	//Connects to the server with params IP and Port
	CONNECT,
	
	//Attempt to quit the application
	QUIT,
	
	//Guess a word or a letter params: A string
	GUESS,
	
	//Request the score
	SCORE,
	
	//New game!
	NEW,
	
	//No Command or wrong Command was given so nothing will happen
	NO_COMM;
}
