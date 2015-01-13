package de.dariah.schereg.view.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingAppletRequestWrapper extends HttpServletRequestWrapper {

	private static final Logger logger = LoggerFactory.getLogger(MappingAppletRequestWrapper.class);

    private final String cachedFunctionName;
    private final Object[] cachedFunctionArgs;
    
    public String getCachedFunctionName() { return cachedFunctionName; }
	public Object[] getCachedFunctionArgs() { return cachedFunctionArgs; }

	public MappingAppletRequestWrapper(HttpServletRequest request) {
        super(request);
        
        String functionName = null;
        Object[] functionArgs = null;
        
        try {
			ObjectInputStream in = new ObjectInputStream(request.getInputStream());
			
			// Unpack function name and arguments
			functionName = (String)in.readObject();
			functionArgs = new Object[(Integer)in.readObject()];
			for(int i=0; i<functionArgs.length; i++)
				functionArgs[i] = in.readObject();
			in.close();
        } catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			this.cachedFunctionName = functionName;
			this.cachedFunctionArgs = functionArgs;
		}
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
		
		ObjectOutputStream out = new ObjectOutputStream(bas);
		out.writeObject(this.cachedFunctionName);
		out.writeObject(this.cachedFunctionArgs.length);
		for(Object input : this.cachedFunctionArgs) out.writeObject(input);
		out.flush();
		out.close();
        
		final ByteArrayInputStream ba = new ByteArrayInputStream(bas.toByteArray());
        
        ServletInputStream inputStream = new ServletInputStream() {
            public int read() throws IOException {
                return ba.read();
            }
        };
        return inputStream;
    }
}
