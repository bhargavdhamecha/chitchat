package com.web.chitchat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.chitchat.model.ChatMessage;
import com.web.chitchat.model.UserInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {
    Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    Map<WebSocketSession, UserInfo> users = new ConcurrentHashMap<>();
    Map<String, WebSocketSession> usernames = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        rooms.put("General", ConcurrentHashMap.newKeySet());
        rooms.put("Java", ConcurrentHashMap.newKeySet());
        rooms.put("Sports", ConcurrentHashMap.newKeySet());
    }

    public void createRoom(String roomName){
        rooms.putIfAbsent(
                roomName,
                ConcurrentHashMap.newKeySet());
        log.info("{} room created successfully.", roomName);
    }

    public void leaveRoom(
            WebSocketSession session){
        UserInfo userInfo = users.remove(session);

        if (userInfo == null) {
            return;
        }

        Set<WebSocketSession> members =
                rooms.get(userInfo.roomName());

        if (members != null) {
            members.remove(session);

            log.info("{} left room {}",
                    userInfo.username(),
                    userInfo.roomName());

            // Optional: remove empty room
            if (members.isEmpty()) {
                rooms.remove(userInfo.roomName());

                log.info("Removed empty room {}",
                        userInfo.roomName());
            }
        }
    }

    public Set<WebSocketSession> getRoomMembers(
            String roomName){
        return rooms.getOrDefault(
                roomName,
                Collections.emptySet());
    }

    public Collection<String> getAvailableRooms(){
        return rooms.keySet();
    }

    public void joinRoom(
            String roomName,
            String username,
            WebSocketSession session) {

        leaveRoom(session);

        Set<WebSocketSession> members =
                rooms.computeIfAbsent(
                        roomName,
                        k -> ConcurrentHashMap.newKeySet());

        members.add(session);

        users.put(
                session,
                new UserInfo(username, roomName));

        log.info("{} joined {}", username, roomName);
    }

    public void broadcast(ChatMessage message)
            throws IOException {

        Set<WebSocketSession> members =
                rooms.getOrDefault(
                        message.roomName(),
                        Collections.emptySet());

        String payload =
                objectMapper.writeValueAsString(message);

        for (WebSocketSession member : members) {

            if (member.isOpen()) {

                member.sendMessage(
                        new TextMessage(payload));
            }
        }
    }
}
