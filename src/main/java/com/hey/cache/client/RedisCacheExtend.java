package com.hey.cache.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hey.model.User;
import com.hey.util.GenerationUtils;
import com.hey.util.PropertiesUtils;
import com.hey.walletmodel.Authen;
import com.hey.walletmodel.CreatePresentRequest;
import com.hey.walletmodel.GetPresentResponse;
import com.hey.walletmodel.LixisOfSession;
import com.hey.walletmodel.P2PTransaction;
import com.hey.walletmodel.Present;
import com.hey.walletmodel.PresentsOfSession;
import com.hey.walletmodel.Profile;
import com.hey.walletmodel.ReceiveLixi;
import com.hey.walletmodel.WalletResponse;
import com.hey.webclient.MyWebClient;

import io.vertx.core.AsyncResult;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.op.ScanOptions;

public class RedisCacheExtend  {
	
	private RedisClient client;
	private Integer numScanCount;
	private MyWebClient webClient;
	
	
	
	public MyWebClient getWebClient() {
		return webClient;
	}

	public void setWebClient(MyWebClient webClient) {
		this.webClient = webClient;
	}
	
	public RedisCacheExtend(RedisClient client) {
		this.client = client;
        numScanCount = Integer.parseInt(PropertiesUtils.getInstance().getValue("scan.count"));

	}
	
	private String generateProfileKey(String userId) {
		return "user_profile:"+userId;
	}
	
	String generateUserAuthKey(String userName) {
        return "user:" + userName;
    }
	
	private String generateWalletKey(String walletId) {
		return "wallet:"+walletId;
	}
	
	private String generateAuthenKey(String userId) {
		return "authenticate:"+userId;
	}
	
	private String generateLixiKey(String presentId, String sessionId) {
		return "lixi:"+presentId+":"+sessionId;
	}
	private String generateReceiveLixiKey(String presentId, String userId) {
		return "receive_lixi:"+presentId+":"+userId;
	}
	private String generateChatPairKey(String userId1, String userId2) {
		return "chat_pair:"+userId1+":"+userId2;
	}
	
	// trong vi thi co 2 so dung vay cu set ay di:
	/// duoc :
	
	
	public Future<String> getUsername(String userId){
		Future<String> future = Future.future();
		client.hget("user_full:"+userId, "user_name", ar->{
			if(ar.succeeded()) future.complete(ar.result());
			else future.fail(ar.cause());
		});
		return future;
		
	}
	
	
	
	public void setHashedPassword(String hashedPassword, String userId) {
		client.hget("user_full:"+userId, "user_name", ar->{
			if(ar.succeeded()) {	
				String key = generateUserAuthKey(ar.result());
				client.hset(key, "hashed_password", hashedPassword, ar1->{
					if(ar1.succeeded()) {
						
					}else throw new RuntimeException(ar1.cause());
				});
			}
			else throw new RuntimeException(ar.cause());
		});
	}
	
