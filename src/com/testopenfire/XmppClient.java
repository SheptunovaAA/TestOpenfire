package com.testopenfire;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * Created by Alena_Fox on 31.01.2015.
 */
public class XmppClient {
    // Задержка для таймера = 10 мин
    private int delay = 60000;
    // порт для обращения клиента к Openfire
    int clientPort = 4002;
    // уникальный идентификатор данного клиента
    String usrId="";

    public XMPPConnection createNewClient(int userId) {
        usrId = String.valueOf(userId);
        try {
            OpenFireRest.createNewUser(usrId);
            XMPPConnection connection = CreateConnectionTOXmppServer();

            // создаем таймер - на удаление данного клиента
            Timer mTimer = new Timer();
            // создаем таск для таймера
            MyTimerTask mMyTimerTask = new MyTimerTask();
            // delay in ms
            mTimer.schedule(mMyTimerTask, delay );

            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private XMPPConnection CreateConnectionTOXmppServer() {
                XMPPConnection connection;
                //подключаемся к серверу
                ConnectionConfiguration config = new ConnectionConfiguration(OpenFireRest.serverAddress, clientPort,OpenFireRest.serverName);
                // настройки шифрования - пока без него делаем
                config.setCompressionEnabled(false);
                config.setSASLAuthenticationEnabled(false);
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
                // создаем соединение
                connection = new XMPPConnection(config);
                try {
                    connection.connect();
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                try{
                    //before logging(!) connection - create a roster to listen changes of Presences
                    //Roster roster = connection.getRoster();
                    // switch subscription mode, default is automatic
                    connection.getRoster().setSubscriptionMode(Roster.SubscriptionMode.accept_all);
                    connection.getRoster().addRosterListener(new RosterListener() {
                        // Ignored events public void entriesAdded(Collection<String> addresses) {}
                        public void entriesDeleted(Collection<String> addresses) {}
                        public void entriesAdded(Collection<String> strings) {  System.out.println("SMTH"); }
                        public void entriesUpdated(Collection<String> addresses) {}
                        // информируем о смене статуса собеседника
                        public void presenceChanged(Presence presence) {
                            System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
                        }
                    });
                    // login into server
                    loginUser(connection,usrId);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return connection;
    }

    /**
     * Log In serverXmpp. Login=Pass on default
     * @param logName - login of current user (must exist on serverXmpp)
     */
    private void loginUser(XMPPConnection connection, String logName) {
        try{
            // логинемся в созданном соединении к севреру
            connection.login(logName, logName);
        } catch (XMPPException e) {
            System.out.println("THERE IS AN ERROR WITH AUTHENTICATION: check LOGIN and PASSWORD of user\n");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param OpponentJID - JID of your Opponent. If opponent with this JIF doesn't exist it will NOT be an error!
     *                    But when(if) user with OpponentJID will be create - the chat will be createToo
     * @return
     */
    public Chat createChat(XMPPConnection connection, String OpponentJID, MessageListener listener){
        Chat chat;
        ChatManager chatmanager = connection.getChatManager();
        // по умолчанию у нас создается бот-болтун (принимаю собщения, которые сам же и отправил)
        if(listener==null) {
            listener = new MessageListener() {
                // на слушатель вешаем оповещение о получаемом от собеседника сообщении
                public void processMessage(Chat chat, Message message) {
                    try {
                        // Send back the same text the other user sent us (аналог бота)
                        chat.sendMessage(message.getBody());
                    } catch (XMPPException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
        // если чат уже создан - то надо к нему присоединиться и повесить слушатель на вх сообщения
        final MessageListener MesListener = listener;
        chatmanager.addChatListener(
                new ChatManagerListener() {
                    @Override
                    public void chatCreated(Chat chat, boolean createdLocally)
                    {
                        if (!createdLocally)
                            chat.addMessageListener(MesListener);;
                    }
                });
        // если чат еще не создан - его надо создать и добавить слушатель на вх сообщ
        chat = chatmanager.createChat(OpponentJID, listener);

        return chat;
    }/**
     * Send Message to opponent in chat
     * @param text - text of sending Message
     * @param chat - current chat (to send msg)
     */
    public void sendMsg(Chat chat, String text){
        try {
            chat.sendMessage(text);
            System.out.println("send msg: "+text);
        }
        catch (XMPPException e) {
            System.out.println("Error recieve Message : '"+text+"'\n");
            e.printStackTrace();
        }
    }

    /**
     * Disconnect already opened connection to server
     */
    public void disconnect(XMPPConnection connection){
        try{
            connection.disconnect();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    


    private void deleteXmppClient(){
        try {
            OpenFireRest.deleteUser(usrId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            deleteXmppClient();
            System.out.println("user -  "+ usrId +"  - delete successful\n");
            //Thread.currentThread().interrupt();
           //return;
        }
    }
}
