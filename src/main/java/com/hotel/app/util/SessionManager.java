package com.hotel.app.util;

import com.hotel.app.entity.User;

import java.util.Collections;
import java.util.List;

public class SessionManager{
    private static SessionManager sessionManager;
    private User user;
    private List<String> roles;

    private SessionManager(){

    }

    public static SessionManager getInstance(){
        if(sessionManager == null){
            sessionManager = new SessionManager();
        }
        return sessionManager;
    }

    public void login(User user, List<String> roles){
        if(user == null){
            throw new IllegalStateException("User cannot be null");
        }

        this.user = user;
        this.roles = roles != null ? roles : Collections.emptyList();
    }

    public void logout(){
        user = null;
        roles = null;
    }

    public User getCurrentUser(){
        return this.user;
    }

    public List<String> getRoles(){
        return roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
    }

    public boolean isLoggedIn(){
        return ! (user == null);
    }

    public boolean hasRole(String role){
        if (!isLoggedIn()) {
            throw new IllegalStateException("No user is currently logged in");
        }
        return this.roles.contains(role);
    }
}