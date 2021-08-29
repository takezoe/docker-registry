import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.webapp.WebAppContext;

import java.net.URL;
import java.net.InetSocketAddress;
import java.security.ProtectionDomain;

public class JettyLauncher
{
    public static void main(String[] args) throws Exception {
        String host = null;
        String port = null;
        InetSocketAddress address;
        String contextPath = null;
        String tmpDirPath = null;
        boolean forceHttps = false;

        for(String arg: args) {
            if(arg.startsWith("--") && arg.contains("=")) {
                String[] dim = arg.split("=");
                if(dim.length >= 2) {
                    switch (dim[0]) {
                        case "--host":
                            host = dim[1];
                            break;
                        case "--port":
                            port = dim[1];
                            break;
                        case "--prefix":
                            contextPath = dim[1];
                            break;
                    }
                }
            }
        }

        if (contextPath != null && !contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        if(host != null) {
            address = new InetSocketAddress(host, getPort(port));
        } else {
            address = new InetSocketAddress(getPort(port));
        }

        Server server = new Server(address);

        // Disabling Server header
        for (Connector connector : server.getConnectors()) {
            for (ConnectionFactory factory : connector.getConnectionFactories()) {
                if (factory instanceof HttpConnectionFactory) {
                    ((HttpConnectionFactory) factory).getHttpConfiguration().setSendServerVersion(false);
                }
            }
        }

        WebAppContext context = new WebAppContext();

        // Disabling the directory listing feature.
        context.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");

        ProtectionDomain domain = JettyLauncher.class.getProtectionDomain();
        URL location = domain.getCodeSource().getLocation();

        context.setContextPath(contextPath == null ? "" : contextPath);
        context.setDescriptor(location.toExternalForm() + "/WEB-INF/web.xml");
        context.setServer(server);
        context.setWar(location.toExternalForm());
        if (forceHttps) {
            context.setInitParameter("org.scalatra.ForceHttps", "true");
        }

        Handler handler = addStatisticsHandler(context);

        server.setHandler(handler);
        server.setStopAtShutdown(true);
        server.setStopTimeout(7_000);
        server.start();
        server.join();
    }

    private static int getPort(String port){
        if(port == null) {
            return 8080;
        } else {
            return Integer.parseInt(port);
        }
    }

    private static Handler addStatisticsHandler(Handler handler) {
        // The graceful shutdown is implemented via the statistics handler.
        // See the following: https://bugs.eclipse.org/bugs/show_bug.cgi?id=420142
        final StatisticsHandler statisticsHandler = new StatisticsHandler();
        statisticsHandler.setHandler(handler);
        return statisticsHandler;
    }
}
