import org.eclipse.jetty.websocket.api.*;
import org.json.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {

    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    private Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    private Map<String, String> userChannelMap = new ConcurrentHashMap<>();
    private List<String> channelList = new CopyOnWriteArrayList<>();
    private int channelCount = 1;

    public void initialize() {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(1);
        webSocket("/chat", WebSocketHandler.class);
        init();
    }

    //Sends a message from one user to all users, along with a list of current usernames
    public void broadcastMessage(String sender, String message) {
        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("userMessage", createHtmlMessageFromSender(sender, message))
                    .put("userlist", userUsernameMap.values())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private String createHtmlMessageFromSender(String sender, String message) {
        return article().with(
                b(sender + " says:"),
                p(message),
                span().withClass("timestamp").withText(new SimpleDateFormat("HH:mm:ss").format(new Date()))
        ).render();
    }

    public boolean removeUser(Session user) {
        try {
            String username = userUsernameMap.get(user);
            userUsernameMap.remove(user);
            broadcastMessage("Server", (username + " left the chat"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addUser(Session user, String content) {
        try {
            userUsernameMap.put(user, content);
            broadcastMessage("Server", (content + " joined the chat"));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean broadcastHelper(Session user, String content) {
        try {
            broadcastMessage(userUsernameMap.get(user), content);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean createChannel(Session user) {
        try {
            String channelName = "channel" + channelCount;
            String userName = userUsernameMap.get(user);
            userChannelMap.put(userName, channelName);
            channelCount++;
            broadcastMessage("Server", (userName + " joined " + channelName));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
