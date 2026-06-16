package com.quizmicroservice.security;

import com.quizmicroservice.service.AuthActivityService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener {

    private final AuthActivityService authActivityService;
    private final HttpServletRequest request;

    public AuthenticationFailureListener(AuthActivityService authActivityService,
                                         HttpServletRequest request) {
        this.authActivityService = authActivityService;
        this.request = request;
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String email = event.getAuthentication().getName();

        authActivityService.logFailedLogin(
                email,
                request,
                "Login failed: bad credentials."
        );
    }
}