package de.us.verticle;

import java.net.InetAddress;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.FileSystem;
import io.vertx.core.impl.launcher.VertxCommandLauncher;

public class EventbusSenderVerticle extends AbstractVerticle {

	private static Logger logger = LogManager.getLogger(EventbusSenderVerticle.class);

	private long timerId = 0;

	private EventBus eventBus;

	private String inputDir;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		logger.info("-------------------------------------------------------------------------");
		logger.info("Start EventbusSenderVerticle...");
		logger.info("-------------------------------------------------------------------------");

		String local = InetAddress.getLocalHost().getHostAddress();

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setClusterHost(local);

		Vertx.clusteredVertx(vertxOptions, res -> {
			if (res.succeeded()) {
				vertx = res.result();
				logger.info("Cluster started");

				List<String> args = VertxCommandLauncher.getProcessArguments();
				
				if (args.get(0).equals("run")) {
					String home = System.getProperty("user.home");
					inputDir = home + args.get(2);
				} else {
					inputDir = args.get(0);
				}

				eventBus = vertx.eventBus();

				sendMessages();
			} else {
				logger.error("Starting Cluster failed!");
			}
		});

	}

	private void sendMessages() {
		
		FileSystem fileSystem = vertx.fileSystem();

		timerId = vertx.setPeriodic(3000, along ->

		fileSystem.readDir(inputDir, res1 -> {

			if (res1.succeeded()) {

				List<String> files = res1.result();

				readFiles(fileSystem, files);

			}
		}));
	}

	private void readFiles(FileSystem fileSystem, List<String> files) {
		for (String file : files) {

			if (file.contains("MT")) {
				fileSystem.readFile(file, res2 -> {

					if (res2.succeeded()) {

						eventBus.send("file.interact", res2.result().toString());

						logger.info("Send content of file " + file + " to the Event Bus");			
						fileSystem.deleteBlocking(file);

					} else {
						logger.error("File " + file + " does not exist!");
					}

				});
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#stop()
	 */
	@Override
	public void stop() {
		vertx.cancelTimer(timerId);
		vertx.close();
	}

}
