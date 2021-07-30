package com.hey.handler;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import com.hey.cache.client.RedisCacheExtend;
import com.hey.manager.JwtManager;
import com.hey.manager.UserWsChannelManager;
import com.hey.model.ChatMessageRequest;
import com.hey.model.IWsMessage;
import com.hey.model.ReceiveMoneyMessage;
import com.hey.service.WalletService;
import com.hey.util.ErrorCode;
import com.hey.util.HeyHttpStatusException;
import com.hey.util.HttpStatus;
import com.hey.util.JsonUtils;
import com.hey.walletmodel.ChangePassRequest;
import com.hey.walletmodel.ChangePassResponse;
import com.hey.walletmodel.ChangePinRequest;
import com.hey.walletmodel.ChangePinResponse;
import com.hey.walletmodel.CreatePresentRequest;
import com.hey.walletmodel.GetP2PsRequest;
import com.hey.walletmodel.GetPresentRequest;
import com.hey.walletmodel.GetPresentResponse;
import com.hey.walletmodel.GetTopupsRequest;
import com.hey.walletmodel.P2PTransaction;
import com.hey.walletmodel.Present;
import com.hey.walletmodel.PresentsOfSession;
import com.hey.walletmodel.Profile;
import com.hey.walletmodel.RegisterWallet;
import com.hey.walletmodel.RegisterWalletResponse;
import com.hey.walletmodel.RemovePresentResponse;
import com.hey.walletmodel.SendP2PRequest;
import com.hey.walletmodel.SendP2PResponse;
import com.hey.walletmodel.TopupRequest;
import com.hey.walletmodel.TopupResponse;
import com.hey.walletmodel.TopupTransaction;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.HttpStatusException;

public class WalletProtectedHandler {
	
	private WalletService walletService;
	private UserWsChannelManager chanelManager;
	private WsHandler wsHandler;
	private RedisCacheExtend redisCache;
    private static final Logger LOGGER = LogManager.getLogger(ProtectedApiHandler.class);

	public RedisCacheExtend getRedisCache() {
		return redisCache;
	}
	public void setRedisCache(RedisCacheExtend redisCache) {
		this.redisCache = redisCache;
	}
	public WalletProtectedHandler(WalletService walletService) {
	
		this.walletService = walletService;
	}
	public void setWsHanlder(WsHandler wsHandler) {
		this.wsHandler = wsHandler;
	}
	
    public WalletService getWalletService() {
		return walletService;
	}


	public void setWalletService(WalletService walletService) {
		this.walletService = walletService;
	}



	public UserWsChannelManager getChanelManager() {
		return chanelManager;
	}



	public void setChanelManager(UserWsChannelManager chanelManager) {
		this.chanelManager = chanelManager;
	}



	public JwtManager getJwtManager() {
		return jwtManager;
	}



	public void setJwtManager(JwtManager jwtManager) {
		this.jwtManager = jwtManager;
	}


	public static final String AUTHENTICATION_SCHEME = "Bearer";
    private JwtManager jwtManager;
    
