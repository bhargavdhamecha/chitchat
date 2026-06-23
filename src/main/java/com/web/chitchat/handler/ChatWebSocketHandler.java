package com.web.chitchat.handler;


import com.web.chitchat.model.ChatMessage;
import com.web.chitchat.service.ChatRoomService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;


import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.web.chitchat.enums.NotificationType.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper;
    private final ChatRoomService chatRoomService;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws IOException {

        ChatMessage msg = objectMapper.readValue(
                message.getPayload(),
                ChatMessage.class);

        switch (msg.msgType()) {

            case CREATE_ROOM ->
                    chatRoomService.createRoom(
                            msg.roomName());

            case JOIN ->
                    chatRoomService.joinRoom(
                            msg.roomName(),
                            msg.userName(),
                            session);

            case CHAT ->
                    chatRoomService.broadcast(msg);

            default -> {}
        }
    }

    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status) {

        chatRoomService.leaveRoom(session);

        log.info("Disconnected: {}", session.getId());
    }
}
