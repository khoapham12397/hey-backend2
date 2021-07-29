package com.hey.manager;

import com.hey.Main;
import com.hey.util.PropertiesUtils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.jwt.impl.JWTUser;
import io.vertx.ext.jwt.JWTOptions;

import java.util.Set;

public class JwtManager {
    private SharedData sharedData;
    private JWTAuth authProvider;
    private JWTOptions jwtOptions;
    private static final String JWT_ASYNC_MAP = "jwt-async-map";
    private static final String JWT_BLACKLIST_KEY = "blacklist-key";
    // tai day co 1 thang la 
    // khao sat lai o day coi no la cai gi ??
    // khao sat kha la ro con lai la viec ma minh dinh hinh cai code cua vertx ??/
    
    // dung vay do ma just do it:
    public JwtManager(Vertx vertx) {
    	// tai day no vao 
        this.sharedData = vertx.sharedData();
        // van de la trong vertx nay no co cai shareData =??
        // thuc te no la cai gi ??
        authProvider = JWTAuth.create(vertx, new JWTAuthOptions()
                .setKeyStore(new KeyStoreOptions()
                        .setType(PropertiesUtils.getInstance().getValue("jwt.keystore.type"))
                        .setPassword(PropertiesUtils.getInstance().getValue("jwt.keystore.password"))
                        .setPath(Main.RESOURCE_PATH + PropertiesUtils.getInstance().getValue("jwt.keystore"))));

        jwtOptions = new JWTOptions()
                .setIssuer(PropertiesUtils.getInstance().getValue("jwt.iss"))
                .addAudience(PropertiesUtils.getInstance().getValue("jwt.aud"))
                .setExpiresInSeconds(PropertiesUtils.getInstance().getIntValue("jwt.expire"));
    }
    // dung la no co cai async call dung ya /l
    
    // tu do the nao van de la vay don
    
    
    // cai thang redis luon la vay od :
    // no khong he change lau ?
    // dung roi do :
    // sau do the nao ?/
    
    // ro rang thang nay no can 1 thang handler dung vay 
    // ma no handler cai gin ??
    // van de nay kha la dac biet do nha:
    // dung ay :
    // cai dieu nay cung hay o cho do la cai gi ??
    // do la viec thay vi dung call back thi cai cach goi cua no cung kha la dac biet dung:
    // tuc la khi ma handle cai event la co duoc cai result cua callback thi no dung cai do:
    // de ma lam cai nay hoac la sao ??
    // sau khi xac dinh duoc cai result of event thi ok thoi dung vay :
    // no lam cai nay dac biet cuc luon:
    // cai result handler nay no co the lam duoc cai gi ???
    // dung ay 
    
    public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
        String jwt = authInfo.getString("jwt");
        checkForExistingAsynMap(jwt).setHandler(event -> {
            if (event.result()) {
                resultHandler.handle(Future.failedFuture("Token has been blacklist"));
            } else {
                authProvider.authenticate(authInfo, resultHandler);
            }
        });
        // nhin cho nay no la the nay ne  :// sua khi ma thang kia no lam gi do :
        // thi ro rang la cai gi xay ra ????
        // dung vay do:
        // cai nay lieu co dung ??/
        // ok
        // co 1 cai request dau tien dang login ??? // thi soa ??
        
    }
    // nhieu qua no cung hoi met day :
    // dung la vay do ma just di t
    
    // ro rang no cung su dung may cai khac biet de lam cai nay kha la dac biet do :
    // dung chua ?? dung la vay do ma just do it and something else:
    // ok chua dung a :
    // bi push vao  trong do don gian ??
    
    public void blacklistToken(String token, String userId, long ttl) {
        putToAsynMap(token, userId, ttl);
    }

    public String generateToken(String userId) {
        JsonObject userObj = new JsonObject()
                .put("userId", userId);
        return authProvider.generateToken(userObj, jwtOptions);
    }
    // khi ma yeu cau no gen 1 cai nay thi no co co che de ma xu ly roi
    
    private void putToAsynMap(String token, String userId, long ttl) {
        sharedData.getAsyncMap(JWT_ASYNC_MAP, event -> {
            AsyncMap<Object, Object> aMap = event.result();
            aMap.put(token, userId, ttl, AsyncResult::mapEmpty);
        });
    }
    // cai thang handler do no lam cai g ??/
    // dung la  ay do :
    // cai jwt cua moi 1 thang la cai mapping userid => voi jwt ->
    // dung roi do :
    
    private Future<Boolean> checkForExistingAsynMap(String token) {
        Future<Boolean> future = Future.future();
        sharedData.getAsyncMap(JWT_ASYNC_MAP, event -> {
            AsyncMap<Object, Object> aMap = event.result();
            aMap.get(token, event2 -> {
                if (event2.result() != null) {
                    future.complete(true);
                } else {
                    future.complete(false);
                }
            });
        });
        return future;
    }

    public void setSharedData(SharedData sharedData) {
        this.sharedData = sharedData;
    }

    public JWTAuth getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(JWTAuth authProvider) {
        this.authProvider = authProvider;
    }
}
// ban than future van co the la cai handler ??
// biet vi sao ko ???
// dau tien minh nen hieu duoc cai luong chay cua no cai dA:
// sau do moi xac dinh duoc nhieu cai khac