package com.web.chitchat.model;


import com.web.chitchat.enums.NotificationType;

public record ChatMessage(
        NotificationType msgType,
        String roomName,
        String userName,
        String message)
{}
