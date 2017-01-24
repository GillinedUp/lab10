/**
 * Created by yurii on 24.01.17.
 */
public class ChatApp {

    private static Chat chat;

    public static void main(String[] args) {
        chat = new Chat();
        chat.initialize();
    }

    public static Chat getChatInstance(){
        return chat;
    }

}
