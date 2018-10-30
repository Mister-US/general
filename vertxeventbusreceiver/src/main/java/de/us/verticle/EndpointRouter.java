package de.us.verticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;

public class EndpointRouter {

	private static final String ENDPOINT_ADD_MESSAGE = "/addMessage";
	private static final String ENDPOINT_QUERY_FOR_MESSAGE = "/queryForMessage";
	private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";

	private Vertx vertx;
	private ServerWebSocket serverWebSocket;
	private Router router;

	private static Logger logger = LogManager.getLogger(EndpointRouter.class);

	/**
	 * Constructor
	 * 
	 * @param vertx
	 * @throws Exception
	 */
	public EndpointRouter(Vertx vertx) {
		this.vertx = vertx;

		setupRouter();

	}

	/**
	 * 
	 * Gives a web router back
	 * 
	 * @return Router
	 */
	public Router getRouter() {
		return router;
	}

	/**
	 * Sets server web socket
	 * 
	 * @param serverWebSocket
	 */
	public void setServerWebSocket(ServerWebSocket serverWebSocket) {
		this.serverWebSocket = serverWebSocket;
	}

	private void setupRouter() {
		router = Router.router(vertx);

		createGetMessageRoute();

		createPostMessageRoute();

	}

	private void createPostMessageRoute() {

		router.route().handler(CorsHandler.create("*").allowedHeader("Content-type"));

		router.route(HttpMethod.POST, ENDPOINT_ADD_MESSAGE).handler(routingContext -> {
			routingContext.request().bodyHandler(buffer -> {

				logger.info("Response from endpoint: " + buffer.toString());

				HttpServerResponse response = routingContext.response();
				response.putHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*").setStatusMessage("Message received")
						.setStatusCode(200).end();

				logger.info("Send message to the socket");
				sendMessage2Socket(buffer.toString());

			});

		});
	}

	private void sendMessage2Socket(String message) {
		if (serverWebSocket != null) {
			serverWebSocket.writeTextMessage(message);
		} else {
			logger.warn("ServerWebSocket is not opened by the client!");
		}
	}

	private void createGetMessageRoute() {

		router.route(HttpMethod.GET, ENDPOINT_QUERY_FOR_MESSAGE).handler(routingContext -> {

			HttpServerResponse response = routingContext.response();
			response.putHeader("content-type", "application/json; charset=utf-8")
					.putHeader(HEADER_ACCESS_CONTROL_ALLOW_ORIGIN, "*").end("Testmessage");
		});

	}

}
