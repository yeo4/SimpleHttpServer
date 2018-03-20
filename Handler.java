import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Handler implements Runnable{
	
	private static int _id = 0;
	final String SERVER_NAME = "Yeo4-NetServer";
	final String FULL_PATH = "www";
	private int id;
	private String PathFile;
	private String HttpTypeReq;
    private HashMap<String,String> GETParams;
    private HashMap<String,String> POSTParams;
    private ArrayList<String> X_RequestParmats = new ArrayList<>();
    private HashMap<String,String> RequestParmats = new HashMap<>();
    private Socket server;
    private BufferedReader FullRequest;
    private BufferedWriter out;
    private boolean ServerError;
    private boolean ServerThread;
    
    public Handler(Socket serve,boolean ServerThread) {
    	server = serve;
    	ServerError = false;
    	this.ServerThread = ServerThread;
    	if(!ServerThread) {
    		run();
    	}
    }
	public boolean isServerError() {
		return ServerError;
	}
	@Override
	public void run() {
        try {
			FullRequest = new BufferedReader(new InputStreamReader(server.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
		} catch (IOException e1) {
			ServerError = true;
			e1.printStackTrace();
			return;
		}
        String R = "";
        if(ServerThread) {
        	Thread t1 = new Thread() {
        	    public void run() {
        	    	RequestHandler();
        	    }
			};
        	t1.setName("Run Req");
	    	t1.run();
	    	
	    	Responde Res = new Responde(SERVER_NAME, FULL_PATH,this);
        	Thread t2 = new Thread(Res);
        	try {
				t1.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	t2.run();
	    	R = Res.getRespond();
        }else {
	    	RequestHandler();
	    	Responde Res = new Responde(SERVER_NAME, FULL_PATH,this);
	    	R = Res.ReturnResponde(this);
        }
    	try {
			out.write(R);
	    	out.flush();
	    	System.err.println("SENT");
	    	FullRequest.close();
	        out.close();
	        server.close();
		} catch (IOException e) {
			ServerError = true;
			e.printStackTrace();
		}

	}
    int contentLength = 0;
	private Runnable RequestHandler(){
    	if(_id == 0) {
    		SetUp();
    	}
    	boolean HttpTypeReqBool = false;
        while (true) {
      		StringBuilder body = new StringBuilder();
      		Character c;
      		
      		//ReadLine:
        	while(true) {
                 try {
					c = ((char)FullRequest.read());
				} catch (IOException e) {
					ServerError = true;
					e.printStackTrace();
					return null;
				}
                 if(c.equals('\n')) {
                	 break;
                 }
                 body.append(c);
        	}
            final String s = body.toString().substring(0,body.toString().length() - 1); //Delete \r
            
            //Stop
            if(s.length() == 0) {
            	if(contentLength > 0) {
            	     body = new StringBuilder();
    	             c = 0;
    	             for (int i = 0; i < contentLength; i++) {
    	                 try {
    	 					c = ((char)FullRequest.read());
    	 				} catch (IOException e) {
    	 					ServerError = true;
    	 					e.printStackTrace();
    	 					return null;
    	 				}
    	                 body.append(c);
    	             }
    	             POSTParams = AnlyzeParams(body.toString(), new HashMap<>());
            	}
            	break;
            }
            
            //Method Req
	       	 if(!HttpTypeReqBool && (s.toLowerCase().startsWith("get") || s.toLowerCase().startsWith("post"))) {
	       		HttpTypeReqBool = true;
	       		HttpTypeReq = s.toUpperCase().substring(0, s.indexOf(' '));
	         	int index = s.indexOf('?');
	    		 if(index > -1 && (index != s.length())) {
	    			 GETParams = AnlyzeParams(s.substring(index + 1,s.toUpperCase().lastIndexOf("HTTP")-1), new HashMap<>());
	    			 PathFile = s.substring(s.indexOf(' ')+1, index);
	    		 }else {
	    			 PathFile = s.substring(s.indexOf(' ')+1,s.toUpperCase().lastIndexOf("HTTP")-1);
	    		 }
	       	 }else if(s.toLowerCase().startsWith("content-length: ")){
	       		 contentLength = Integer.parseInt(s.toLowerCase().substring(("content-length: ").length()));
	       	 }
	       	 else { 	 
	       		 for(int i=0;i<X_RequestParmats.size();i++) {
		       		 if(s.toLowerCase().startsWith(X_RequestParmats.get(i))) {
		             	RequestParmats.put(X_RequestParmats.get(i), s.substring(X_RequestParmats.get(i).length()));
		       			X_RequestParmats.remove(X_RequestParmats.get(i));
	
		       		 }
		       	 }
	       	 }
	       }
	    	++_id;
	    	id = _id;
			return null;
    }
    private void SetUp() {
    	X_RequestParmats.add("user-agent: ");
	}
    
    private HashMap<String,String> AnlyzeParams(String s, HashMap<String,String> Params){
    	while(s.length() != 0) {
    		int AndIndex = s.indexOf('&');
        	int EqualIndex = s.indexOf('=');
        	if((AndIndex > -1) && (EqualIndex < AndIndex)) {
        		Params.put(s.substring(0,EqualIndex), s.substring(EqualIndex+1,AndIndex));
        		s = s.substring(AndIndex + 1);
        	}else {
        		if(EqualIndex < 0) {
        			EqualIndex = s.length();
            		Params.put(s, "");
        		}else {
            		Params.put(s.substring(0,EqualIndex), s.substring(EqualIndex+1));
        		}
        		s = "";
        	}
    	}
		return Params;    	
    }
    
    public String getHttpTypeReq() {
		return HttpTypeReq;
	}
	public HashMap<String, String> getRequestParmats() {
		return RequestParmats;
	}
	public int getId() {
		return id;
	}
	public String getPathFile() {
		return PathFile;
	}
	public HashMap<String, String> getGETParams() {
		return GETParams;
	}
	public HashMap<String, String> getPOSTParams() {
		return POSTParams;
	}
	public Socket getServer() {
		return server;
	}
	
	@Override
	public String toString() {
		return "Request [id=" + id + ", PathFile=" + PathFile + ", HttpTypeReq=" + HttpTypeReq + ", GETParams="
				+ GETParams + ", POSTParams=" + POSTParams + ", X_RequestParmats=" + X_RequestParmats
				+ ", RequestParmats=" + RequestParmats + "]";
	}
}
