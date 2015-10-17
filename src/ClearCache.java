import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ClearCache extends ServerResource{
	@Get
    public StringRepresentation clearResource() {
		StringRepresentation result = null;
		Server.CachedSegementList.clear();
		result = new StringRepresentation("OK");
		return result;	
	}
}
