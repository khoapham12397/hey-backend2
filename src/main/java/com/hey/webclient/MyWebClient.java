package com.hey.webclient;

import java.util.HashMap;
import java.util.Map;

import com.hey.walletmodel.ChangePassRequest;
import com.hey.walletmodel.ChangePassResponse;
import com.hey.walletmodel.ChangePinRequest;
import com.hey.walletmodel.ChangePinResponse;
import com.hey.walletmodel.Profile;
import com.hey.walletmodel.TopupRequest;
import com.hey.walletmodel.TopupResponse;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;

public class MyWebClient {
	private WebClient client;
	
	public MyWebClient(Vertx vertx) {
		this.client = WebClient.create(vertx);
	}
	
	
	public Future<Profile> getProfile(String userId){
		
		Future<Profile> future = Future.future();
		
		HttpRequest<JsonObject> rq = client.get(8080, "localhost", "/profile?userId="+userId)
				.putHeader("Accept","application/json")
				.as(BodyCodec.jsonObject());
		rq.send(ar->{
			if(ar.succeeded()) {
				HttpResponse<JsonObject> res = ar.result();
				
				if(res.statusCode()==200) {
					Profile profile = res.bodyAsJson(Profile.class);
					future.complete(profile);
				}else future.fail("internal server error");
				
			}else future.fail(ar.cause());
		});
		
		return future;
	}
	
	
	public Future<JsonObject> getWallet(){
		Future<JsonObject> future = Future.future();
		
		return future;
	}
	public Future<JsonObject> doEx(){
		Future<JsonObject> future = Future.future();
		HttpRequest<JsonObject> rq = client.post(8080,"localhost","/postEx")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject());
		
		Map<String,Object> map = new HashMap<>();
		map.put("name","khoa pham");
		map.put("age",24L);
		JsonObject.mapFrom(map);
		
		return future;
	}
	
	public Future<ChangePassResponse> changePassword(ChangePassRequest body){
		
		Future<ChangePassResponse> future = Future.future();
		
		HttpRequest<JsonObject> rq = client.post(8080,"localhost","/changePass")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject());
		rq.sendJson(body, ar->{
			if(ar.succeeded()) {
				HttpResponse<JsonObject> res = ar.result();
				if(res.statusCode()==200 && res.body()!=null) {
					future.complete(res.bodyAsJson(ChangePassResponse.class));
				}else future.fail("Internal Server Error");
			}else {
				future.fail(ar.cause());
			}
		});
		
		return future;
	}
	
	public Future<ChangePinResponse> changePin(ChangePinRequest body){
	
		Future<ChangePinResponse> future = Future.future();
		HttpRequest<JsonObject> rq = client.post(8080,"localhost","/changePin")
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject());
		rq.sendJson(body, ar->{
			if(ar.succeeded()) {
				HttpResponse<JsonObject> res =ar.result();
				if(res.statusCode()==200 && res.body()!=null) {
					future.complete(res.bodyAsJson(ChangePinResponse.class));
				}
				else future.fail(res.body().getString("message"));
				
			}else future.fail(ar.cause());
		});
		
		return future;
	}
	
	public Future<JsonObject> callPostService(String url , JsonObject body){
		Future<JsonObject> future = Future.future();
		
		HttpRequest<JsonObject> rq = client.post(8080,"localhost",url)
			.putHeader("Accept", "application/json")
			.as(BodyCodec.jsonObject());
		rq.sendJsonObject(body, ar->{
			if(ar.succeeded()) {
				HttpResponse<JsonObject> res = ar.result();
				if(res.statusCode()==200 && res.body()!=null) {
					future.complete(res.body());
				}
				else future.fail(res.body().getString("message"));
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<JsonObject> callGetService(String url , JsonObject body){
		
		Future<JsonObject> future = Future.future();
		
		HttpRequest<JsonObject> rq = client.get(8080,"localhost",url)
				.putHeader("Accept", "application/json")
				.as(BodyCodec.jsonObject());
			rq.send(ar->{
				if(ar.succeeded()) {
					HttpResponse<JsonObject> res = ar.result();
					if(res.statusCode()==200 && res.body()!=null) {
						future.complete(res.body());
					}
					else future.fail(res.body().getString("message"));
				}
				else future.fail(ar.cause());
			});
		return future;
	}
	
	public Future<JsonObject> getTransactions(){
		Future<JsonObject> future = Future.future();
		return future;
	}
	
}
