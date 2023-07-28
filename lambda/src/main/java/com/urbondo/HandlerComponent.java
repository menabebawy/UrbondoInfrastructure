package com.urbondo;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = HandlerModule.class)
public interface HandlerComponent {
    PrintApiGatewayRequest buildPrintGatewayRequest();
}
