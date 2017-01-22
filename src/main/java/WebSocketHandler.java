import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketHandler {

    private String sender, msg;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
//        String username = "User" + Chat.nextUserNumber++;
//        Chat.userUsernameMap.put(user, username);
//        Chat.broadcastMessage(sender = "Server", msg = (username + " joined the chat"));
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        String username = Chat.userUsernameMap.get(user);
        Chat.userUsernameMap.remove(user);
        Chat.broadcastMessage(sender = "Server", msg = (username + " left the chat"));
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        String reason = message.substring(0, message.indexOf('='));
        String content = message.substring(message.indexOf('=') + 1);
        switch(reason) {
            case "username":
                Chat.userUsernameMap.put(user, content);
                Chat.broadcastMessage(sender = "Server", msg = (content + " joined the chat"));
                break;
            case "userMessage":
                Chat.broadcastMessage(sender = Chat.userUsernameMap.get(user), msg = content);
                break;
//            case "addChannel":
//                Chat.userChannelMap.put(Chat.userUsernameMap.get(user), content);
//                break;
        }
    }

}
