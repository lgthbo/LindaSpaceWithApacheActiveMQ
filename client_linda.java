import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import java.util.*;
/*class Clinet_rcv_thread extends Thread {
    private String myname;
    public Clinet_rcv_thread(){}
    public Clinet_rcv_thread(String name){
        this.myname = name;
    }
    public void run(){
       ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
       Connection connection = connectionFactory.createConnection();
       connection.start();
       Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
       Destination destination = session.createQueue(this.name);
       MessageConsumer consumer = session.createConsumer(destination);
       consumer.setMessageListener(new MessageListener() {
           public void onMessage(Message message) {
               TextMessage textMessage = (TextMessage) message;
               try {
                   System.out.println("接收消息：" + textMessage.getText());
               } catch (Exception e) {
                   e.printStackTrace();
               }
           }
       });
    }
}*/
public class client_linda {
    private static final String url = "tcp://localhost:61616";
    private static final String queueName = "SendFromClinet";
    public static boolean lock = false;
    public static void sendToLindaSpace(String msg) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);
        TextMessage textMessage = session.createTextMessage(msg);
        producer.send(textMessage);
        connection.close();
    }
    public static void main(String[] args) throws JMSException {
        System.out.println("Hello! Welcome to Client ya! What is your name?");
        Scanner input_scanner = new Scanner(System.in);
        String clinetName = input_scanner.nextLine();
        //Thread clinetListener = new Clinet_rcv_thread(clinetName);
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(clinetName);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println("Receive message from Linda server:\n" + textMessage.getText());
                    lock = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        while(true){
            String output = input_scanner.nextLine();
            if(lock){
                System.out.println("This Client is lock, waiting for message from server.");
            }else{
                sendToLindaSpace(clinetName+" "+output);
                lock = true;
            }
        }

    }
}
