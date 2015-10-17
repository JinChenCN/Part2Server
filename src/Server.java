import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class Server {
	static Integer port = 8085;
	static String filePath = "";
	static String segmentPath = "";
	static File[] listOfFiles = null;
	static List<String> CachedSegementList = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {  		
		getProperties(args[0]);
		File folder = new File(filePath);
    	listOfFiles = folder.listFiles();
    	
		// Initiate saved segments
		File segfolder = new File(segmentPath);
		for(File seg : segfolder.listFiles()) {
			 seg.delete();               
	        }	
    	
	    // Create a new Component.  
	    Component component = new Component(); 

	    // Add a new HTTP server listening on port configured, the default port is 8183.  
	    component.getServers().add(Protocol.HTTP, port);  

	    // Attach the application.  
	    component.getDefaultHost().attach("/711P2",  
	            new APISever());  

	    // Start the component.  
	    component.start();		    	   
	} 
	
	private static void getProperties(String configFilePath){
		Properties configFile = new Properties();
		FileInputStream file;
		try {
			file = new FileInputStream(configFilePath);
			configFile.load(file);
			file.close();
			port = Integer.parseInt(configFile.getProperty("ServerPort"));
			filePath = configFile.getProperty("SeverFilePath");
			segmentPath = configFile.getProperty("SeverSegPath");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
