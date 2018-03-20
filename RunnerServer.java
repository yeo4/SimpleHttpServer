import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RunnerServer {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(8080);
		
	            System.out.println("Waiting for client on port " + 
	                    serverSocket.getLocalPort() + "...");
	            
	            while(true) {
	                 Socket server = serverSocket.accept();
	                 System.out.println("Just connected to " + server.getRemoteSocketAddress());
	                 System.out.println();
	                 
	                 //Threads
	                 Handler h = new Handler(server,true);
	                 Thread t = new Thread(h);
	                 t.run();
	                 
	                 //Without Threads
	                // Handler h = new Handler(server,false);


	            }			
	}
	
	/**
	 * 
	 * AccessDeniedException
	 * 
	 */
}
