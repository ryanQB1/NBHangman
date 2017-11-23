package client.net;

import java.net.InetSocketAddress;

public interface OutputHandler {
	
	public void handleMsg(String msg);
        
	public void connected(InetSocketAddress addr);
        
        public void disconnected();
}
