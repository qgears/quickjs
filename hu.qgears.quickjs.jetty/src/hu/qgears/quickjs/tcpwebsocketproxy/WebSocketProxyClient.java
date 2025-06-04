package hu.qgears.quickjs.tcpwebsocketproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.Fields.Field;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import hu.qgears.commons.UtilEventQueue;
import hu.qgears.commons.UtilListenableProperty;
import joptsimple.tool.AbstractTool.IArgs;

/**
 * Client of Web Socket proxying.
 */
public class WebSocketProxyClient {
	public static final String proxyPort = "proxyPort";

	public static class Args implements IArgs {
		public String host;
		public int port;
		public String path;
		public String data;
		public boolean ssl;

		public String proxyHost;
		public int proxyPort;
		public File tcpdumpLogFolder;

		public String loginPath;
		public String user;
		public String pass;

		@Override
		public void validate() {
			// TODO Auto-generated method stub

		}

		public void setUri(String uri) {
			HttpURI parsed = HttpURI.build(uri);
			switch (parsed.getScheme().toLowerCase()) {
			case "https":
			case "wss":
				ssl = true;
				port = 443;
				break;
			case "ws":
			case "http":
				ssl = false;
				port = 80;
				break;
			}
			int parsedPort = parsed.getPort();
			if (parsedPort > 0) {
				port = parsedPort;
			}
			host = parsed.getHost();
			path = parsed.getPath();
		}

		public String getPortPostfix() {
			if ((ssl && port != 443) || (!ssl && port != 80)) {
				return ":" + port;
			}
			return "";
		}
	}

	private int nConnections;
	public final UtilListenableProperty<String> status = new UtilListenableProperty<>("Initializing...");
	public UtilListenableProperty<Boolean> exit = new UtilListenableProperty<>(false);

	public void run(Args args) throws Exception {
		if (args.tcpdumpLogFolder != null) {
			args.tcpdumpLogFolder.mkdirs();
		}
		WebSocketClient cli = null;
		try {
			cli = new WebSocketClient();
			cli.start();
			status.setProperty("Intializing 2 ...");
			// Set up cookie
			HttpClient httpc = cli.getHttpClient();
			HttpCookie cookie = new HttpCookie("cookie-consent", "accepted");
			cookie.setPath("/");
			cookie.setSecure(args.ssl);
			String basicUri = (args.ssl ? "https" : "http") + "://" + args.host + args.getPortPostfix();
			httpc.getCookieStore().add(new URI(basicUri + "/"), cookie);
			if (args.loginPath != null && args.loginPath.length() > 0) {
				URI uri = new URI(
						(args.ssl ? "https" : "http") + "://" + args.host + args.getPortPostfix() + args.loginPath);

				Field user = new Field("user", args.user);
				Field pass = new Field("pass", args.pass);
				Fields fields = new Fields();
				fields.put(user);
				fields.put(pass);

				ContentResponse cr = httpc.FORM(uri, fields);
				String resp = cr.getContentAsString();
				if (resp.indexOf("Not logged in.") > 0) {
					System.out.println("LOG IN REJECTED");
					status.setProperty("LOG IN REJECTED");
					Thread.sleep(2000);
				} else {
					status.setProperty("Logged in successfully...");
				}
			}
			WebSocketProxy wsp = new WebSocketProxy("client", new WebSocketProxyListener() {
				@Override
				public void serverPortOpened(WebSocketProxyServerSocket webSocketProxyServerSocket) {
					// TODO Auto-generated method stub

				}

				@Override
				public void connected(WebSocketProxyConnectedSocket webSocketProxyConnectedSocket) {
					nConnections++;
					status.setProperty("Connections opened: " + nConnections);
				}

				@Override
				public void clientConnected(WebSocketProxy p) {
					status.setProperty("Connected.");
					p.createConnector(args.data, args.proxyHost, args.proxyPort);
				}

				@Override
				public void data(byte[] payload, int offset, int len, String id, boolean fromWs) {
					if (args.tcpdumpLogFolder != null) {
						try {
							try (FileOutputStream fos = new FileOutputStream(
									new File(args.tcpdumpLogFolder, id + "-" + fromWs), true)) {
								fos.write(payload, offset, len);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				@Override
				public void disconnected(WebSocketProxy p) {
					exit.setProperty(true);
				}
			});
			wsp.errorEvent.addListener(err -> {
				status.setProperty("Error: " + (err == null ? "null" : err.getMessage()));
			});
			wsp.setPingInterval(5000);
			URI webSocketUri = new URI(
					(args.ssl ? "wss" : "ws") + "://" + args.host + args.getPortPostfix() + args.path);
			// Cookies are manually copied from the HTTP client session into the upgrade request
			// Cookies let us through authentication
			ClientUpgradeRequest request = new ClientUpgradeRequest();
			List<HttpCookie> cookies = new ArrayList<HttpCookie>();
			for (HttpCookie o : httpc.getCookieStore().getCookies()) {
				HttpCookie cook = new HttpCookie(o.getName(), o.getValue());
				cookies.add(cook);
			}
			request.setCookies(cookies);
			cli.connect(wsp, webSocketUri, request);
			System.out.println("URI to connect to: " + webSocketUri);
			try (UtilEventQueue<Boolean> events = new UtilEventQueue<>(exit)) {
				while (!exit.getProperty()) {
					while (events.events.poll(5000, TimeUnit.MILLISECONDS) == null) {
					}
				}
			}
			status.setProperty("Exited.");
		} finally {
			if (cli != null) {
				cli.stop();
				cli.destroy();
			}
		}
	}
}