	public void handle(RoutingContext ctx) {
		HttpServerRequest request = ctx.request();
		HttpServerResponse response =ctx.response();
	 	String requestPath = request.path();

	 	try {
	 		String authorization= request.headers().get(HttpHeaders.AUTHORIZATION);
	 		
	 		if(StringUtils.isBlank(authorization)) {
                throw new HttpStatusException(HttpStatus.UNAUTHORIZED.code(), HttpStatus.UNAUTHORIZED.message());
	 		}
            authorization = authorization.replace(AUTHENTICATION_SCHEME, "").trim();
            
            JsonObject authInfo = new JsonObject();
            authInfo.put("jwt", authorization);
            jwtManager.authenticate(authInfo, event ->{
            	if(event.succeeded()) {
            		String path = StringUtils.substringAfter(requestPath, "/api/wallet/protected");
            		String userId = event.result().principal().getString("userId");
            		JsonObject jsonObject= null;
            		if(ctx.getBody()!=null && ctx.getBody().length() >0) 
            			jsonObject = ctx.getBodyAsJson();
            		switch(path) {
            		case "/profile":
        				getProfile(request,response,jsonObject,userId);
        			
        				break;
        			case "/changePassword":
        				changePassword(request,response,jsonObject,userId);
        				break;
        			case "/changePin":
        				changePin(request,response,jsonObject,userId);
        				break;
        			case "/balance":
        				getWallet(request,response,jsonObject,userId);
        				break;
        			case "/topup":
        				topup(request,response,jsonObject,userId);
        				break;
        			case "/sendP2P":
        				
        				sendP2P(request,response,jsonObject,userId);
        				break;
        			case "/sendPresent":
        				sendPresent(request,response,jsonObject,userId);
        				break;
        			case "/receivePresent":
        				
        				receivePresent(request, response, jsonObject,userId);
        				break;
        			case "/getAllLixis":
        				getAllLixis(request, response, jsonObject, userId);
        				break;
        			case "/removeLixi":
        				removeLixi(request, response, jsonObject, userId);
        				break;
        			case "/getVd":
        				getvd(request, response, jsonObject, userId);
        				break;
        			case "/topupDirect":
        				topupDirect(request, response, jsonObject, userId);
        				break;
        			case "/getTopupTransactions":
        				getTopupTransactions(request, response, jsonObject, userId);
        				break;
        			case "/getP2PTransactions":
        				getP2PTransactions(request, response, jsonObject, userId);
        				break;
        			case "/check2User":
        				check2User(request, response, jsonObject, userId);
        				break;
        			case "/registerWallet":
        				registerWallet(request, response, jsonObject, userId);
        				break;
        			}
            	}
            });
	 		
	 			 		
	 	}catch (HttpStatusException e) {
	 		
            JsonObject obj = new JsonObject();
            obj.put("code", ErrorCode.AUTHORIZED_FAILED.code());
            obj.put("message", e.getPayload());
            response.setStatusCode(e.getStatusCode())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonUtils.toErrorJSON(obj));
        }
	}
	

	
	private void registerUser(HttpServerRequest request, HttpServerResponse response, JsonObject jsonObject,
			String userId) {
		
	}
	private void registerWallet(HttpServerRequest request, HttpServerResponse response, JsonObject jsonObject,
			String userId) {
		// dang ky no thi lam soa ?
			userId = "2f2d38a7-22b9-4eef-87e0-677187c6fe2d";
		 Future<RegisterWalletResponse> future = walletService.registerWallet(jsonObject.mapTo(RegisterWallet.class), userId);
		 future.compose(result->{
			//	System.out.println("Da xoa xong");
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			},Future.future().setHandler(handler->{
				handleException(handler.cause(), response);	

			}));
	}
	private void check2User(HttpServerRequest request, HttpServerResponse response, JsonObject jsonObject,
			String userId) {
			Future<String> sessionId = redisCache.getSessionIdChatPair(userId, jsonObject.getString("userId"));
			sessionId.compose(result->{
				
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			
			},Future.future().setHandler(handler->{
				handleException(handler.cause(), response);	
			}));
	}
	private void delAllPresent(HttpServerRequest request, HttpServerResponse response, JsonObject jsonObject,String userId) {
		Future<Void> fut = walletService.deleteAllPresent();
		fut.compose(result->{
			System.out.println("Da xoa xong");
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON("Successful"));
		},Future.future().setHandler(handler->{
			handleException(handler.cause(), response);	

		}));
	}
	private void delAllMsgPresent(HttpServerRequest request, HttpServerResponse response, JsonObject jsonObject,String userId) {
		Future<Void> fut = walletService.deleteAllMsgPresent();
		fut.compose(result->{
			System.out.println("Da xoa xong all");
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON("Successful"));
		},Future.future().setHandler(handler->{
			handleException(handler.cause(), response);	

		}));
	}
	
	public void getProfile(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<Profile> future = walletService.getProfile(userId);
		System.out.println("vao ham getProfile");
		
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler -> {handleException(handler.cause(), response);}));
	}
	public void getWallet(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<Profile> future = walletService.getProfile(userId);
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler -> {handleException(handler.cause(), response);}));
	}
	
	
	public void changeProfile(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		
	}
	
	public void changePassword(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		ChangePassRequest rq = jsonObject.mapTo(ChangePassRequest.class);
		
		Future<ChangePassResponse> future = walletService.changePassword(rq,userId);
		
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler->{handleException(handler.cause(), response);;}));
	}
	
	public void changePin(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		
		ChangePinRequest rq= jsonObject.mapTo(ChangePinRequest.class);
	
		Future<ChangePinResponse> future = walletService.changePin(rq,userId);
		
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler->{handleException(handler.cause(), response);;}));
	}
	
	public void topup(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		System.out.println(jsonObject.getString("message")+" "+jsonObject.getLong("amount")+ " with pin: "+jsonObject.getString("pin"));
		Future<TopupResponse> future = walletService.topup(jsonObject.mapTo(TopupRequest.class), userId);
		
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler->{handleException(handler.cause(), response);}));
	}
	public void topupDirect(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		System.out.println(jsonObject.getString("message")+" "+jsonObject.getLong("amount")+ " with pin: "+jsonObject.getString("pin"));
		Future<TopupResponse> future = walletService.topup(jsonObject.mapTo(TopupRequest.class), userId);
		
		future.compose(result->{
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
		},Future.future().setHandler(handler->{handleException(handler.cause(), response);}));
	}
	
	
	
	private void processMessageP2P(SendP2PRequest request , String userId) {
		System.out.println("username:"+request.getUsername());
		Future<String> getIdFuture = redisCache.getUserIdByUsername(request.getUsername());
		getIdFuture.setHandler(ar->{
			if(ar.succeeded()) {
				Future<String> findSessionId = redisCache.findSessionIdTwoPerson(userId, ar.result());
				Future<String> getUsernameFuture = redisCache.findUsernameById(userId);
				
				CompositeFuture cp = CompositeFuture.all(getUsernameFuture,findSessionId);
				cp.setHandler(ar1->{
					if(ar1.succeeded()) {
						String sender = cp.resultAt(0);
						String sessionId = cp.resultAt(1);
						
						List<String> usernames = new ArrayList<>();
						usernames.add(sender); usernames.add(request.getUsername());
						
						ChatMessageRequest msg = new ChatMessageRequest();
						msg.setGroupChat(false);
						
						if(sessionId==null) msg.setSessionId("-1");
						else msg.setSessionId(sessionId);
						
						msg.setType(IWsMessage.TYPE_CHAT_MESSAGE_RESPONSE);
						msg.setUsernames(usernames);
						msg.setMessage(sender + "-SEND-"+ request.getUsername() + "-AMOUNT-"+request.getAmount());
						wsHandler.insertChatMessageOnExistedChatSessionId(msg, null, userId);
					}
				});
			}
		
		});
		
		
		
	}
	
	
	public void sendP2P(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		
		
		SendP2PRequest rq = jsonObject.mapTo(SendP2PRequest.class);
		
		Future<SendP2PResponse> future = walletService.sendP2P(rq, userId);
		
		future.compose(result->{
			
			response.setStatusCode(HttpStatus.OK.code())
			.putHeader("content-type", "application/json; charset=utf-8")
			.end(JsonUtils.toSuccessJSON(result));
			
			
			 
			
			
		},Future.future().setHandler(handler->{handleException(handler.cause(), response);}));

	}
	
	public void sendPresent(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		CreatePresentRequest rq = jsonObject.mapTo(CreatePresentRequest.class);
		Future<Present> future = walletService.createLixi(rq, userId);
		future.compose(result->{
			;
			if(result!=null) {
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}else {
				JsonObject obj =new JsonObject();
				obj.put("message", "Not Enought Balance");
						response.setStatusCode(HttpStatus.OK.code())
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(JsonUtils.toSuccessJSON(obj));
			}
		}, Future.future().setHandler(handler->{
			System.out.println(handler.cause().toString());
			
			handleException(handler.cause(), response);
		}));
		
	}
	
	public void receivePresent(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		GetPresentRequest rq = jsonObject.mapTo(GetPresentRequest.class);
		
		
		Future<GetPresentResponse> future = walletService.getLixi(rq, userId);
		future.compose(result->{
			
			if(result!=null) {
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}else {
				JsonObject obj =new JsonObject();
				obj.put("message", "Sorry , Not have present for you !!!");
						response.setStatusCode(HttpStatus.OK.code())
						.putHeader("content-type", "application/json; charset=utf-8")
						.end(JsonUtils.toSuccessJSON(obj));
			}
		}, Future.future().setHandler(handler->{
			System.out.println(handler.cause().toString());
			
			handleException(handler.cause(), response);
		}));
	}
	
	public void getAllLixis(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<List<PresentsOfSession>> future = walletService.getAllLixisOfUser(userId);
		future.compose(result->{
			if(result!=null) {
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}
		}, Future.future().setHandler(handler->{
			handleException(handler.cause(), response);
		}));
	}
	public void removeLixi(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<RemovePresentResponse> future = walletService.removePresent(jsonObject.getString("presentId")
				, jsonObject.getString("sessionId"), userId);
		future.compose(result->{
			if(result!=null) {
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}
		}, Future.future().setHandler(handler->{
			handleException(handler.cause(), response);
		}));
	}
	public void getTopupTransactions(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<List<TopupTransaction>> future = walletService.getTopupTransactions(jsonObject.mapTo(GetTopupsRequest.class), userId);
		future.compose(result->{
			if(result!=null) {
				System.out.println("co result");
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}
		}, Future.future().setHandler(handler->{
			handleException(handler.cause(), response);
		}));
	}
	public void getP2PTransactions(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		Future<List<P2PTransaction>> future = walletService.getP2PTransactions(jsonObject.mapTo(GetP2PsRequest.class), userId);
		future.compose(result->{
			if(result!=null) {
				System.out.println("co result");
				response.setStatusCode(HttpStatus.OK.code())
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(result));
			}
		}, Future.future().setHandler(handler->{
			handleException(handler.cause(), response);
		}));
	}
	public void getvd(HttpServerRequest request , HttpServerResponse response,JsonObject jsonObject ,String userId) {
		boolean x = BCrypt.checkpw("123456", "$2a$10$UJspLYH87IVKKCrjeWtXz.RCGM2Z5k3zrxWaYnhJjUqacaMG39lqe");
		JsonObject obj = new JsonObject();
		obj.put("code", x);
		response.setStatusCode(200)
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(JsonUtils.toSuccessJSON(obj));

	}
	public void handleException(Throwable throwable, HttpServerResponse response) {

        if (throwable instanceof HeyHttpStatusException) {
            HeyHttpStatusException e = (HeyHttpStatusException) throwable;
            JsonObject obj = new JsonObject();
            obj.put("code", e.getCode());
            obj.put("message", e.getPayload());
            response.setStatusCode(e.getStatusCode())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonUtils.toErrorJSON(obj));
            return;
        }
        else
        if (throwable instanceof Exception) {
            Exception e = (Exception) throwable;
            LOGGER.error(e);
            e.printStackTrace();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonUtils.toError500JSON());
            return;
        }
        else {
        response.setStatusCode(HttpStatus.OK.code())
       .putHeader("content-type", "application/json; charset=utf-8")
       .end(JsonUtils.toErrorJSON(throwable.getMessage()));
        }
	}


}
