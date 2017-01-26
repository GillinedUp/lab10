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
    private ChatBot chatBot = new ChatBot();;

    public void initialize() {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(300);
        channelList.add("chatBot");
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
                        .put("channelList", channelList)
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    // broadcasts message in channel the user currently in
    public void broadcastInChannel(String sender, String message) {
//        String username = userUsernameMap.get(session);
        String channelName = userChannelMap.get(sender);
        userUsernameMap.keySet().stream().filter(Session::isOpen)
                .filter(session -> {
                    try {
                        return userChannelMap.get(userUsernameMap.get(session)).equals(channelName);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", createHtmlMessageFromSender(sender, message))
                        .put("userlist", userUsernameMap.values())
                        .put("channelList", channelList)
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

    // method for broadcasting
    public boolean broadcastInChannelHelp(Session user, String content) {
        if (userChannelMap.containsKey(userUsernameMap.get(user))) {           // check if user connected to any channel
            try {
                broadcastInChannel(userUsernameMap.get(user), content);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    // user handling
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

    public void addUser(Session user, String content) {
        if(!userUsernameMap.containsValue(content)) {                // if that name hasn't already taken
            try {
                userUsernameMap.put(user, content);
                broadcastMessage("Server", (content + " joined the chat"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                user.getRemote().sendString(String.valueOf(new JSONObject()
                        .put("userMessage", "usernameIsTaken")        // send info to js
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // channel handling
    public void createChannel(Session user) {
        try {
            String channelName = "channel" + channelCount;
            String userName = userUsernameMap.get(user);
            userChannelMap.put(userName, channelName);
            channelList.add(channelName);
            channelCount++;
            broadcastMessage("Server", (userName + " joined " + channelName));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exitChannel(Session user) {
        try {
            String userName = userUsernameMap.get(user);
            if(userChannelMap.containsKey(userName)) {               // check if user connected to any channel
                userChannelMap.remove(userName);                     // disconnect him from it
                broadcastMessage("Server", (userName + " left the channel"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinChannel(Session user, String content) {
        try {
            String userName = userUsernameMap.get(user);
            // check if user isn't connected to any channel
            if (!userChannelMap.containsKey(userName)) {
                userChannelMap.put(userName, content);
                broadcastMessage("Server", (userName + " joined " + content));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ChatBot
    public boolean chatBotAsk(Session user, String message) {
        String userName = userUsernameMap.get(user);
        String channelName = userChannelMap.get(userName);
        if(channelName.equals("chatBot")) {
            try {
                broadcastInChannel("chatBot", chatBot.getAnswer(message));
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
