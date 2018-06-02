package com.fanap.podchat;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.util.Log;

import com.fanap.podasync.Async;
import com.fanap.podasync.AsyncAdapter;
import com.fanap.podasync.JsonUtil;
import com.fanap.podchat.model.ChatMessage;
import com.fanap.podchat.model.ChatMessageContent;
import com.fanap.podchat.model.ChatMessageType;
import com.fanap.podchat.model.ChatMessageType.Constants;
import com.fanap.podchat.model.ChatThread;
import com.fanap.podchat.model.Invite;
import com.fanap.podchat.model.Message;
import com.fanap.podchat.model.UserInfo;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Chat extends AsyncAdapter {

    private Async async;
    private Moshi moshi;
    private List<String> conversations;
    private static String TAG = "Chat";
    private String token;
    private ChatListenerManager listenerManager;
    private int userId;

    public void init(Context context) {
        async = Async.getInstance(context).addListener(this);
        moshi = new Moshi.Builder().build();
        listenerManager = new ChatListenerManager();
    }

    public void connect(String serverAddress, String appId, String severName, String token) {
        setToken(token);
        async.connect(serverAddress, appId, severName, token);
        getUserInfo();
    }

    @Override
    public void OnTextMessage(String textMessage) throws IOException {
        super.OnTextMessage(textMessage);
        Log.i("OnTextMessage: Chat", textMessage);
        int messageType = 0;

        JsonAdapter<ChatMessage> jsonAdapter = moshi.adapter(ChatMessage.class);
        ChatMessage chatMessage = jsonAdapter.fromJson(textMessage);
        if (chatMessage != null) {
            messageType = chatMessage.getType();
        }
        @ChatMessageType.Constants int currentMessageType = messageType;
        switch (currentMessageType) {
            case Constants.ADD_PARTICIPANT:

                break;
            case Constants.BLOCK:
                break;
            case Constants.CHANGE_TYPE:
                break;
            case Constants.DELIVERY:
                HandleOnDelivery(chatMessage);
                break;
            case Constants.ERROR:
                break;
            case Constants.FORWARD_MESSAGE:
                break;
            case Constants.GET_CONTACTS:
                handleOnGetContacts(chatMessage);
                break;
            case Constants.GET_HISTORY:
                handleOnGetHistory(chatMessage);
                break;
            case Constants.GET_STATUS:
                break;
            case Constants.GET_THREADS:
                handleOnGetThread(chatMessage);
                break;
            case Constants.INVITATION:
                handleOnInvitation(chatMessage);
                break;
            case Constants.LAST_SEEN_TYPE:
                break;
            case Constants.LEAVE_THREAD:
                break;
            case Constants.MESSAGE:
                Log.d(TAG, "OnTextMessage:MESSAGE  .");
                break;
            case Constants.MUTE_THREAD:
                Log.i("MUTE_THREAD",chatMessage.getContent());
                break;
            case Constants.PING:
                break;
            case Constants.RELATION_INFO:
                break;
            case Constants.REMOVE_PARTICIPANT:
                break;
            case Constants.RENAME:
                break;
            case Constants.SEEN:
                handleOnSeenMessage(chatMessage);
                break;
            case Constants.SENT:
                handleOnSentMessage(chatMessage);
                break;
            case Constants.THREAD_PARTICIPANTS:
                break;
            case Constants.UNBLOCK:
                break;
            case Constants.UN_MUTE_THREAD:
                Log.i("UN_MUTE_THREAD",chatMessage.getContent());

                break;
            case Constants.UPDATE_METADATA:
                break;
            case Constants.USER_INFO:
                handleOnUserInfoMessage(chatMessage);
                break;
            case Constants.USER_STATUS:
                break;
            case Constants.USER_S_STATUS:
                break;
        }
    }

    private void handleOnUserInfoMessage(ChatMessage chatMessage) {
        Log.i("USER_INFO", chatMessage.getContent());
        UserInfo userInfo = new UserInfo();
        setUserId(userInfo.getId());
    }

    private void handleOnSentMessage(ChatMessage chatMessage) {
        listenerManager.callOnSentMessage(chatMessage.getContent());
    }

    private void handleOnSeenMessage(ChatMessage chatMessage) {
        listenerManager.callOnSeenMessage(chatMessage.getContent());
        //if (params.owner !== userInfo.id) {
        //        return sendMessage({chatMessageVOType: chatMessageVOTypes.SEEN, token: token, content: params.messageId, pushMsgType: 4});
        //      }
    }

    private void handleOnInvitation(ChatMessage chatMessage) {
        Log.d(TAG, "INVITATION  .");
        listenerManager.callOnInvitation(chatMessage.getContent());
    }

    private void HandleOnDelivery(ChatMessage chatMessage) {
        listenerManager.callOnDeliveryMessage(chatMessage.getContent());
        //if (params.owner !== userInfo.id) {
        //        return sendMessage({chatMessageVOType: chatMessageVOTypes.DELIVERY, token: token, content: params.messageId, pushMsgType: 4});
        //      }
    }

    private void handleOnGetContacts(ChatMessage chatMessage) {
        listenerManager.callOnGetContacts(chatMessage.getContent());
    }

    private void handleOnGetHistory(ChatMessage chatMessage) throws IOException {
        JsonAdapter<Message> jsonHistoryAdapter = moshi.adapter(Message.class);
        Message jsonHistoryMessage = jsonHistoryAdapter.fromJson(chatMessage.getContent());
        listenerManager.callOnGetThreadHistory(chatMessage.getContent());
        jsonHistoryMessage.getId();
        jsonHistoryMessage.getUniqueId();
        jsonHistoryMessage.getPreviousId();
        jsonHistoryMessage.getMessage();
        jsonHistoryMessage.getParticipant();
    }

    private void handleOnGetThread(ChatMessage chatMessage) {
        Log.d(TAG, "OnTextMessage:GET_THREADS  .");
        chatMessage.getUniqueId();
        chatMessage.getTime();
        conversations = new ArrayList<>(Arrays.asList(chatMessage.getContent().split(",")));
        setConversations(conversations);
        listenerManager.callOnGetThread(chatMessage.getContent());
    }

    public void sendTextMessage(String textMessage, long threadId) {

        Log.i("send Message called", textMessage);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(textMessage);
        chatMessage.setType(Constants.MESSAGE);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(getUniqueId());
        chatMessage.setTime(1000);
        chatMessage.setSubjectId(threadId);

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        async.sendMessage(asyncContent, 4);
    }

    public void getThread(int count, int offset) {
        Log.i("get thread called", "count" + count);
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setCount(count);
        chatMessageContent.setOffset(offset);

        JsonAdapter<ChatMessageContent> messageContentJsonAdapter = moshi.adapter(ChatMessageContent.class);
        String content = messageContentJsonAdapter.toJson(chatMessageContent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(ChatMessageType.Constants.GET_THREADS);
        chatMessage.setTokenIssuer("1");
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(getUniqueId());

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        async.sendMessage(asyncContent, 3);
    }

    public void getHistory(int count, int offset, String order, long threadId) {
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setCount(count);
        chatMessageContent.setOffset(offset);
        chatMessageContent.setOrder(order);

        JsonAdapter<ChatMessageContent> messageContentJsonAdapter = moshi.adapter(ChatMessageContent.class);
        String content = messageContentJsonAdapter.toJson(chatMessageContent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(Constants.GET_HISTORY);
        chatMessage.setUniqueId(getUniqueId());
        chatMessage.setToken(getToken());
        chatMessage.setSubjectId(threadId);

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        async.sendMessage(asyncContent, 3);
    }

    public void getHistory(int count, int offset) {
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setCount(count);
        chatMessageContent.setOffset(offset);

        JsonAdapter<ChatMessageContent> messageContentJsonAdapter = moshi.adapter(ChatMessageContent.class);
        String content = messageContentJsonAdapter.toJson(chatMessageContent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(Constants.GET_HISTORY);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(getUniqueId());

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        async.sendMessage(asyncContent, 3);
    }

    public void getContacts(int count, int offset) {
        ChatMessageContent chatMessageContent = new ChatMessageContent();
        chatMessageContent.setCount(count);
        chatMessageContent.setOffset(offset);

        JsonAdapter<ChatMessageContent> messageContentJsonAdapter = moshi.adapter(ChatMessageContent.class);
        String content = messageContentJsonAdapter.toJson(chatMessageContent);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(content);
        chatMessage.setType(Constants.GET_CONTACTS);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(getUniqueId());

        JsonAdapter<ChatMessage> chatMessageJsonAdapter = moshi.adapter(ChatMessage.class);
        String asyncContent = chatMessageJsonAdapter.toJson(chatMessage);

        async.sendMessage(asyncContent, 3);
    }

    public void createThread(int type, String title) {
        Log.i("createThread called", "count" + type);

        List<Invite> invites = new ArrayList<>();
        invites.add(new Invite(441, 2));
//        invites.add(new Invite(442,2));

        ChatThread chatThread = new ChatThread();
        chatThread.setType(type);
        chatThread.setTitle(title);
        chatThread.setInvitees(invites);

        String contentThreadChat = JsonUtil.getJson(chatThread);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(contentThreadChat);
        chatMessage.setType(Constants.INVITATION);
        chatMessage.setToken(getToken());
        chatMessage.setUniqueId(getUniqueId());
        chatMessage.setTokenIssuer("1");

        String asyncContent = JsonUtil.getJson(chatMessage);

        async.sendMessage(asyncContent, 4);
        Log.i("Create thread", asyncContent);
    }

    public void getUserInfo() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.USER_INFO);
        chatMessage.setToken(getToken());
        String asyncContent = JsonUtil.getJson(chatMessage);

        async.sendMessage(asyncContent, 3);
    }

    public void muteThread(int threadId) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.MUTE_THREAD);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");
        chatMessage.setSubjectId(threadId);
        chatMessage.setUniqueId(getUniqueId());

        String asyncContent = JsonUtil.getJson(chatMessage);
        async.sendMessage(asyncContent, 4);
    }

    public void unMuteThread(int threadId) {

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.UN_MUTE_THREAD);
        chatMessage.setToken(getToken());
        chatMessage.setTokenIssuer("1");
        chatMessage.setSubjectId(threadId);
        chatMessage.setUniqueId(getUniqueId());

        String asyncContent = JsonUtil.getJson(chatMessage);
        async.sendMessage(asyncContent, 4);
    }

    public void editMessage(int messageId, String messageContent) {

        Message message = new Message();
        message.setId(messageId);
        message.setMessage(messageContent);

        String editedMessage = JsonUtil.getJson(message);

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setType(Constants.EDIT_MESSAGE);
        chatMessage.setToken(getToken());
        chatMessage.setContent(editedMessage);
        chatMessage.setTokenIssuer("1");
        String asyncContent = JsonUtil.getJson(chatMessage);

        async.sendMessage(asyncContent, 4);
    }

    public String onMessage() {
        return async.getMessageLiveData().getValue();
    }

    /**
     * Add a listener to receive events on this Chat.
     *
     * @param listener A listener to add.
     * @return {@code this} object.
     */
    public Chat addListener(ChatListener listener) {
        listenerManager.addListener(listener);
        return this;
    }

    public Chat addListeners(List<ChatListener> listeners) {
        listenerManager.addListeners(listeners);

        return this;
    }

    public Chat removeListener(ChatListener listener) {
        listenerManager.removeListener(listener);

        return this;
    }

    /**
     * Get the manager that manages registered listeners.
     */
    ChatListenerManager getListenerManager() {
        return listenerManager;
    }


    /**
     * Get list of conversations
     */
    public List<String> getConversation() {
        return conversations;
    }

    public void setConversations(List<String> conversations) {
        this.conversations = conversations;
    }

    public LiveData<String> getState() {
        return async.getStateLiveData();
    }

    private static synchronized String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    private void setToken(String token) {
        this.token = token;
    }

    private String getToken() {
        return token;
    }

    private int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
