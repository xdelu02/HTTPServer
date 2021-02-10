package server;

import Resources.PuntiVendita;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.io.*;
import java.net.*;
import java.util.*;
import SQL.*;
import Resources.*;

public class JavaHTTPServer implements Runnable{ 	
	static final File WEB_ROOT = new File("./files");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
        static final String FILE_REDIRECT = "301.html";
        static final String DB = "HTTPServer";
	static final int PORT = 3000;
	
	static final boolean VERBOSE = true;
	private String fileString;
	private Socket connect;           
        private ArrayList<Persona> persone;
        private JavaMySQL sql = new JavaMySQL();
        
	public JavaHTTPServer(Socket c) {
            connect = c;
	}
	
	public static void main(String[] args) {
            try {
                    ServerSocket serverConnect = new ServerSocket(PORT);
                    System.out.println("Server started.\n Listening for connections on port : " + PORT + " ...\n");

                    while (true) {
                        JavaHTTPServer myServer = new JavaHTTPServer(serverConnect.accept());

                        if (VERBOSE) {
                            System.out.println("Connecton opened. (" + new Date() + ")");
                        }

                        Thread thread = new Thread(myServer);
                        thread.start();
                    }

            } catch (IOException e) {
                System.err.println("Server Connection error : " + e.getMessage());
            }
	}

	@Override
	public void run() {
            BufferedReader in = null; 
            PrintWriter out = null; 
            BufferedOutputStream dataOut = null;
            String fileRequested = null;

            try {
                // we read characters from the client via input stream on the socket
                in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
                // we get character output stream to client (for headers)
                out = new PrintWriter(connect.getOutputStream());
                // get binary output stream to client (for requested data)
                dataOut = new BufferedOutputStream(connect.getOutputStream());

                // get first line of the request from the client
                String input = in.readLine();
                // we parse the request with a string tokenizer
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
                // we get file requested
                fileRequested = parse.nextToken();                  
                persone = sql.getResult();
                
                // we support only GET and HEAD methods, we check
                if (!method.equals("GET")  &&  !method.equals("HEAD")) {
                    if (VERBOSE) {
                        System.out.println("501 Not Implemented : " + method + " method.");
                    }

                    // we return the not supported file to the client
                    File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                    int fileLength = (int) file.length();
                    String contentMimeType = "text/html";
                    //read content to return to client
                    byte[] fileData = readFileData(file, fileLength);

                    // we send HTTP Headers with data to client
                    out.println("HTTP/1.1 501 Not Implemented");
                    out.println("Server: Java HTTP Server from SSaurel : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + contentMimeType);
                    out.println("Content-length: " + fileLength);
                    out.println(); // blank line between headers and content, very important !
                    out.flush(); // flush character output stream buffer
                    // file
                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();

                } else {
                    // GET or HEAD method
                    if (fileRequested.endsWith("/")) {
                        fileRequested += DEFAULT_FILE;
                    }else if(fileRequested.equals("/puntivendita.xml")){
                        ObjectMapper objMapper = new ObjectMapper(); 
                        PuntiVendita pv = objMapper.readValue(new File(WEB_ROOT + "/puntivendita.json"), PuntiVendita.class); //deserialize json to java
                        XmlMapper xmlMapper = new XmlMapper();
                        fileString = xmlMapper.writeValueAsString(pv);
                    }else if(fileRequested.equals("/" + DB + ".json")) {                  
                        ObjectMapper objMapper = new ObjectMapper();
                        fileString = objMapper.writeValueAsString(persone);
                        System.out.println("Serializzato");
                    }else if(fileRequested.equals("/" + DB + ".xml")) {                     
                        XmlMapper xmlMapper = new XmlMapper();
                        fileString = xmlMapper.writeValueAsString(persone);
                        System.out.println("Serializzato");
                    }
                    
                    int fileLength = fileString.length();
                    String content = getContentType(fileRequested);

                    if (method.equals("GET")) { // GET method so we return content
                        // send HTTP Headers
                        out.println("HTTP/1.1 200 OK");
                        out.println("Server: Java HTTP Server from SSaurel : 1.0");
                        out.println("Date: " + new Date());
                        out.println("Content-type: " + content);
                        out.println("Content-length: " + fileLength);
                        out.println("Cache-Control: " + "no-store");
                        out.println(); // blank line between headers and content, very important !
                        out.flush(); // flush character output stream buffer

                        dataOut.write(fileString.getBytes(), 0, fileLength);
                        dataOut.flush();
                    }

                    if (VERBOSE) {
                        System.out.println("File " + fileRequested + " of type " + content + " returned");
                    }

                }

        } catch (FileNotFoundException fnfe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception : " + ioe.getMessage());
            }

        } catch (IOException ioe) {
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                connect.close(); // we close socket connection
            } catch (Exception e) {
                System.err.println("Error closing stream : " + e.getMessage());
            } 

            if (VERBOSE) {
                System.out.println("Connection closed.\n");
            }
            }			
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
            FileInputStream fileIn = null;
            byte[] fileData = new byte[fileLength];

            try {
                fileIn = new FileInputStream(file);
                fileIn.read(fileData);
            } finally {
                if (fileIn != null) fileIn.close();
            }

            return fileData;
	}
	
	// return supported MIME Types
	private String getContentType(String fileRequested) {
            if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html")) return "text/html";
            else if (fileRequested.endsWith(".xml")) return "text/xml";
            else return "text/plain";
	}
	
        //error 404
	private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
            String content = "text/html";
            
            if(fileRequested.endsWith(".html")) {
                File file = new File(WEB_ROOT, FILE_NOT_FOUND);
                int fileLength = (int) file.length();               
                byte[] fileData = readFileData(file, fileLength);
            
                out.println("HTTP/1.1 404 File Not Found");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + content);
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer

                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

                if (VERBOSE) {
                    System.out.println("File " + fileRequested + " not found");
                }
            }else {              
                File file = new File(WEB_ROOT, FILE_REDIRECT);                               
                int fileLength = (int) file.length();        
                byte[] fileData = readFileData(file, fileLength);
                
                out.println("HTTP/1.1 301 Page Not Found");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + content);
                out.println("Content-length: " + fileLength);
                out.println("Location: "+fileRequested + "/");
                out.println(); // blank line between headers and content, very important !
                out.flush(); // flush character output stream buffer
                
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
                
                if (VERBOSE) {
                    System.out.println("Page " + fileRequested + " redirect");
                }
            }                      
	}	
}