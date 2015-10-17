
import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class FileList extends ServerResource{
	@Get
    public JsonRepresentation getResource() throws JSONException {	
		JSONArray list =  new JSONArray();
		File[] files= Server.listOfFiles;
		for(File file : files)
		{
			list.put(file.getName());
		}		
		return new JsonRepresentation(list);		
    }
}

