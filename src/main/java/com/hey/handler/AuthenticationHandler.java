package com.hey.handler;

import com.hey.service.AuthenticationService;
import com.hey.util.HttpStatus;
import com.hey.util.JsonUtils;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AuthenticationHandler extends BaseHandler{

    private static final Logger LOGGER = LogManager.getLogger(AuthenticationHandler.class);

    private AuthenticationService authenticationService;
    
    
    public void setWebService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }
    
    public AuthenticationService getWebService() {
        return authenticationService;
    }

    public void signIn(RoutingContext routingContext) {
        String requestJson = routingContext.getBodyAsString();
        Future<JsonObject> signInFuture = authenticationService.signIn(requestJson);
        // get cai routingcontext: => 
        signInFuture.compose(jsonObject -> {

                routingContext.response()
                        .setStatusCode(HttpStatus.OK.code())
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(JsonUtils.toSuccessJSON(jsonObject));

        }, Future.future().setHandler(handler -> {
            handleException(handler.cause(), routingContext.response());
        }));

    }
  
    
    public void signOut(RoutingContext routingContext) {
        authenticationService.signOut(routingContext);
    }

    public void initTestData(RoutingContext routingContext) {

        Future<JsonObject> futureInitTestData = authenticationService.initTestData();

        futureInitTestData.compose(jsonObject -> {

            routingContext.response()
                    .setStatusCode(HttpStatus.OK.code())
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(JsonUtils.toSuccessJSON(jsonObject));

        }, Future.future().setHandler(handler -> {
            handleException(handler.cause(), routingContext.response());
        }));

    }

}
// xac dinh lai no set jwt o dau ???
