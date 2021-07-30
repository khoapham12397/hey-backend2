package com.hey.api;

import com.hey.handler.ProtectedApiHandler;
import com.hey.handler.PublicApiHandler;
import com.hey.handler.WalletProtectedHandler;
import com.hey.handler.WalletPublicHandler;
import com.hey.handler.AuthenticationHandler;
import com.hey.util.PropertiesUtils;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CSRFHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public final class ApiServer {
    private HttpServer httpServer;
    private ProtectedApiHandler protectedApiHandler;
    private PublicApiHandler publicApiHandler;
    private AuthenticationHandler authenticationHandler;
    private WalletPublicHandler walletPublicHandler;
    private WalletProtectedHandler walletProtectedHandler;
    private CSRFHandler csrfHandler;
    private static final Logger LOGGER = LogManager.getLogger(ApiServer.class);

    private ApiServer() {

    }
    
    
    public static ApiServer newInstance() {
        ApiServer apiServer = new ApiServer();
        return apiServer;
    }

    public Future<Void> createHttpServer(Vertx vertx) {
        if (httpServer != null) Future.succeededFuture();
        LOGGER.info("Starting API Server ...");

        Router router = Router.router(vertx);
        // thuc te la gi ??? handler cai routing context nay no la cai gi ??
        // no cung la 1 cai gi do ??
        // ma minh cung khong hieu ro lam dung vay d:
        // ban chat cua no chinh la set up cho cai response dung vay :
        // sau do con nhung cai khac no mang tinh config
        
        router.route("/").handler(routingContext -> {
            
        	HttpServerResponse httpServerResponse = routingContext.response();
            httpServerResponse
                    .putHeader("content-type", "text/html")
                    .end("<h1>Helloworld</h1>");

        });

        router.route().handler(BodyHandler.create());

        Set<String> allowedHeaders = new HashSet<>();
        allowedHeaders.add("x-requested-with");
        allowedHeaders.add("Access-Control-Allow-Origin");
        allowedHeaders.add("origin");
        allowedHeaders.add("accept");
        allowedHeaders.add("Content-Type");
        allowedHeaders.add("Authorization");
        // dau tien can xac dinh cai authorization cua no :
        // dung vay do :
        // sau do the nao ?? dung la vya do
        
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.add(HttpMethod.DELETE);
        allowedMethods.add(HttpMethod.PATCH);
        allowedMethods.add(HttpMethod.PUT);
        //router.route().handler(CSRFHandler.create("not a good secret"));
        //csrfHandler = CSRFHandler.create("my secret");
      
        //router.post("/*").handler(CSRFHandler.create("not a good secret"));
        //router.route().handler(CookieHandler.create());
       
        //http://localhost:3000
        router.route("/*").handler(CorsHandler.create(".*.").allowedHeaders
                (allowedHeaders).allowedMethods(allowedMethods).allowCredentials(true))
                .handler(BodyHandler.create());
        
        
        router.get("/inittestdata"	).handler(authenticationHandler::initTestData);
      
        router.post("/signin").handler(authenticationHandler::signIn);
        router.post("/signout").handler(authenticationHandler::signOut);

        router.route("/api/protected/*").handler(protectedApiHandler::handle);
        router.route("/api/wallet/public/*").handler(walletPublicHandler::handle);
        router.route("/api/wallet/protected/*").handler(walletProtectedHandler::handle);

        router.post("/api/public/*").handler(publicApiHandler::handle);
        
        Future future = Future.future();
        httpServer = vertx
                .createHttpServer()
                .requestHandler(router::accept)
                .exceptionHandler(exHandler -> {
                    LOGGER.error(exHandler.getCause());
                })
                .listen(
                        PropertiesUtils.getInstance().getIntValue("api.port"),
                        ar -> {
                            if (ar.succeeded()) {
                                LOGGER.info("API Server start successfully !");
                                System.out.println();
                                
                                future.complete();
                                
                            } else {
                                LOGGER.error("API Server start fail. Reason: {}", ar.cause().getMessage());
                                future.fail(ar.cause());
                            }
                        }
                );

        return future;
    }

    public WalletPublicHandler getWalletPublicHandler() {
		return walletPublicHandler;
	}


	public void setWalletPublicHandler(WalletPublicHandler walletPublicHandler) {
		this.walletPublicHandler = walletPublicHandler;
	}


	public WalletProtectedHandler getWalletProtectedHandler() {
		return walletProtectedHandler;
	}


	public void setWalletProtectedHandler(WalletProtectedHandler walletProtectedHandler) {
		this.walletProtectedHandler = walletProtectedHandler;
	}


	public void setProtectedApiHandler(ProtectedApiHandler protectedApiHandler) {
        this.protectedApiHandler = protectedApiHandler;
    }

    public void setPublicApiHandler(PublicApiHandler publicApiHandler) {
        this.publicApiHandler = publicApiHandler;
    }

    public void setWebHandler(AuthenticationHandler authenticationHandler) {
        this.authenticationHandler = authenticationHandler;
    }

    public ProtectedApiHandler getProtectedApiHandler() {
        return protectedApiHandler;
    }

    public PublicApiHandler getPublicApiHandler() {
        return publicApiHandler;
    }

    public AuthenticationHandler getWebHandler() {
        return authenticationHandler;
    }
}
