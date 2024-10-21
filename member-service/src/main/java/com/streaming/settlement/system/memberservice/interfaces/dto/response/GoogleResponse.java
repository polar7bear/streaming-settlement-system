package com.streaming.settlement.system.memberservice.interfaces.dto.response;

import java.util.Map;
import java.util.Optional;

public class GoogleResponse implements OAuth2Response {

    private final static String ID = "sub";
    private final static String NAME = "name";
    private final static String EMAIL = "email";
    private final static String PROVIDER = "google";
    private final Map<String, Object> attribute;

    public GoogleResponse(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    public String getProviderId() {
        return Optional.ofNullable(attribute.get(ID))
                .map(Object::toString)
                .orElse("");
    }

    @Override
    public String getEmail() {
        return Optional.ofNullable(attribute.get(EMAIL))
                .map(Object::toString)
                .orElse("");
    }

    @Override
    public String getName() {
        return Optional.ofNullable(attribute.get(NAME))
                .map(Object::toString)
                .orElse("");
    }
}
