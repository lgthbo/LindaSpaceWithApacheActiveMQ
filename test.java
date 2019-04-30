import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import java.util.*;
public class test{

    private static final String url = "tcp://localhost:61616";
    private static final String queueName = "testQ2";

    public static void main(String[] args) throws JMSException{
        //1. 创建连接工厂
           ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
           //2. 创建连接
           Connection connection = connectionFactory.createConnection();
           //3. 启动连接
           connection.start();
           //4. 创建会话
           Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
           //5. 创建目的
           Destination destination = session.createQueue(queueName);
           //6. 创建一个消费者
           MessageConsumer consumer = session.createConsumer(destination);
           //7. 消费者接收消息
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
}
