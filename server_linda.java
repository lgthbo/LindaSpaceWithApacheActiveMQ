import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import java.util.*;
public class server_linda{
    private static final String url = "tcp://localhost:61616";
    private static final String queueName = "SendFromClinet";
    public static Vector<Tuple> space ;
    public static Vector<Tuple> suspendedList;
    public static Vector<String[]> assignList;

    //將資料傳給 client
    public static void sendToClient(String msg ,String target) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(target);
        MessageProducer producer = session.createProducer(destination);
        TextMessage textMessage = session.createTextMessage(msg);
        producer.send(textMessage);
        connection.close();
    }
    //顯示 space 內部狀況
    public static void showSpace(){
        System.out.println("Space status :");
    }
    //用來控制 space內部狀況
    public static void spaceHandler(String input_string)  throws JMSException {
        String[] tmpArray = input_string.split(" ");    //將輸入先存到一個陣列裡面
        //先檢查要不要 assign
        for(int i=2;i<len;i++){
            if(tmpArray[i].contains("?")){
                //TODO 有問號 就會生成 assign pair
            }

            if(!tmpArray[i].matches("-?\\d+(\\.\\d+)?") && !tmpArray[i].contains("\"")){
                //tmpArray[i] = find(tmpArray[i]);
                //TODO 假設不是數字也不是字串 => 要 assign
            }
        }
        String source = tmpArray[0]; //第一個會是來源
        String cmd = tmpArray[1];   //第二個會是指令
        Tuple rcvTuple = new Tuple(tmpArray,source);
        if(cmd.equals("out")){
            rcvTuple.printRcvMessage();
            space.add(rcvTuple);
            //找看看有沒有target 在 suspended list 裡面
            for(int i=0;i<suspendedList.size();i++){
                if(rcvTuple.compareTo(suspendedList.get(i)) == 1){
                    String ret_str = suspendedList.get(i).getSendMessage(); //已經是可以傳回去的String了
                    String ret_target = suspendedList.get(i).getSource();
                    //如果是 in 就要拿走 tuple
                    if(suspendedList.get(i).getCMD().equals("in")){
                        suspendedList.remove(i);
                    }
                    //把這個tuple send 給 clinet
                    sendToClient(ret_str,ret_target);
                    break;
                }
            }

        }else if(cmd.equals("read") || cmd.equals("in")){
            boolean suspended = true;       //儲存是否找不到
            for(int i=0;i<space.size();i++){
                if(rcvTuple.compareTo(space.get(i)) == 1){
                    suspended = false;
                    String ret_str = space.get(i).getSendMessage(); //已經是可以傳回去的String了
                    String ret_target = space.get(i).getSource();
                    //如果是 in 就要拿走 tuple
                    if(cmd.equals("in")){
                        space.remove(i);
                    }
                    //把這個tuple send 給 clinet
                    sendToClient(ret_str,ret_target);
                    break;
                }
            }
            if(suspended){  //如果找不到
                suspendedList.add(rcvTuple);
                System.out.println("Can not find target tuple , The process suspended.");
            }
        }else{
            System.out.println("Server get error input.");
        }
    }
    public static void main(String[] args)  throws JMSException {
        System.out.println("Hello! I'm Server ya !");
        space = new Vector<Tuple>();    //Linda space
        suspendedList = new Vector<Tuple>();   //用來儲存那些想 in 和 read 但是沒有成功的 Operation
        /*
            這個assigned 裡面有三層 都是放 String
            isset
            key
            value

        */
        assignList = new Vector<String[]>();
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue(queueName);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    spaceHandler(textMessage.getText());
                    //System.out.println("接收消息：" + textMessage.getText());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
