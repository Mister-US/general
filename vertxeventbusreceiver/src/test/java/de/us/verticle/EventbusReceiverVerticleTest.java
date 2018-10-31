package de.us.verticle;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

@RunWith(VertxUnitRunner.class)
public class EventbusReceiverVerticleTest {
	
	private Vertx vertx;
	private int port;
	
	@Before
	public void before(TestContext testContext) throws IOException {
		ServerSocket socket = new ServerSocket(0);
		port = socket.getLocalPort();
		socket.close();
		
		DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));
		
		vertx = Vertx.vertx();
		vertx.deployVerticle(EventbusReceiverVerticle.class.getName(), deploymentOptions, testContext.asyncAssertSuccess());
	}
	
	@After
	public void after(TestContext testContext) {
		vertx.close(testContext.asyncAssertSuccess());
	}

	@Test
	public void TestAddMessage(TestContext testContext) throws IOException {
		Async async = testContext.async();
		WebClient webClient = WebClient.create(vertx);
		JsonObject jsonObject = new JsonObject(readFile("src/test/resources/TestJson"));
		webClient.post(8080, "localhost", "/addMessage").sendJsonObject(jsonObject, response -> {
			HttpResponse<Buffer> httpResponse = response.result();
			testContext.assertEquals(httpResponse.statusCode(), 200);
			webClient.close();
			async.complete();
		});
	}
	
	private String readFile(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line = br.readLine();

		while (line != null) {
			sb.append(line);
			sb.append("\n");
			line = br.readLine();
		}
		return sb.toString();
	}
	
}
