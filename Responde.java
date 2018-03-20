import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Responde implements Runnable{
	 private String SERVER_NAME;
	 private String PATH;
	 private int HTTP_CODE;
	 private Date SentDate;
	 private Date Expires;
	 private Date Last_modified;
	 private boolean Sent;
	 public static final boolean WINDOWS_OS = (System.getProperty("os.name").contains("indows")) ? true : false;
	 private Handler Hand;
	 private String Respond = "";

	public Responde(String sERVER_NAME, String pATH,Handler Hand) {
		super();
		SERVER_NAME = sERVER_NAME;
		PATH = pATH;
		Sent = false;
		Last_modified = new Date();
		HTTP_CODE = 200;
		this.Hand = Hand;
	}
	public String getRespond() {
		return Respond;
	}
	@Override
	public void run() {
		Respond = ReturnResponde(Hand);
	}
	
	public String ReturnResponde(Handler r) {
		if(r.isServerError()) {
			HTTP_CODE = 500;
		}
		String Res = "";

		if(HTTP_CODE/100 == 2) {
			try {
            String Path = r.getPathFile();
            Path = (Path.equals("/")) ? "/index.html" : Path;
            if(WINDOWS_OS) {
            	Path = Path.replace('/', '\\');
            }
            try {
                if(Files.exists(Paths.get(PATH + Path))) {
                	 Res += (readFile(PATH + Path));
                	 Last_modified = LastModifiedTime(PATH + Path);
                }else {
                	HTTP_CODE = 404;
                	return ReturnResponde(r);
                }
            }catch (InvalidPathException e) {
            	HTTP_CODE = 403;
            	return ReturnResponde(r);
			}

			}catch(Exception e) {
				HTTP_CODE = 500;
			}
		}else {
			try {
				/*if(Files.exists(Paths.get(PATH+"\\"+HTTP_CODE+".html"))) {
				Res = "<!DOCTYPE html>\r\n" + 
						"<html>\r\n" + 
						"<head> \r\n" + 
						"<meta http-equiv=\"refresh\" content=\"0; URL=" +  r.getServer().getLocalSocketAddress().toString().substring(1)  + "\\" + HTTP_CODE+".html"+" \" />\r\n" +
						"</head>\r\n" + 
						"</html>";
				System.out.println("<meta http-equiv=\"refresh\" content=\"0; URL=" +  r.getServer().getLocalSocketAddress().toString().substring(1)  + "\\" + HTTP_CODE+".html"+" \" />\r\n");
			}else {
				throw new Exception("File NOT Found 404");
			}*/
					Res = readFile(PATH+"\\"+HTTP_CODE+".html");
				}catch(Exception e) {
					Res = "<!DOCTYPE html>\r\n" + 
							"<html>\r\n" + 
							"<body>\r\n" + 
							"\r\n" + 
							"<h1>"+HTTP_CODE+"</h1>\r\n" + 
							"\r\n" + 
							"</body>\r\n" + 
							"</html>";
				}
		}
		SentDate = new Date();
		Expires = new Date(SentDate.getTime() + 60*60*60*5);
		Res = _ReturnResponde(Res);
		
		Sent = true;	
		return Res;
	}
	private String _ReturnResponde(String res) {
		String t = "";
        t +=(("HTTP/1.1 "+HTTP_CODE+"\r\n"));
        t +=(("Date: "+SentDate.toGMTString() +"\r\n"));
        t +=(("Server: "+SERVER_NAME+"\r\n"));
        t +=(("Content-Type: text/html\r\n"));
        t +=(("Content-Length: "+t.length()+"\r\n"));
        t +=(("Expires: "+Expires.toGMTString()+"\r\n"));
        t +=(("Last-modified: "+SentDate.toGMTString()+"\r\n"));
        t += (("\r\n\r\n"));
        t += res;
		return t;
	}
	public int getHTTP_CODE() {
		return HTTP_CODE;
	}

	public void setHTTP_CODE(int hTTP_CODE) {
		HTTP_CODE = hTTP_CODE;
	}
	private Date LastModifiedTime(String Path) throws IOException {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Files.getLastModifiedTime(Paths.get(Path)).toMillis());
		
		int mYear = calendar.get(Calendar.YEAR);
		int mMonth = calendar.get(Calendar.MONTH);
		int mDay = calendar.get(Calendar.DAY_OF_MONTH);
		int mHour = calendar.get(Calendar.HOUR);
		int mMin = calendar.get(Calendar.MINUTE);

		Date d = new Date(mYear,mMonth,mDay,mHour,mMin);
		
		return d;
	}
	private String readFile(String path) throws IOException {
		  List<String> encoded = Files.readAllLines(Paths.get(path));
		  String s = "";
		  for(int i=0;i<encoded.size();i++) {
			  s += encoded.get(i);
		  }
		  return s;
	}

	@Override
	public String toString() {
		return "Responde [SERVER_NAME=" + SERVER_NAME + ", PATH=" + PATH + ", HTTP_CODE=" + HTTP_CODE + ", SentDate="
				+ SentDate + ", Expires=" + Expires + ", Last_modified=" + Last_modified + ", Sent=" + Sent + "]";
	}

}
