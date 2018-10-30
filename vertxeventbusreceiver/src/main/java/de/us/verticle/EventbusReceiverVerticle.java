package de.us.verticle;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.ServerWebSocket;

public class EventbusReceiverVerticle extends AbstractVerticle {

	private static Logger logger = LogManager.getLogger(EventbusReceiverVerticle.class);

	private ServerWebSocket serverWebSocket;
	private EndpointRouter endpointRouter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start(io.vertx.core.Future)
	 */
	@Override
	public void start(Future<Void> startFuture) throws Exception {

		logger.info("-------------------------------------------------------------------------");
		logger.info("Start EventbusReceiverVerticle...");
		logger.info("-------------------------------------------------------------------------");

		String local = InetAddress.getLocalHost().getHostAddress();

		VertxOptions vertxOptions = new VertxOptions();
		vertxOptions.setClusterHost(local);

		Vertx.clusteredVertx(vertxOptions, res -> {
			if (res.succeeded()) {
				vertx = res.result();
				logger.info("Cluster started");
				consumeMessages();
			} else {
				logger.error("Starting Cluster failed!");
			}
		});

		endpointRouter = new EndpointRouter(vertx);

		logger.info("Start WebServer on Port 8080...");

		vertx.createHttpServer().websocketHandler(this::handleWebSocket)
				.requestHandler(endpointRouter.getRouter()::accept).listen(8080, result -> {
					if (result.succeeded()) {
						startFuture.complete();
					} else {
						startFuture.fail(result.cause());
					}
				});

	}

	private void handleWebSocket(ServerWebSocket serverWebSocket) {

		logger.info("Connected on WebSocket...");

		this.serverWebSocket = serverWebSocket;
		endpointRouter.setServerWebSocket(serverWebSocket);

		serverWebSocket.handler(buffer -> logger.info("Buffer received from websocket client: " + buffer.toString()));

	}

	private void consumeMessages() {
		EventBus eventBus = vertx.eventBus();

		eventBus.consumer("file.interact", message -> {

			logger.info("-------------------------------------------------------------------");
			logger.info("Received Message from Eventbus...");

			logger.info("Send Message to the socket");
			sendMessage2Socket(message.body().toString());

		});
	}

	private void sendMessage2Socket(String message) {
		if (serverWebSocket != null) {
			serverWebSocket.writeTextMessage(message);
		} else {
			logger.warn("ServerWebSocket is not opened by the client!");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#stop()
	 */
	@Override
	public void stop() {
		vertx.close();
	}

}
