package com.jinglu.fl.demo.webSocket;

import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

/**
 * @Description: TODO
 * @author: fulin
 * @date: 2021年04月17日 10:22
 */
@Component
@ServerEndpoint("/ws/websocket")
public class WebSocket {
    //静态变量，用来记录当前在线连接数。应设为线程安全
    private static int onlineCount = 0;

    //存放客户端对应的WebSocket对象
    //用Map存放，其中Key用户的标识
    private static HashMap<String,WebSocket> webSocketHashMap = new HashMap<>();
    //与某个客户端的连接会话，需要通过它来个客户端发送数据
    private Session session;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        this.session = session;
        webSocketHashMap.put(session.hashCode() + "" , this);
        System.out.println("这个客户端用户名：" + session.hashCode());
        this.sendMessage("您的账号用户名：" + session.hashCode());
        addOnlineCount();
        System.out.println("有新的连接加入！,当前在线人数为：" + getOnlineCount());
    }

    private void addOnlineCount() {
        onlineCount++;
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        //从map中删除
        webSocketHashMap.remove(this);
        subOnlineCount();
        System.out.println("有一连接断开！当前在线人数为：" + getOnlineCount());
    }

    private void subOnlineCount() {
        onlineCount--;
    }

    @OnError
    public void onError(Session session,Throwable throwable){
        System.out.println("发生错误");
        throwable.printStackTrace();
    }
    @OnMessage
    public void onMessage(String message,Session session) throws IOException {
        //通过标识符解析内容
        int pos = message.indexOf("#*#*");
        //发送的信息
        String UserSay = message.substring(0,pos);
        //用户账号
        String UserAccount = message.substring(pos+4,message.length());
        String say = session.hashCode() + "说：" + UserSay;
        System.out.println(say);
        WebSocket socket = webSocketHashMap.get(UserAccount);
        socket.sendMessage(say);
    }
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }
}
