package hello;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

public class ServletContext {

    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setConfigurations(new Configuration[]{ new SpringAppInitializingConfiguration()});

        server.setHandler(webAppContext);
        server.start();
        server.join();
    }
    
}
