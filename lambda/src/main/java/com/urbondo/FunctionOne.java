package com.urbondo;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class FunctionOne implements RequestHandler<Void, String> {

    @Override
    public String handleRequest(Void input, Context context) {
        return "Mena Bebawy";
    }
}