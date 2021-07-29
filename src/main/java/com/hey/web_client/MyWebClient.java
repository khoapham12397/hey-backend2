package com.hey.web_client;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class MyWebClient {
	
	//private WebClient webClient=null;
	private HttpRequest<JsonObject> request = null;
	public MyWebClient(Vertx vertx) {
	
		request = WebClient.create(vertx) // (1)
			      .get(8080, "localhost", "/get") // (2)
			//      .ssl(true)  // (3)
			      .putHeader("Accept", "application/json")  // (4)
			      .as(BodyCodec.jsonObject()); // (5)
	}
	
	public void getExample() {
		request.send(ar->{
			if(ar.succeeded()) {
			 	HttpResponse<JsonObject> response = ar.result();
			 	System.out.println("STATUS CODE: "+response.statusCode());
			 	System.out.println(response.body().getString("user_status"));
			 	
			}
		});
	}
}
