package com.urbondo;

import dagger.Module;
import dagger.Provides;

@Module
public class HandlerModule {

    @Provides
    public PrintApiGatewayRequest providesPrintApiGatewayRequest() {
        return new PrintApiGatewayRequest();
    }
}
