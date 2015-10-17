import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class Document extends ServerResource {
	String segmentName = "";
	int hconst = 69069; // good hash multiplier for MOD 2^32
	int mult = 1; // this will hold the p^n value
	int[] buffer; // circular buffer - reading from file stream
	int buffptr = 0;
	InputStream is;
	FileOutputStream out = null;
	List<String> segments = new ArrayList<String>();
	
	@Get
    public JsonRepresentation getResource() throws IOException, JSONException {

		int mask = 1 << 13;
		mask--; // 13 bit of '1's
		
		JSONArray list =  new JSONArray();
		String fileName = "";
		
		//		
		Request request = getRequest();
		Form form = request.getResourceRef().getQueryAsForm();		
		
		if(form.getValues("fileName") != null)
		{
			fileName += form.getValues("fileName");
		}

		File f = new File(Server.filePath + "//" + fileName);
		FileInputStream fs;
		try {
			fs = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fs); 
			// BufferedInputStream is faster to read byte-by-byte from
			is = bis;

			long length = bis.available();
			long curr = length;
			
			// get the initial 1k hash window //
			int hash = inithash(1024); 
			String segNameInit = Integer.toString(hash);
			out.close();
			if(!segments.contains(segNameInit))
			{
				File temp = new File(Server.segmentPath + "//out.txt");
				File segmentInit = new File(Server.segmentPath + "//" + segNameInit + ".txt");
				temp.renameTo(segmentInit);
				segments.add(segNameInit);										
			}
			JSONObject segmentJson = new JSONObject() ; 
			segmentJson.put("name", segNameInit+".txt");
			if(!Server.CachedSegementList.contains(segNameInit))
			{
				segmentJson.put("content", Files.readAllBytes(new File(Server.segmentPath + "//" + segNameInit + ".txt").toPath()));
				Server.CachedSegementList.add(segNameInit);	
			}
			else
			{
				segmentJson.put("content", "");
			}
			list.put(segmentJson);
			
			out = new FileOutputStream(Server.segmentPath + "//out.txt");
			////////////////////////////////////
			
			curr -= bis.available();
							
			while (curr < length) {
			if ((hash & mask) == 0) {
				String segName = Integer.toString(hash);
				out.close();
				if(!segments.contains(segName))
				{
					File temp = new File(Server.segmentPath + "//out.txt");
					File segment = new File(Server.segmentPath + "//" + segName + ".txt");
					temp.renameTo(segment);
					segments.add(segName);										
				}
				JSONObject segment = new JSONObject() ; 
				segment.put("name", segName+".txt");
				if(!Server.CachedSegementList.contains(segName))
				{
					segment.put("content", Files.readAllBytes(new File(Server.segmentPath + "//" + segName + ".txt").toPath()));
					Server.CachedSegementList.add(segName);	
				}
				else
				{
					segment.put("content", "");
				}
				list.put(segment);
				

				out = new FileOutputStream(Server.segmentPath + "//out.txt");
			}
			// next window's hash  //
			hash = nexthash(hash);
			/////////////////////////
			curr++;
			}	
			
			if(curr == length)
			{
				String segName = Integer.toString(hash);
				out.close();
				if(!segments.contains(segName))
				{
					File temp = new File(Server.segmentPath + "//out.txt");
					File segment = new File(Server.segmentPath + "//" + segName + ".txt");
					temp.renameTo(segment);
					segments.add(segName);										
				}
			
				JSONObject segment = new JSONObject() ; 
				segment.put("name", segName+".txt");
				if(!Server.CachedSegementList.contains(segName))
				{
					segment.put("content", Files.readAllBytes(new File(Server.segmentPath + "//" + segName + ".txt").toPath()));
					Server.CachedSegementList.add(segName);	
				}
				else
				{
					segment.put("content", "");
				}
				list.put(segment);
			}
			
			fs.close();
		} catch (Exception e) {
			}
		return new JsonRepresentation(list);
		}
	
	private int nexthash(int prevhash) throws IOException {
		int c = is.read(); // next byte from stream
		out.write(c);
		
		prevhash -= mult * buffer[buffptr]; // remove the last value
		prevhash *= hconst; // multiply the whole chain with prime
		prevhash += c; // add the new value
		buffer[buffptr] = c; // circular buffer, 1st pos == lastpos
		buffptr++;
		buffptr = buffptr % buffer.length;
		
		return prevhash;
	}
	
	private int inithash(int length) throws IOException {
		return inithash(length - 1, 0);
	}
	
	private int inithash(int from, int to) throws IOException {
		buffer = new int[from - to + 1]; // create circular buffer
		out = new FileOutputStream(Server.segmentPath + "//out.txt");
		
		int hash = 0;
		
		int cnt = to;
		while (cnt != 0) { // skip first characters if needed
			int c = is.read();
			out.write(c);
			if (c == -1)
				throw new IOException();
			cnt--;
		}
	
	// calculate the hash sum of p^n * a[x]
	for (int i = 0; i <= from - to; i++) { 
		int c = is.read();
		out.write(c);
		if (c == -1) // file is shorter than the required window size		
			break;	
	
		// store byte so we can remove it from the hash later
		buffer[buffptr] = c; 
		buffptr++;
		buffptr = buffptr % buffer.length;
		
		hash *= hconst; // multiply the current hash with constant
		
		hash += c; // add byte to hash
	
		if(i>0) // calculate the large p^n value for later usage
		mult *= hconst;
		}
		
		return hash;
	}	
	
}
