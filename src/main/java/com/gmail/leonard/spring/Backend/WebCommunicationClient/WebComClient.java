package com.gmail.leonard.spring.Backend.WebCommunicationClient;

import com.gmail.leonard.spring.Backend.CommandList.CommandListContainer;
import com.gmail.leonard.spring.Backend.FAQ.FAQListContainer;
import com.gmail.leonard.spring.Backend.UserData.ServerListData;
import com.gmail.leonard.spring.Backend.UserData.SessionData;
import com.gmail.leonard.spring.Backend.WebCommunicationClient.Events.*;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class WebComClient {

    private static WebComClient instance = new WebComClient();
    public static WebComClient getInstance() { return instance; }

    private static final String EVENT_COMMANDLIST = "command_list";
    public static final String EVENT_FAQLIST = "faq_list";
    private static final String EVENT_SERVERLIST = "server_list";
    private static final String EVENT_SERVERMEMBERS = "server_members";
    private static final String EVENT_TOPGG = "topgg";
    private static final String EVENT_DONATEBOT_IO = "donatebot.io";
    private static final String EVENT_FEEDBACK = "feedback";

    private HashMap<String, TransferCache> transferCaches = new HashMap<>();

    private boolean started = false;
    private Socket socket;

    private WebComClient() {
        addTransferCaches(
                new TransferCache(EVENT_COMMANDLIST),
                new TransferCache(EVENT_FAQLIST),
                new TransferCache(EVENT_SERVERLIST, "user_id"),
                new TransferCache(EVENT_SERVERMEMBERS, "user_id"),
                new TransferCache(EVENT_TOPGG),
                new TransferCache(EVENT_DONATEBOT_IO),
                new TransferCache(EVENT_FEEDBACK)
        );
    }

    private void addTransferCaches(TransferCache... newTransferCaches) {
        Stream.of(newTransferCaches)
                .forEach(transferCache -> transferCaches.put(transferCache.getEvent(), transferCache));
    }

    private <T> CompletableFuture<T> send(String event, Class<T> c) {
        return send(event, null, c);
    }

    private <T> CompletableFuture<T> send(String event, JSONObject jsonObject, Class<T> c) {
        CompletableFuture<T> CompletableFuture = transferCaches.get(event).register(jsonObject, c);

        if (jsonObject != null) socket.emit(event, jsonObject.toString());
        else socket.emit(event);

        return CompletableFuture;
    }

    public void start(int port) {
        if (started) return;
        started = true;

        IO.Options options = new IO.Options();
        options.reconnection = true;
        try {
            socket = IO.socket("http://127.0.0.1:" + port + "/");

            //Events
            socket.on(EVENT_COMMANDLIST, new OnCommandList(transferCaches.get(EVENT_COMMANDLIST)));
            socket.on(EVENT_FAQLIST, new OnFAQList(transferCaches.get(EVENT_FAQLIST)));
            socket.on(EVENT_SERVERLIST, new OnServerList(transferCaches.get(EVENT_SERVERLIST)));
            socket.on(EVENT_SERVERMEMBERS, new OnServerMembers(transferCaches.get(EVENT_SERVERMEMBERS)));

            socket.on(EVENT_TOPGG, new OnGenericResponseless(transferCaches.get(EVENT_TOPGG)));
            socket.on(EVENT_DONATEBOT_IO, new OnGenericResponseless(transferCaches.get(EVENT_DONATEBOT_IO)));
            socket.on(EVENT_FEEDBACK, new OnGenericResponseless(transferCaches.get(EVENT_FEEDBACK)));

            socket.connect();
            System.out.println("The WebCom client has been started!");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<CommandListContainer> updateCommandList() {
        return send(EVENT_COMMANDLIST, CommandListContainer.class);
    }

    public CompletableFuture<FAQListContainer> updateFAQList() {
        return send(EVENT_FAQLIST, FAQListContainer.class);
    }

    public CompletableFuture<ServerListData> getServerListData(SessionData sessionData) {
        if (sessionData.isLoggedIn()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", sessionData.getUserId().get());
            return send(EVENT_SERVERLIST, jsonObject, ServerListData.class);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<JSONObject> getServerMembersCount(SessionData sessionData, long serverId) {
        if (sessionData.isLoggedIn()) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", sessionData.getUserId().get());
            jsonObject.put("server_id", serverId);
            socket.emit(EVENT_SERVERMEMBERS, jsonObject.toString());

            return send(EVENT_SERVERMEMBERS, jsonObject, JSONObject.class);
        }

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> sendTopGG(JSONObject jsonObject) {
        return send(EVENT_TOPGG, jsonObject, Void.class);
    }

    public CompletableFuture<Void> sendDonatebotIO(JSONObject jsonObject) {
        return send(EVENT_DONATEBOT_IO, jsonObject, Void.class);
    }

    public CompletableFuture<Void> sendFeedback(String reason, String explanation) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reason", reason);
        jsonObject.put("explanation", explanation);
        return send(EVENT_FEEDBACK, jsonObject, Void.class);
    }

}
