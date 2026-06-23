package com.web.chitchat.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.chitchat.enums.NotificationType;
import com.web.chitchat.helpers.ConsoleHelpers;
import com.web.chitchat.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class TerminalChatClient extends TextWebSocketHandler {

    private WebSocketSession session;
    private final ObjectMapper mapper = new ObjectMapper();
    private final CountDownLatch connected =
            new CountDownLatch(1);

    public void connect() throws Exception {

        new StandardWebSocketClient().execute(
                this,
                "ws://localhost:8080/chat"
        );
        connected.await();
    }

    @Override
    public void afterConnectionEstablished(
            WebSocketSession session) {
        this.session = session;
        connected.countDown();
       log.info("Connected");
    }

    public void send(ChatMessage msg) throws Exception {
        session.sendMessage(
                new TextMessage(mapper.writeValueAsString(msg))
        );
    }

    @Override
    public void handleTextMessage(
            WebSocketSession session,
            TextMessage message) throws JsonProcessingException {

        ChatMessage msg = mapper.readValue(
                message.getPayload(),
                ChatMessage.class
        );

        System.out.printf(
                "\n[%s] %s: %s\n> ",
                msg.roomName(),
                msg.userName(),
                msg.message()
        );
    }



    public static void main(String[] args) throws Exception {

        TerminalChatClient client = new TerminalChatClient();
        client.connect();

        Scanner scanner = new Scanner(System.in);

        ConsoleHelpers.prompt("Username: ");
        String user = scanner.nextLine();

        ConsoleHelpers.prompt("Room: ");
        String room = scanner.nextLine();

        client.send(new ChatMessage(
                NotificationType.JOIN,
                room,
                user,
                null
        ));

        System.out.println("\n--- You joined the chat ---");
        System.out.println("Type messages below:\n");

        //TODO: on printing exit cmd it should exit the terminal and kill the client
        while (true) {
            System.out.print("> ");
            String msg = scanner.nextLine();

            client.send(new ChatMessage(
                    NotificationType.CHAT,
                    room,
                    user,
                    msg
            ));
        }
    }
}
