package com.hey.service;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import com.hey.cache.client.RedisCacheExtend;
import com.hey.handler.WsHandler;
import com.hey.manager.UserWsChannelManager;
import com.hey.model.ChatMessageRequest;
import com.hey.model.IWsMessage;
import com.hey.model.ReceiveMoneyMessage;
import com.hey.util.GenerationUtils;
import com.hey.walletmodel.Authen;
import com.hey.walletmodel.ChangePassRequest;
import com.hey.walletmodel.ChangePassResponse;
import com.hey.walletmodel.ChangePinRequest;
import com.hey.walletmodel.ChangePinResponse;
import com.hey.walletmodel.ChangeProfileRequest;
import com.hey.walletmodel.CreatePresentRequest;
import com.hey.walletmodel.GetBalanceResponse;
import com.hey.walletmodel.GetP2PsRequest;
import com.hey.walletmodel.GetPresentRequest;
import com.hey.walletmodel.GetPresentResponse;
import com.hey.walletmodel.GetTopupsRequest;
import com.hey.walletmodel.LixisOfSession;
import com.hey.walletmodel.P2PTransaction;
import com.hey.walletmodel.Present;
import com.hey.walletmodel.PresentsOfSession;
import com.hey.walletmodel.Profile;
import com.hey.walletmodel.ReceiveLixi;
import com.hey.walletmodel.RegisterWallet;
import com.hey.walletmodel.RegisterWalletResponse;
import com.hey.walletmodel.RemovePresentResponse;
import com.hey.walletmodel.SendP2PRequest;
import com.hey.walletmodel.SendP2PResponse;
import com.hey.walletmodel.TopupRequest;
import com.hey.walletmodel.TopupResponse;
import com.hey.walletmodel.TopupTransaction;
import com.hey.walletmodel.WalletResponse;
import com.hey.webclient.MyWebClient;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class WalletService {
	
	private RedisCacheExtend redisCache;
	
	private MyWebClient webClient;
	private UserWsChannelManager channelManager;
	private WsHandler wsHandler;
	public WsHandler getWsHandler() {
		return wsHandler;
	}
	public void setWsHandler(WsHandler wsHandler) {
		this.wsHandler = wsHandler;
	}
	public void setUserWsChannelManager(UserWsChannelManager channelManager) {
		this.channelManager = channelManager;
	}
	public WalletService(RedisCacheExtend redisCache, MyWebClient webClient){
		this.redisCache = redisCache;
		this.webClient = webClient;
	}
	
	public Future<TopupResponse> topupDirect(TopupRequest rq,String userId){
		Future<TopupResponse> future = Future.future();
		// 1-6 ??
		JsonObject obj =new JsonObject();
		obj.put("userId",userId);
		obj.put("pin", rq.getPin());
		obj.put("amount", rq.getAmount());
		
		//rq.getPin();
		Future<JsonObject> callFuture = webClient.callPostService("/topupDirect", obj);
		callFuture.compose(result->{
			future.complete(result.mapTo(TopupResponse.class));
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());;
		}));
		return future;
	}

	public Future<ChangePassResponse> changePassword(ChangePassRequest rq, String userId){
		Future<ChangePassResponse> future = Future.future();
		JsonObject body = new JsonObject();
		
		body.put("oldPassword", rq.getOldPass());
		body.put("newPassword", rq.getNewPass());
		body.put("userId", userId);
		
		Future<JsonObject> callFuture = webClient.callPostService("/changePassword", body);
		callFuture.setHandler(ar->{
			if(ar.succeeded()) {
				JsonObject obj = ar.result();
				ChangePassResponse res = obj.mapTo(ChangePassResponse.class);
				future.complete(res);
				if(res.getCode()) redisCache.setHashedPassword(res.getHashedPassword(), userId);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<RegisterWalletResponse> registerWallet( RegisterWallet rq, String userId){
		Future<RegisterWalletResponse> future = Future.future();
		Future<Boolean> existWFuture= redisCache.existWallet(userId);
		existWFuture.setHandler(ar->{
			if(ar.succeeded()) {
				if(!ar.result()) {
					JsonObject body =new JsonObject();
					body.put("userId",  userId); body.put("hashedPin", rq.getHashedPin());
					body.put("identity", rq.getIdentity()); 
					body.put("phone", rq.getPhone());
					body.put("email",rq.getEmail());
					Future<JsonObject> callFuture = webClient.callPostService("/registerWallet", body);
					callFuture.compose(result->{
						future.complete(result.mapTo(RegisterWalletResponse.class));
						redisCache.insertWallet(rq.getHashedPin(), userId);
					},Future.future().setHandler(handler->{
						future.fail(handler.cause());
					}));
				}
				else future.fail("Your wallet is already exist");
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	public Future<GetBalanceResponse> getWallet(String userId){
		Future<GetBalanceResponse> future = Future.future();
		JsonObject obj =new JsonObject();
		obj.put("userId", userId);
		
	 	Future<JsonObject> callFuture = webClient.callPostService("/getBalance", obj );
	 	callFuture.compose(result->{
	 		GetBalanceResponse res = result.mapTo(GetBalanceResponse.class);
	 		future.complete(res);
	 	},Future.future().setHandler(handler->{
	 		future.fail(handler.cause());
	 	}));
	 	return future;
	}
	public Future<ChangePinResponse> changePin(ChangePinRequest rq, String userId){
		Future<ChangePinResponse> future = Future.future();
		JsonObject body = new JsonObject();
		body.put("userId", userId);
		body.put("oldPin", rq.getOldPin());
		body.put("newPin", rq.getNewPin());
		Future<JsonObject> callFuture = webClient.callPostService("/changePin", body);
		callFuture.setHandler(ar->{
			if(ar.succeeded()) {
				ChangePinResponse res = ar.result().mapTo(ChangePinResponse.class);
				future.complete(res);
				if(res.getCode()) redisCache.setHashedPin(rq.getNewPin(), userId);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public void callProfile(String userId , Future<Profile> future) {
		String url = "/profile?userId="+userId;
		Future<JsonObject> callFuture = webClient.callGetService(url,null);
		callFuture.setHandler(ar1->{
			if(ar1.succeeded()) {
				Profile profile = ar1.result().mapTo(Profile.class);
				
				future.complete(profile);
				redisCache.insertProfile(profile, userId);
			}
			else future.fail(ar1.cause());
		});
	}
	
	public Future<Profile> changeProfile(Profile rq, String userId){
		Future<Profile> future = Future.future();
		rq.setUserId(userId);
		Future<JsonObject> callFuture = webClient.callPostService("/changeProfile", JsonObject.mapFrom(rq));
		callFuture.setHandler(ar->{
			if(ar.succeeded()) {
				if(ar.result()!=null) {
					future.complete(ar.result().mapTo(Profile.class));
				}
				else future.complete(null);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<TopupResponse> topup(TopupRequest rq, String userId) {
		
		Future<TopupResponse> future = Future.future();
		
		String pin = rq.getPin(); 
	
		Future<WalletResponse> authFuture = redisCache.getWallet(userId);
		
		
		authFuture.compose(result->{
		
			System.out.println("hashedPin: "+ result.getHashedPin());
			if(result!=null && pin.equals(result.getHashedPin())) {
				JsonObject body =new JsonObject();
				body.put("walletId", userId);
				body.put("amount", rq.getAmount());
				body.put("message", rq.getMessage());
				String url ="/topup";
				Future<JsonObject> callFuture = webClient.callPostService(url, body);
				
				callFuture.setHandler(ar->{
					if(ar.succeeded()) {
						future.complete(ar.result().mapTo(TopupResponse.class));
					}
					else 
						future.fail(ar.cause());
					
				});	
			}
			else future.fail("Wrong PIN");
			
		}, Future.future().setHandler(handler->{
			
			future.fail(handler.cause());
		}));
		
		return future;
	}
	
	public Future<Profile> getProfile(String userId){
		
		Future<Profile> future = Future.future();
		
		Future<Profile> profileFuture = redisCache.getProfile(userId);		
		
		profileFuture.compose(result->{
			if(result!=null) future.complete(result);
			else {
					
				callProfile(userId, future);
			}
		},Future.future().setHandler(ar->{
			callProfile(userId,future);
		
		}));
		return future;
	}
	
	
	
	private void processMessageP2P(P2PTransaction ts,String senderId,String receiverId) {
		Future<String> getSessionFuture = redisCache.getSessionIdChatPair(senderId, receiverId);
		getSessionFuture.compose(result->{
			ChatMessageRequest msg = new ChatMessageRequest();
			msg.setGroupChat(false);
			msg.setType(IWsMessage.TYPE_CHAT_MESSAGE_REQUEST);
			msg.setMsgType(1);
			msg.setMessage("P2P:"+ts.getTransactionId()+ 
					":"+ts.getSender()+":"+ts.getReceiver()+":"+ ts.getAmount()+":"+ts.getTimestamp());
			List<String> usernames = new ArrayList<>();
			usernames.add(ts.getReceiver());
			
			msg.setUsernames(usernames);
			
			
			List<String> userIds =new ArrayList<>();
			userIds.add(senderId); userIds.add(receiverId);
			
			if(result==null) {
				msg.setSessionId("-1");
				wsHandler.insertChatMessageOnNewChatSessionId(msg, null, userIds);
			}
			else {
				msg.setSessionId(result);
				wsHandler.insertChatMessageOnExistedChatSessionId(msg,null,senderId);
			}
		}, Future.future().setHandler(handler->{
			throw new RuntimeException(handler.cause());
		}));
	}
	
	private void processMsgCreateLixi(Present present) {
		Future<String> getUsernameFuture = redisCache.getUsername(present.getUserId());
		getUsernameFuture.setHandler(ar->{
			if(ar.succeeded()) {
				String name = ar.result();
				if(name!=null) {
					ChatMessageRequest msg = new ChatMessageRequest();
					msg.setMessage("PRESENT:"+present.getPresentId() +":"+ present.getSessionId()+":"+ name+":"+ present.getTotalAmount()+ ":"+present.getStartTime());
					msg.setSessionId(present.getSessionId());
					msg.setGroupChat(true);
					msg.setType(IWsMessage.TYPE_CHAT_MESSAGE_REQUEST);
					msg.setUsernames(null);
					msg.setMsgType(2);
					
					wsHandler.insertChatMessageOnExistedChatSessionId(msg, null, present.getUserId());
				}
			}
		});
		
		
		
	}
	
	
	
	public Future<SendP2PResponse> sendP2P(SendP2PRequest rq, String userId){
		Future<SendP2PResponse> future = Future.future();
		Future<String> getUserIdFuture = redisCache.getUserIdByUsername(rq.getUsername());
		Future<WalletResponse> walletFuture = redisCache.getWallet(userId);
		
		CompositeFuture cp = CompositeFuture.all(getUserIdFuture, walletFuture);
		cp.setHandler(ar->{
			if(ar.succeeded()) {
				String receiverId = cp.resultAt(0);
				WalletResponse wallet = cp.resultAt(1);
				String hashedPin = wallet.getHashedPin();
				
				if(wallet!=null && hashedPin.equals(rq.getPin())) {
					String url = "/sendP2P";
					JsonObject body = new JsonObject();
					
					body.put("message", rq.getMessage());
					body.put("senderId", userId);
					body.put("receiverId",receiverId);
					body.put("amount", rq.getAmount());
					
					Future<JsonObject> callFuture = webClient.callPostService(url, body);
					callFuture.setHandler(ar1->{
						if(ar1.succeeded()) {
							SendP2PResponse response = ar1.result().mapTo(SendP2PResponse.class);
							future.complete(response);
							/*
							ReceiveMoneyMessage msg =new ReceiveMoneyMessage();
							//msg.setSenderId(userId);
							// toi phan xu ly:  dung:
							
							msg.setSender("lvhung");
							msg.setType(IWsMessage.TYPE_NOTFICATION_RECEIVE_MONEY);
							msg.setAmount(rq.getAmount());
							msg.setTimestamp(System.currentTimeMillis());
							System.out.println("ReceiverId: "+ receiverId);
							channelManager.sendMessage(msg, receiverId);
							*/
							Future<String> getUsernameFuture= redisCache.getUsername(userId);
							getUsernameFuture.setHandler(ar2->{
								P2PTransaction ts =new P2PTransaction();
								ts.setAmount(rq.getAmount());
								ts.setSender(ar2.result()); ts.setReceiver(rq.getUsername());
								ts.setTransactionId(response.getTransactionId());
								ts.setTimestamp(response.getTimestamp());
								processMessageP2P(ts, userId, receiverId);
								
							});
						}
						else {
							future.fail(ar1.cause());
						}
					});
				}
				else future.fail("wrong pin");
			}
			else future.fail(ar.cause());
		});
		/*
		authenFuture.compose(authen->{
			String hashedPin = authen.getHashedPin();
			
			if(authen!=null && hashedPin.equals(rq.getPin())) {
				String url = "/sendP2P";
				JsonObject body = new JsonObject();
				
				body.put("message", rq.getMessage());
				body.put("senderId", userId);
				body.put("receiverId",rq.getReceiverId());
				body.put("amount", rq.getAmount());
				
				Future<JsonObject> callFuture = webClient.callPostService(url, body);
				callFuture.setHandler(ar1->{
					if(ar1.succeeded()) {
						future.complete(ar1.result().mapTo(SendP2PResponse.class));
					}
					else {
						future.fail(ar1.cause());
					}
				});
			}
			else future.fail("wrong pin");
		}, Future.future().setHandler(ar->{
			future.fail(ar.cause());
		}));
		*/
		return future;
	}
	// vi du the nay :
	// neu ma lam ay d:
	
	
	public Future<Present> createLixi(CreatePresentRequest rq, String userId) {
		Future<Present> future= Future.future();
		Future<Boolean>  userInSessionFuture = redisCache.userInSession(userId, rq.getSessionId());
		userInSessionFuture.compose(result->{
			if(result) {
				
				Present present= new Present();
				present.setPresentId(GenerationUtils.generateId());
				
				present.setStartTime(System.currentTimeMillis());
				present.setUserId(userId);
				present.setSessionId(rq.getSessionId());
				present.setTotalAmount(rq.getAmount());
				
				JsonObject body = JsonObject.mapFrom(rq);
				body.put("userId", userId);
				body.put("presentId", present.getPresentId());
				
				Future<Present> insertPresentFuture = redisCache.insertLixi(present);
				insertPresentFuture.setHandler(ar->{
				if(ar.succeeded()) {
					Future<JsonObject> callFuture = webClient.callPostService("/createPresent", body);
					callFuture.compose(res->{
						if(res.getBoolean("code")) {
							future.complete(present);
							processMsgCreateLixi(present);
							
						}
						else {
							redisCache.removeLixi(present.getPresentId(), present.getSessionId());
							
							future.fail(res.getString("message"));
						}
					}, Future.future().setHandler(handler->{
						future.fail(handler.cause());
					}));
				}
				else future.fail(ar.cause());
				});

				
				
			}
			
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		
		return future;
	}
	
	public Future<GetPresentResponse> getLixi(GetPresentRequest rq,String userId){
		Future<GetPresentResponse> future = Future.future();
		String presentId = rq.getPresentId();
		String sessionId = rq.getSessionId();
		Future<Boolean> checkUserFuture = redisCache.userInSession(userId, sessionId);
		checkUserFuture.compose(result->{
			if(result) {
				
				Future<Boolean> checkUserGotFuture = redisCache.checkUserGotPresent(userId, presentId);
				checkUserGotFuture.compose(gotten->{
					if(gotten) future.fail("You have already receive this present");
					else {
						JsonObject body =new JsonObject();
						body.put("userId", userId);
						body.put("presentId", presentId);
						Future<JsonObject> callFuture = webClient.callPostService("/getPresent", body);
						callFuture.compose(res->{
							if(res.getBoolean("code")) {
								Long amount  = res.getLong("amount");
								GetPresentResponse response = new GetPresentResponse();
								
								response.setAmount(amount);
								response.setCode(1);
								response.setMessage(res.getString("message"));
								
								ReceiveLixi rec =new ReceiveLixi();
								rec.setPresentId(presentId);
								rec.setAmount(amount);
								rec.setSessionId(sessionId);
								rec.setUserId(userId);
								
								redisCache.insertReceiverLixi(rec);
								
								future.complete(response);
							}
							else {
								if(res.getLong("amount")==-1L) {
									future.complete(null);
									redisCache.removeLixi(presentId, sessionId);
								}
							}
						}, Future.future().setHandler(handler->{
							future.fail(handler.cause());
						}));
					}
				}, Future.future().setHandler(handler->{
					future.fail(handler.cause());
				}));
				
			}
		}, Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	public Future<List<PresentsOfSession>> getAllLixisOfUser(String userId){
		return redisCache.getLixisOfUser(userId);
	}
	
		
	public Future<RemovePresentResponse> removePresent(String presentId,String sessionId ,String userId){
		Future<RemovePresentResponse> future = Future.future();
		Future<Boolean> checkOwnerFuture = redisCache.isOwnerOfPresent(userId, presentId, sessionId);
		checkOwnerFuture.compose(result->{
			if(result) {
				Future<Void> removeFuture = redisCache.removePresent(presentId, sessionId);
				removeFuture.setHandler(ar->{
					if(ar.succeeded()) {
						RemovePresentResponse res = new RemovePresentResponse();
						res.setCode(true);
						res.setMessage("Returned reminder of present for you !");
						future.complete(res);
					}
					else future.fail(ar.cause());
				});
			}
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	public Future<List<TopupTransaction>> getTopupTransactions(GetTopupsRequest rq, String userId){
		Future<List<TopupTransaction>> future = Future.future();
		JsonObject obj = JsonObject.mapFrom(rq);
		obj.put("userId" , userId);
		System.out.println("qua day xu ly");
		Future<JsonObject> callFuture = webClient.callPostService("/getTopupsOfUser", obj);
		callFuture.setHandler(ar->{
			if(ar.succeeded()) {
			
				JsonObject result= ar.result();
				List<TopupTransaction> ls= result.getJsonArray("data").getList();
				future.complete(ls);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	
	Future<P2PTransaction> getP2PTransaction(JsonObject obj){
		Future<P2PTransaction> future = Future.future();
		String senderId = obj.getString("senderId");
		String receiverId = obj.getString("receiverId");
		
		Future<String> getSenderFutute = redisCache.getUsername(senderId);
		Future<String> getReceiverFuture = redisCache.getUsername(receiverId);
		CompositeFuture cp = CompositeFuture.all(getSenderFutute,getReceiverFuture);
		cp.setHandler(ar->{
			if(ar.succeeded()) {
				String sender = cp.resultAt(0);
				String receiver =cp.resultAt(1);
				P2PTransaction ts = new P2PTransaction();
				ts.setSender(sender); ts.setReceiver(receiver);
				ts.setTimestamp(obj.getLong("timestamp"));
				ts.setAmount(obj.getLong("amount"));
				ts.setTransactionId(obj.getLong("id"));
				future.complete(ts);
			}
		});
		return future;
	}
	
	public Future<List<P2PTransaction>> getP2PTransactions(GetP2PsRequest rq, String userId){
		Future<List<P2PTransaction>> future = Future.future();
		JsonObject body = JsonObject.mapFrom(rq);
		body.put("userId", userId);
		Future<JsonObject> callFuture = webClient.callPostService("/getP2PsOfUser", body);
		callFuture.compose(result->{
				JsonArray arr= result.getJsonArray("data");
				System.out.println("lay duoc data");
				if(arr.size()>0) {
					List<Future> getAllFutures = new ArrayList<>();
					for(int i=0;i<arr.size();i++) {
						JsonObject obj = arr.getJsonObject(i);
						//System.out.println("id at "+ i +obj.getLong("id"));
						Future<P2PTransaction> future1 = getP2PTransaction(arr.getJsonObject(i));
						getAllFutures.add(future1);
					}
					CompositeFuture cp = CompositeFuture.all(getAllFutures);
					cp.setHandler(ar1->{
						if(ar1.succeeded()) {
							List<P2PTransaction> results = new ArrayList<>();
							for(int i=0;i<arr.size();i++) {
								results.add(cp.resultAt(i));
							}
							future.complete(results);
						}
						else future.fail(ar1.cause());
					});
					}else {
						future.complete(null);
					}
			
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		
		return future;
	}
	
	
	public Future<Void> deleteAllPresent(){
		Future<Void> future = Future.future();
		Future<Void>  delFuture= redisCache.deleteAllpresent();
		delFuture.setHandler(ar->{
			if(ar.succeeded()) future.complete();
			else future.fail(ar.cause());
		});
		return future;
	}
	public Future<Void> deleteAllMsgPresent() {
		Future<Void> future  = Future.future();
		Future<Void> future1= redisCache.deleteAllMsgPresent();
		future1.setHandler(ar->{
			if(ar.succeeded()) future.complete();
			else future.fail(ar.cause());
		});
		return future;
	}
}	

