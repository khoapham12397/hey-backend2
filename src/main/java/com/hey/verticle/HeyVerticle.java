package com.hey.verticle;

import com.hey.api.ApiServer;
import com.hey.api.WsServer;
import com.hey.cache.client.RedisCacheClient;
import com.hey.cache.client.RedisCacheExtend;
import com.hey.handler.ProtectedApiHandler;
import com.hey.handler.PublicApiHandler;
import com.hey.handler.WalletPublicHandler;
import com.hey.handler.AuthenticationHandler;
import com.hey.handler.WsHandler;
import com.hey.manager.JwtManager;
import com.hey.manager.UserWsChannelManager;
import com.hey.repository.DataRepository;
import com.hey.service.APIService;
import com.hey.service.AuthenticationService;
import com.hey.service.WalletService;
import com.hey.util.PropertiesUtils;
import com.hey.webclient.MyWebClient;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HeyVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LogManager.getLogger(HeyVerticle.class);
    private ApiServer apiServer;
    private WsServer wsServer;
    private APIService apiService;
    private MyWebClient myClient;
    
    private WalletService walletService;
    private WalletPublicHandler walletPublicHandler;
    private RedisCacheExtend redisCache;
    public APIService getApiService() {
    	return this.apiService;
    }
    public ApiServer getApiServer() {
        return apiServer;
    }

    public WsServer getWebsocketServer() {
        return wsServer;
    }
    
    @Override
    public void start(Future<Void> future) {
    	System.out.println("Starting ...");
        LOGGER.info("{} verticle {} start", deploymentID(), Thread.currentThread().getName());

        // Create a JWT Auth Provider
        LOGGER.info("Initial JWT for verticle {}", Thread.currentThread().getName());
        JwtManager jwtManager = new JwtManager(vertx);

        //Inject dependency
        LOGGER.info("Starting Inject Dependency for verticle {}", Thread.currentThread().getName());
        System.out.println("Redist host: "+PropertiesUtils.getInstance().getValue("redis.host"));
        RedisClient client = RedisClient.create(vertx,
                new RedisOptions().setHost(PropertiesUtils.getInstance().getValue("redis.host")));
        // dau tien no chua co hinh thanh lien ket hay bat cu thu gi ??
        
        DataRepository repository = new RedisCacheClient(client);
        
        // User Channel Manager
        UserWsChannelManager userWsChannelManager = new UserWsChannelManager();
        userWsChannelManager.setEventBus(vertx.eventBus());
        userWsChannelManager.setSharedData(vertx.sharedData());
        // dung la tu day 2 cai thang nay no dung cai nay de ma lam ???
        // cai viec config nay cung kha dac biet dug vya :
        
        // API Service
        apiService = new APIService();
        apiService.setDataRepository(repository);
        apiService.setUserWsChannelManager(userWsChannelManager);
        
        // Authentication Service
        AuthenticationService authenticationService = new AuthenticationService();
        authenticationService.setDataRepository(repository);
        authenticationService.setJwtManager(jwtManager);

        // Protected API Handler
        ProtectedApiHandler protectedApiHandler = new ProtectedApiHandler();
        protectedApiHandler.setDataRepository(repository);
        protectedApiHandler.setJwtManager(jwtManager);
        protectedApiHandler.setApiService(apiService);

        // Public API Handler
        PublicApiHandler publicApiHandler = new PublicApiHandler();
        publicApiHandler.setDataRepository(repository);
        publicApiHandler.setApiService(apiService);

        // Authentication Handler
        AuthenticationHandler authenticationHandler = new AuthenticationHandler();
        authenticationHandler.setDataRepository(repository);
        authenticationHandler.setWebService(authenticationService);

        // Web Socket Handler
        WsHandler wsHandler = new WsHandler();
        wsHandler.setDataRepository(repository);
        wsHandler.setApiService(apiService);
        wsHandler.setUserWsChannelManager(userWsChannelManager);
       
        // API Server
        this.apiServer = ApiServer.newInstance();
        apiServer.setProtectedApiHandler(protectedApiHandler);
        apiServer.setPublicApiHandler(publicApiHandler);
        apiServer.setWebHandler(authenticationHandler);

        // Web Socket Server
        this.wsServer = WsServer.newInstance();
        wsServer.setWsHandler(wsHandler);
        wsServer.setUserWsChannelManager(userWsChannelManager);
        wsServer.setJwtManager(jwtManager);

        LOGGER.info("Inject Dependency successfully for verticle {}", Thread.currentThread().getName());
      
        myClient= new MyWebClient(vertx);
        redisCache =new RedisCacheExtend(client);
        walletService = new WalletService(redisCache, myClient);
        walletService.setUserWsChannelManager(userWsChannelManager);
        walletPublicHandler =new WalletPublicHandler(walletService);
        walletPublicHandler.setChanelManager(userWsChannelManager);
        walletPublicHandler.setJwtManager(jwtManager);
        walletPublicHandler.setRedisCache(redisCache);
        apiServer.setWalletPublicHandler(walletPublicHandler);
        wsHandler.setRedisCache(redisCache);
        redisCache.setWebClient(myClient);
        walletService.setWsHandler(wsHandler);
        apiService.setWebClient(myClient);
        
        
        Future.succeededFuture()
                .compose(v -> apiServer.createHttpServer(vertx))
                .compose(v -> wsServer.createWsServer(vertx))
               
                .setHandler(future);
        vertx.setPeriodic(10*60*1000, ar->{
        	redisCache.checkPresentsExpiredAndDelete();
        });
       
    }
   
    
    
    @Override
    public void stop() {
        LOGGER.info("Shutting down application");
    }
}