	public Future<Boolean> existWallet(String userId){
		Future<Boolean> future  = Future.future();
		client.exists(generateWalletKey(userId), ar->{
			if(ar.succeeded()) {
				if(ar.result()>0) future.complete(true);
				else future.complete(false);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	public void setHashedPin(String hashedPin, String userId) {
		String key = generateWalletKey(userId);
		client.hset(key, "hashed_pin", hashedPin, ar->{
			if(ar.succeeded()) {
				
			}
			else throw new RuntimeException(ar.cause());
		});
	}
	
	
	public void insertProfile(Profile profile, String userId){
		JsonObject obj = new JsonObject();
		obj.put("full_name", profile.getFullName());
		obj.put("address", profile.getAddress());
		obj.put("gender", profile.getGender());
		obj.put("phone", profile.getPhone());
		obj.put("identity", profile.getIdentityNumber());
		
		String key = generateProfileKey(userId);
		client.hmset(key, obj, ar->{System.out.println("Add new Profile to cache :"+ key);});
	}
	
	public Future<Profile> getProfile(String userId){
		Future<Profile> future = Future.future();
		client.hgetall(generateProfileKey(userId), ar ->{
			if(ar.succeeded()) {
				
				JsonObject obj = ar.result();
				if(obj.getString("full_name")!=null) {
					
					Profile profile= new Profile();
					profile.setFullName(obj.getString("full_name"));
					profile.setPhone(obj.getString("phone"));
					profile.setPhone(obj.getString("address"));
					profile.setIdentityNumber(obj.getString("identity"));
					profile.setGender(Boolean.valueOf(obj.getString("gender")));
					future.complete(profile);
				}else {
					future.complete(null);
				}
			}else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<Authen> getAuthen(String userId){
		
		Future<Authen> future = Future.future();
		//RedisClient client =this.getClient();
		client.hgetall(generateAuthenKey(userId), ar->{
			if(ar.succeeded()) {
				JsonObject obj = ar.result();
				if(obj.getString("hashed_password") != null) {
					Authen auth =new Authen();
					auth.setHashedPass(obj.getString("hashed_password"));
					auth.setHashedPin(obj.getString("hashed_pin"));
					future.complete(auth);
				}else future.complete(null);
			}else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<WalletResponse> getWallet(String userId){
		
		Future<WalletResponse> future  = Future.future();
		//RedisClient client= this.getClient();
		client.hgetall(generateWalletKey(userId), ar->{
			if(ar.succeeded()) {
				JsonObject obj = ar.result();
				if(obj.getString("balance")!=null) {
					WalletResponse wallet = new WalletResponse();
					wallet.setBalance(Long.valueOf(obj.getString("balance")));
					wallet.setHashedPin(obj.getString("hashed_pin"));
					
					future.complete(wallet);
				}else future.complete(null);
			}else future.fail(ar.cause());
		});
		
		return future;
	}

	public Future<List<String>> getKeysByPattern(String keyPattern) {
	        Future<List<String>> future = Future.future();
	        List<String> keys = new ArrayList<>();
	       
	        ScanOptions scanOptions = new ScanOptions();
	        scanOptions.setCount(numScanCount);
	        scanOptions.setMatch(keyPattern);
	       
	        
	        client.scan("0", scanOptions, res -> {
	            if (res.succeeded()) {

	                JsonArray jsonArray = (JsonArray)res.result().getList().get(1);
	                jsonArray.forEach(object -> {
	                    if (object instanceof String) {
	                        keys.add((String) object);
	                    }
	                });

	                future.complete(keys);

	            }else {
	                future.fail(res.cause());
	            }

	        });
	      
	        //StringUtils.contains("name", searchSeq)
	        return future;
	}
	
	public Future<Present> insertLixi(Present present){
		Future<Present> future  = Future.future();
		
		String presentId = present.getPresentId();
		String presentKey = generateLixiKey(presentId, present.getSessionId());
		
		
		JsonObject obj = new JsonObject();
		
		obj.put("user_id", present.getUserId());
		obj.put("total_amount", present.getTotalAmount());
		obj.put("start_time", present.getStartTime());
		
		client.hmset(presentKey, obj, ar->{
			if(ar.succeeded()) {
				future.complete(present);
			}
			else {
				future.fail(ar.cause());
			}
		});
		
		return future;
	}
	
	public Future<Boolean> checkUserGotPresent(String userId, String presentId){
		Future<Boolean> future = Future.future();
		
		String pattern = generateReceiveLixiKey(presentId, userId);
		client.exists(pattern, ar->{
			if(ar.succeeded()) {
				if(ar.result()==0L) future.complete(false);
				else future.complete(true);
			}
			else future.fail(ar.cause());
		});
	
		return future;
	}

	public void removeLixi(String presentId, String sessionId){
		client.del(generateLixiKey(presentId, sessionId), ar->{
			if(ar.succeeded()) {
				System.out.println("remove lixi present "+presentId);
			}
		});
	}
	
	public Future<Boolean> insertReceiverLixi(ReceiveLixi rec){
		
		Future<Boolean> future = Future.future();

		JsonObject obj = new JsonObject();
		obj.put("amount", rec.getAmount());
		obj.put("timestamp", System.currentTimeMillis());
		
		String key = generateReceiveLixiKey(rec.getPresentId(), rec.getUserId());
		
		client.hmset(key, obj, ar->{
			if(ar.succeeded()) {
				System.out.println("set nhan li xi: "+ rec.getUserId()+" present: "+ rec.getPresentId());
				future.complete(true);
			}
			else future.fail(ar.cause());
		});
		
		return future;
	}
	public Future<Boolean> userInSession(String userId, String sessionId){
		Future<Boolean> future = Future.future();
		Future<List<String>> getKeysFuture = getKeysByPattern("chat:list:"+sessionId+"*");
		getKeysFuture.compose(result->{
			if(result!=null && result.size()>0) {
				String x = result.get(0);
				if(x.contains(userId)) future.complete(true);
				else future.complete(false);
			}
			else future.complete(false);
		}, Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	public Future<Present> getPresent(String presentId, String sessionId){
		Future<Present> future = Future.future();
		
		client.hgetall(generateLixiKey(presentId, sessionId), ar->{
			if(ar.succeeded()) {
				JsonObject obj = ar.result();
				if(obj!=null && obj.getString("user_id")!=null) {
					//System.out.println("find out present object");
					Present p = new Present();
					p.setPresentId(presentId);
					p.setSessionId(sessionId);
					//p.setCurrentAmount(Long.valueOf(obj.getString("current_amount")));
					p.setTotalAmount(Long.valueOf(obj.getString("total_amount")));
					p.setStartTime(Long.valueOf(obj.getString("start_time")));
					p.setUserId(obj.getString("user_id"));
				
					future.complete(p);
				}
				else {
					future.complete(null);
				}
			}
			else future.fail(ar.cause());
		});
		
		return future;
	}
	
	public void insertWallet(String hashedPin, String userId) {
		String key = generateWalletKey(userId);
		JsonObject obj = new JsonObject();
		obj.put("hashed_pin", hashedPin);
		obj.put("balance", 0);
		client.hmset(key, obj, ar->{
			if(!ar.succeeded()) throw new RuntimeException(ar.cause()); 
		});
	}
	
	public void deletePresent(String presentKey) {
		String[] arr = presentKey.split(":");
		String presentId = arr[1];
		deleteRecevieLixis(presentId);		
		client.del(presentKey,ar->{
			//System.out.println("delete success "+ presentKey);
			if(ar.succeeded()) System.out.println("delete success " +presentKey);
		});
	}
	
	public void deleteRecevieLixis(String presentId) {
		String pattern = "receive_lixi:"+presentId+":*";
		Future<List<String>> getKeysFuture = getKeysByPattern(pattern);
		getKeysFuture.compose(keys->{
			if(keys!=null && keys.size()>0) {
				for(String key : keys) {
					client.del(key,ar1->{});
				}
			}
		},Future.future().setHandler(handler->{
			
		}));
 	}
	
	public void checkPresent(String presentKey) {
		
		client.hgetall(presentKey, ar->{
			if(ar.succeeded()) {
				JsonObject obj = ar.result();
				Long time = Long.valueOf(obj.getString("start_time"));
				Long del  = System.currentTimeMillis()-time;
				
				if(del >= 24*1000*60*60) {
					
					String[] arr= presentKey.split(":");
					String presentId = arr[1];
					JsonObject body = new JsonObject();
					
					body.put("presentId",presentId );
					
					Future<JsonObject> callFuture = webClient.callPostService("/removePresent",body);
					callFuture.setHandler(ar1->{
						if(ar1.succeeded()) {
							if(ar1.result().getBoolean("code")) {
								deletePresent(presentKey);
							}
						}
						else throw new RuntimeException(ar1.cause());
					});
				}
			}
			else throw new RuntimeException(ar.cause());
		});
	}
	
	
	public void checkPresentsExpiredAndDelete(){
		
		Future<List<String>> getKeysFuture = getKeysByPattern("lixi:*");
		
		getKeysFuture.compose(keys->{
			
			for(int i=0;i<keys.size();i++) {
				checkPresent(keys.get(i));
			}
		},Future.future().setHandler(handler->{
			
		}));
	}
	/*
	public Future<List<String>> getLixisOfSession(String sessionId){
		Future<List<String>> future = Future.future();
		String pattern = "lixi:*:"+sessionId;
		Future<List<String>> getKeysFuture = getKeysByPattern(pattern);
		getKeysFuture.compose(keys->{
			
			List<String> results  = new ArrayList<>();
			for(String key : keys) {
				String presentId = key.split(":")[1];
				results.add(presentId);
			}
			future.complete(results);
			
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	*/
	public Future<List<Present>> getPresentsOfSession(String sessionId){
		Future<List<Present>> future = Future.future();
		String pattern = "lixi:*:"+sessionId;
		Future<List<String>> getKeysFuture = getKeysByPattern(pattern);
		getKeysFuture.compose(keys->{
			
			List<Future> getPresentFutures  = new ArrayList<>();
			for(String key : keys) {
				String presentId = key.split(":")[1];
				Future<Present> getPrFuture = getPresent(presentId, sessionId);
				getPresentFutures.add(getPrFuture);
			}
			CompositeFuture cp = CompositeFuture.all(getPresentFutures);
			cp.setHandler(ar->{
				if(ar.succeeded()) {
					List<Present> results = new ArrayList<>();
					for(int i=0;i<keys.size();i++) 
						results.add(cp.resultAt(i));
					future.complete(results);
				}
				else {
					future.fail(ar.cause());
				}
			});
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	
	public Future<List<PresentsOfSession>> getLixisOfUser(String userId){
		Future<List<PresentsOfSession>> future = Future.future();
		String pattern = "chat:list:*"+":"+ userId+"*";
		Future<List<String>> getKeysFuture = getKeysByPattern(pattern);
		getKeysFuture.compose(keys->{
			if(keys!=null && keys.size()>0) {
				
				List<Future> getLixiFutures = new ArrayList<>();
				
				for(String key : keys) {
					String sessionId = key.split(":")[2];
					Future<List<Present>> getLixiFuture = getPresentsOfSession(sessionId);
					getLixiFutures.add(getLixiFuture);
				}
				CompositeFuture cp = CompositeFuture.all(getLixiFutures);
				cp.setHandler(ar->{
					if(ar.succeeded()) {
						List<PresentsOfSession> result = new ArrayList<>();
						for(int i=0;i<keys.size();i++) {
							PresentsOfSession item = new PresentsOfSession();
							item.setSessionId(keys.get(i).split(":")[2]);
							item.setPresent(cp.resultAt(i));
							result.add(item);
						}
						future.complete(result);
					}
					else {
						future.fail(ar.cause());
					}
				});
				
			}
		}, Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	public Future<Void> removePresent(String presentId, String sessionId){
		Future<Void> future = Future.future();
		JsonObject obj = new JsonObject();
		obj.put("presentId", presentId);
		Future<JsonObject> callFuture = webClient.callPostService("/removePresent", obj);
		callFuture.compose(result->{
			if(result.getBoolean("code")) {
				deletePresent(generateLixiKey(presentId, sessionId));
				
				future.complete();
			}
		},Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
	}
	
	public Future<Boolean> isOwnerOfPresent(String userId, String presentId, String sessionId){
		Future<Boolean> future = Future.future();
		client.hget(generateLixiKey(presentId, sessionId), "user_id", ar->{
			if(ar.succeeded()) {
				if(ar.result()==null) {
					future.fail("The present is not exist");
				}
				else if(ar.result().equals(userId))  future.complete(true);
			 	else future.complete(false);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<String> findSessionIdTwoPerson(String userId1 ,String userId2){
		Future<String> future = Future.future();
		String patt1 = "chat:list:*:"+userId1+":"+userId2; 
		String patt2 = "chat:list:*" +userId2+":"+userId1;
		Future<List<String>>  getKeysFuture1= getKeysByPattern(patt1);
		Future<List<String>>  getKeysFuture2= getKeysByPattern(patt2);
		
		CompositeFuture cp = CompositeFuture.all(getKeysFuture1,getKeysFuture2);
		cp.setHandler(ar->{
			if(ar.succeeded()) {
			 	List<String> keys = cp.resultAt(0);
			 	keys.addAll(cp.resultAt(1));
			 	for(String k : keys) {
			 		if(k.length()==120) {
			 			String sessionId = k.split(":")[2];
			 			future.complete(sessionId);
			 			return;
			 		}
			 	}
			 	future.complete(null);
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<String> findUsernameById(String userId){
		Future<String> future = Future.future();
		client.hget("user_full:"+userId,"user_name", ar->{
			if(ar.succeeded()) future.complete(ar.result());
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<String> getUserIdByUsername(String username){
		Future<String> future = Future.future();
		System.out.println("Find username :" +username);
		client.hget("user:"+username,"user_id", ar->{
			if(ar.succeeded()) future.complete(ar.result());
			else future.fail(ar.cause());
		});
		return future;
	}
	
	
	
	public Future<Void> insertChatPair(String sessionId, String userId1 ,String userId2){
		Future<Void> future =Future.future();
		String key= generateChatPairKey(userId1, userId2);
		JsonObject obj = new JsonObject();
		obj.put("session_id", sessionId);
 		client.hmset(key,obj,ar->{
 			if(ar.succeeded()) {
 				future.complete();
 			}
 			else future.fail(ar.cause());
 		});
		return future;
	}
	
	public Future<String> getSessionIdChatPair(String userId1, String userId2){
		Future<String> future = Future.future();
		client.hget(generateChatPairKey(userId1, userId2), "session_id", ar->{
			if(ar.succeeded()) {
				if(ar.result()!=null) future.complete(ar.result());
				else {
					client.hget(generateChatPairKey(userId2, userId1), "session_id", ar1->{
						if(ar1.succeeded()) {
							future.complete(ar1.result());
						}
						else future.fail(ar1.cause());
					});
				}
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	public Future<Void> delete1Present(String presentKey){
		Future<Void> future = Future.future();
		client.del(presentKey, ar->{
			if(ar.succeeded()) future.complete();
			else future.fail(ar.cause());
		});
		return future;
	}
	public Future<Void> deleteAllpresent(){
		Future<Void> future = Future.future();
		Future<List<String>> keysFuture = getKeysByPattern("lixi:*");
		keysFuture.compose(keys->{
			List<Future> delFutures=  new ArrayList<>();
			for(String key : keys) {
				delFutures.add(delete1Present(key));
			}
			CompositeFuture cp = CompositeFuture.all(delFutures);
			
			cp.setHandler(ar->{
				if(ar.succeeded()) 
					future.complete();
				else future.fail(ar.cause());
			});
			
		}, Future.future().setHandler(handler->{
			future.fail(handler.cause());
		}));
		return future;
		
	}
	public Future<Void> checkAndDelMsg(String key){
		Future<Void> future = Future.future();
		client.hget(key, "type", ar->{
			if(ar.succeeded()) {
				String val = ar.result();
				if(val!=null && val.equals("2")) {
					client.del(key, ar1->{
						if(ar1.succeeded()) {
							System.out.println("delete msg "+ key);
							future.complete();
						}
						else future.fail(ar1.cause());
					});
				}else future.complete();
			}
			else future.fail(ar.cause());
		});
		return future;
	}
	
	public Future<Void> deleteAllMsgPresent() {
		Future<Void> future = Future.future();
		Future<List<String>> getKeysFuture = getKeysByPattern("chat:message:*"); 
		getKeysFuture.compose(keys->{	
			List<Future> lst = new ArrayList<>();
			for(String key : keys) {
				lst.add(checkAndDelMsg(key));
			}
			CompositeFuture cp = CompositeFuture.all(lst);
			cp.setHandler(ar->{
				if(ar.succeeded()) {
					//System.out.println("Delete success all "+ keys.size()+" msg ");
					future.complete();
				}
				else throw new RuntimeException(ar.cause());
			});
		},Future.future().setHandler(handler->{
			
			throw new RuntimeException(handler.cause());
		}));
		return future;
	}
}