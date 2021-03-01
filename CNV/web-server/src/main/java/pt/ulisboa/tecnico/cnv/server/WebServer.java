package pt.ulisboa.tecnico.cnv.server;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import pt.ulisboa.tecnico.cnv.metrics.LocalStorage;
import pt.ulisboa.tecnico.cnv.metrics.Metric;
import pt.ulisboa.tecnico.cnv.metrics.RemoteStorage;
import pt.ulisboa.tecnico.cnv.solver.Solver;
import pt.ulisboa.tecnico.cnv.solver.SolverArgumentParser;
import pt.ulisboa.tecnico.cnv.solver.SolverFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class WebServer {

	public static void main(final String[] args) throws Exception {
		
		//final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8000), 0);

		final HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);


		server.createContext("/climb", new MyHandler());
		server.createContext("/metrics", new MetricsHandler());
		//RemoteStorage.getInstance();
		// be aware! infinite pool of threads!
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();

		System.out.println(server.getAddress().toString());
	}

	static class MyHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {

			try {


				// Setup metrics
				long threadID = Thread.currentThread().getId();

				// Get the query.
				final String query = t.getRequestURI().getQuery();

				System.out.println("> Query:\t" + query);

				// Break it down into String[].
				final String[] params = query.split("&");
			/*
			for(String p: params) {
				System.out.println(p);
			}
			*/
				// Store as if it was a direct call to SolverMain.
				final ArrayList<String> newArgs = new ArrayList<>();
				String uuid= null;
				for (final String p : params) {
					final String[] splitParam = p.split("=");
					if(!splitParam[0].equals("uuid")) {
						newArgs.add("-" + splitParam[0]);
						newArgs.add(splitParam[1]);
					}else {
						uuid = splitParam[1];
					}
				/*
				System.out.println("splitParam[0]: " + splitParam[0]);
				System.out.println("splitParam[1]: " + splitParam[1]);
				*/
				}
				newArgs.add("-d");

				// Store from ArrayList into regular String[].
				final String[] args = new String[newArgs.size()];
				int i = 0;
				for(String arg: newArgs) {
					args[i] = arg;
					i++;
				}
			/*
			for(String ar : args) {
				System.out.println("ar: " + ar);
			} */

				SolverArgumentParser ap = null;
				try {
					// Get user-provided flags.
					ap = new SolverArgumentParser(args);
				}
				catch(Exception e) {
					System.out.println(e);
					return;
				}
				if(uuid == null) {
					uuid = UUID.randomUUID().toString();
					//	LocalStorage.getRequests().put(uuid, threadID);
				}
				// Initializing metric in this thread to be used by BIT
				Metric m = new Metric(ap, uuid);
				LocalStorage.getMemory().put(threadID, m);


				System.out.println("> Finished parsing args.");

				// Create solver instance from factory.
				final Solver s = SolverFactory.getInstance().makeSolver(ap);

				// Write figure file to disk.
				File responseFile = null;
				try {

					final BufferedImage outputImg = s.solveImage();

					final String outPath = ap.getOutputDirectory();

					final String imageName = s.toString();

					if(ap.isDebugging()) {
						System.out.println("> Image name: " + imageName);
					}

					final Path imagePathPNG = Paths.get(outPath, imageName);
					ImageIO.write(outputImg, "png", imagePathPNG.toFile());

					responseFile = imagePathPNG.toFile();

				} catch (final IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}

				// Send response to browser.
				final Headers hdrs = t.getResponseHeaders();

				hdrs.add("Content-Type", "image/png");

				hdrs.add("Access-Control-Allow-Origin", "*");
				hdrs.add("Access-Control-Allow-Credentials", "true");
				hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
				hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");

				t.sendResponseHeaders(200, responseFile.length());
				final OutputStream os = t.getResponseBody();
				Files.copy(responseFile.toPath(), os);
				os.close();

				System.out.println("> Sent response to " + t.getRemoteAddress().toString());

				// Save instrumentation metrics
				RemoteStorage.getInstance().addMetric(LocalStorage.getMemory().get(threadID));
				LocalStorage.getMemory().remove(threadID);
			}
			catch(Exception e) {
				// Send response to browser.
				e.printStackTrace();
				final Headers hdrs = t.getResponseHeaders();

				hdrs.add("Content-Type", "image/png");

				hdrs.add("Access-Control-Allow-Origin", "*");
				hdrs.add("Access-Control-Allow-Credentials", "true");
				hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
				hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
				String error = "Error";
				t.sendResponseHeaders(400, error.getBytes().length);
				final OutputStream os = t.getResponseBody();
				os.write(error.getBytes());
				os.close();

				System.out.println("> Sent response to " + t.getRemoteAddress().toString());
			}

		}
	}

	static class MetricsHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange t) throws IOException {
			long threadID = Thread.currentThread().getId();
			// Get the query.
			final String query = t.getRequestURI().getQuery();

			System.out.println("> Query:\t" + query);

			// Break it down into String[].
			String[] params = new String[0];
			if(query != null)
				 params = query.split("&");
			/*
			for(String p: params) {
				System.out.println(p);
			}
			*/
			// Store as if it was a direct call to SolverMain.
			HashMap<String, String> newArgs = new HashMap<>();
			for (final String p : params) {
				final String[] splitParam = p.split("=");
				newArgs.put(splitParam[0], splitParam[1]);
			}

			// Send response to browser.
			Gson gson = new Gson();
			String json = gson.toJson(LocalStorage.getMemory().values());

			final Headers hdrs = t.getResponseHeaders();

			hdrs.add("Content-Type", "application/json");

			hdrs.add("Access-Control-Allow-Origin", "*");
			hdrs.add("Access-Control-Allow-Credentials", "true");
			hdrs.add("Access-Control-Allow-Methods", "POST, GET, HEAD, OPTIONS");
			hdrs.add("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
			t.sendResponseHeaders(200, json.length());


			final OutputStream os = t.getResponseBody();
			os.write(json.getBytes());
			os.close();

			System.out.println("> Sent response to " + t.getRemoteAddress().toString());
		}
	}
}
