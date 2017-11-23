package client.view;


//Needed because multiple threads might want to put out at the same time
class ThreadSafeStdOut {
	
	synchronized void print(String output) {
		System.out.print(output);
	}
	
	synchronized void println(String output) {
		System.out.println(output);
	}
}
