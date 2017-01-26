import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

@WebSocket
public class WebSocketHandler {

    private String sender, msg;
    private Chat chat;

    @OnWebSocketConnect
    public void onConnect(Session user) throws Exception {
        chat = ChatApp.getChatInstance();
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
                    chat.addUser(user, content);
                    break;
                case "userMessage":
                    if (!chat.chatBotAsk(user, content))
                        chat.broadcastInChannelHelp(user, content);
                    break;
                case "addChannel":
                    chat.createChannel(user);
                    break;
                case "exitChannel":
                    chat.exitChannel(user);
                    break;
                case "joinChannel":
                    chat.joinChannel(user, content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
