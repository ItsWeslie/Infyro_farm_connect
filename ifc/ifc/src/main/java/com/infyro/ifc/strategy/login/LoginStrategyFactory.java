package com.infyro.ifc.strategy.login;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginStrategyFactory {

    private final Map<String,LoginStrategy> loginStrategies;

    public LoginStrategy getStrategy(String loginType) {

        LoginStrategy loginStrategy = loginStrategies.get(loginType);

        if (loginStrategy == null) {
            throw new IllegalArgumentException("Unknown login type: " + loginType);
        }
        return loginStrategy;
    }
}
