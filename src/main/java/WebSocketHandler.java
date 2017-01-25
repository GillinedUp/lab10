import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketHandler {

    private String sender, msg;
    private Chat chat;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        chat = ChatApp.getChatInstance();
//        String username = "User" + Chat.nextUserNumber++;
//        Chat.userUsernameMap.put(user, username);
//        Chat.broadcastMessage(sender = "Server", msg = (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        if (chat.removeUser(user)) {}
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        String reason = message.substring(0, message.indexOf('='));
        String content = message.substring(message.indexOf('=') + 1);
        try {
            switch(reason) {
                case "username":
                    if (chat.addUser(user, content)) {}
                    break;
                case "userMessage":
                    if (chat.broadcastInChannelHelper(user, content)) {}
                    break;
                case "addChannel":
                    if (chat.createChannel(user)) {}
                    break;
                case "exitChannel":
                    if (chat.exitChannel(user)) {}
                    break;
                case "joinChannel":
                    if (chat.joinChannel(user, content)) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
