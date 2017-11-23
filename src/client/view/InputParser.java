package client.view;

import java.util.Arrays;

class InputParser {
	
	private static final String PAR_DEL = " ";
	private final String EntLine;
	
	private Command cmd;
	private String[] params;
	
	InputParser(String enteredline){
		this.EntLine = enteredline;
		parseCmd(EntLine);
		parseParams(EntLine);
	}
	
	Command getCmd() {
		return cmd;
	}
	
	String getInput() {
		return EntLine;
	}
	
	String getParam(int index) {
		if(params==null) return null;
		if(index >= params.length) return null;
		return params[index];
	}
	
	int amntParams() {
		if(params==null) return 0;
		return params.length;
	}
	
	private void parseCmd(String enteredLine) {
		try {
			String[] inp = cleanup(enteredLine).split(PAR_DEL);
			cmd = Command.valueOf(inp[0].toUpperCase());
		}
		catch(Throwable cmdNotRecognised){
			cmd = Command.NO_COMM;
		}
	}
	
	private String cleanup(String src) {
		if(src == null) return src;
		return src.trim().replaceAll(PAR_DEL + "+", PAR_DEL);
	}
	
	private void parseParams(String enteredLine) {
		if(enteredLine==null) return;
		
		String[] inp = cleanup(enteredLine).split(PAR_DEL);
		params = Arrays.copyOfRange(inp, 1, inp.length);
	}
}
