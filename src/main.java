/**
 * Created by Alena_Fox on 31.01.2015.
 */
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class main {
    static int delay = 50000;
    static int numberOfClients = 10;
    public static void main(String [] argv) {
        for (int i=1;  i < numberOfClients; i=i+2) {
            final int finalI = i;
            Thread newTread = new Thread(){
                public void run() {
                    twoClientsEmulation(finalI);
                }
            };
            newTread.start();
        }
    }

    public static void twoClientsEmulation(int i){
        // переменная, контролирующая время беседы - при завершении - закрываем соединения
        final boolean[] finish = {true};
        // создаем два собеседника
        XmppClient client1 = new XmppClient();
        XmppClient client2 = new XmppClient();
        // создаем клиента, который будет жить 10 минут
        int userid_1=i;
        int userid_2=i+1;
        XMPPConnection connection1 = client1.createNewClient(userid_1);
        XMPPConnection connection2 = client2.createNewClient(userid_2);
        // создаем чат для данных собеседников
        Chat chat = client1.createChat(connection1, String.valueOf(userid_2), new MessageListener() {
            @Override
            public void processMessage(Chat chat, Message message) {
                // не делаем у одного клиента никаких действий при получении им сообщений
            }
        });

        // создаем таймер - для завершения дисконнекта
        Timer mTimer = new Timer();
        // delay in ms
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                finish[0] = false;
            }
        }, delay);

        // переписка
        while (finish[0]) {
            try {
                // отправляем первым собеседником сообщения (второй собеседник будет слать его нам в ответ)
                client1.sendMsg(chat, "msg text - "+UUID.randomUUID().toString());
                sleep(14000);

                // break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO: after using this service -  disconnect from xmpp Server!
        connection1.disconnect();
        connection2.disconnect();

        System.out.println("ok");
    }
}
