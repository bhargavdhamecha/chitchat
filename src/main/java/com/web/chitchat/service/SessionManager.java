package com.web.chitchat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionManager {

    private final Map<String, WebSocketSession> sessions =
            new ConcurrentHashMap<>();

    public void addSession(String username,
                           WebSocketSession session) {
        sessions.put(username, session);
    }

    public void removeSession(String username) {
        sessions.remove(username);
    }

    public WebSocketSession getSession(String username) {
        return sessions.get(username);
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    /*
    addUser()
    removeUser()
    getUserSession()
    getOnlineUsers()
     */
}