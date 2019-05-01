import javax.jms.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQConnection;
import java.util.*;
class Tuple implements Comparable<Tuple>{
    Vector<String> msg;
    String cmd;
    String source;
    Tuple(){}
    Tuple(String[] msg,String source){
        int len = msg.length;
        this.cmd = msg[1];
        this.msg = new Vector<String>();
        for(int i=2;i<len;i++){
            this.msg.add(msg[i]);
        }
        this.source=source;
    }
    String getCMD(){
        return this.cmd;
    }
    String getSource(){
        return this.source;
    }
    void printRcvMessage(){
        System.out.print("Server receive tuple (");
        for(int i=0;i<msg.size();i++){

            System.out.print(msg.get(i));
            if(i != msg.size()-1){
                System.out.print(",");
            }
        }
        System.out.println(") from "+this.source+" and put it into tuple space.");
    }
    String getMessage(){
        String ret = "(";
        for(int i=0;i < this.msg.size();i++){
            ret += this.msg.get(i);
            if(i != this.msg.size()-1){
                ret+=",";
            }
        }
        ret += ")";
        return ret;
    }
    //取得傳送格式 msg 並印出傳送資訊
    String getSendMessage(){
        String ret = "(";
        System.out.print("Send tuple");
        for(int i=0;i<this.msg.size();i++){
            ret += this.msg.get(i);
            if(i != this.msg.size()-1){
                ret+=",";
            }
        }
        ret += ")";
        System.out.println(ret+" back to client "+this.source+".");
        return ret;
    }
    //override compareTo
    public int compareTo(Tuple target){
        if(this.msg.equals(target.msg)){
            return 1;
        }else{
            if(this.msg.size() == target.msg.size()){

                    //這裡面存的是 還不在這世界上但是可能會在的 assign pair
                    //如果最後return 1 那就要把這些存到 真正的 assignList 裡面
                Vector<String[]> toAssignList = new Vector<String[]>();
                int equal = 1;
                for(int i=0;i<this.msg.size();i++){


                    if(this.msg.get(i).equals(target.msg.get(i))){//兩段字串相同
                        continue;
                    }else if(this.msg.get(i).charAt(0)=='?' && target.msg.get(i).charAt(0)=='?'){//都是問號 不會通過
                        return 0;
                    }else if(this.msg.get(i).charAt(0)=='?'){
                        /*  client 要一個不知道的tuple => rcvtuple 是問號開頭
                        *   client 使用的是 in 或是 read
                        */
                        String[] tmparr = {this.msg.get(i).substring(1),target.msg.get(i)};
                        toAssignList.add(tmparr);
                        continue;
                    }else if(target.msg.get(i).charAt(0)=='?'){
                        /*  client 送出要求 suspendedList 某個 tuple 第 i 個字串是問號開頭
                        *   client 使用的是 out
                        *   我們要把送過來的東西中的值 assign 給 ?後面的變數
                        */
                        String[] tmparr = {target.msg.get(i).substring(1),this.msg.get(i)};
                        target.msg.set(i,this.msg.get(i));
                        toAssignList.add(tmparr);
                        continue;
                    }else{
                        equal = 0;
                        break;
                    }
                }
                if(equal == 1){
                    for(int i=0;i<toAssignList.size();i++){
                        boolean newassign = true;
                        for(int j=0;j<server_linda.assignList.size();j++){
                            if(toAssignList.get(i)[0].equals( server_linda.assignList.get(j)[0] ) ){
                                server_linda.assignList.set( j , toAssignList.get(i) );
                                newassign = false;
                                break;
                            }
                        }
                        if(newassign){
                            server_linda.assignList.add( toAssignList.get(i) );
                        }
                    }
                }
                return equal;
            }
            return 0;
        }
    }
}
public class server_linda{
    private static final String url = "tcp://localhost:61616";
    private static final String queueName = "SendFromClinet";
    public static Vector<Tuple> space;
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
        System.out.println("Linda Space status :");
        for(int i=0;i<space.size();i++){
            System.out.print(space.get(i).getMessage());
            if(i != space.size()-1){
                System.out.print(" ");
            }
        }
        System.out.println();
    }
    //用來控制 space內部狀況
    public static void spaceHandler(String input_string)  throws JMSException {
        String[] tmpArray = input_string.split(" ");    //將輸入先存到一個陣列裡面
        String source = tmpArray[0]; //第一個會是來源
        String cmd = tmpArray[1];   //第二個會是指令
        //先檢查要不要 assign
        for(int i = 2; i < tmpArray.length; i++){
            //不是正常輸入格式
            if(!tmpArray[i].matches("-?\\d+(\\.\\d+)?") &&( tmpArray[i].charAt(0)!='\"' || tmpArray[i].charAt(0)!='\"')  ){
                if(tmpArray[i].charAt(0) == '?'){
                    continue;
                    //第一個有問號 之後可能會生成 assign pair
                    //在剛輸入的時候可以容許存進去吧
                    //這也是唯一正常的例外
                }
                boolean isErr=true;
                for( int j = 0;j < assignList.size(); j++ ){
                    if(assignList.get(j)[0].equals(tmpArray[i])){ //Key 相同 就用value取代
                        tmpArray[i] = assignList.get(j)[1];
                        isErr = false;
                    }
                }
                if(isErr){
                    sendToClient(source,"Server get error input.");
                    System.out.println("Server get error input.");
                    return;
                }
                //tmpArray[i] = find(tmpArray[i]);
                //TODO 假設不是數字也不是字串 => 要 assign
            }
        }
        Tuple rcvTuple = new Tuple(tmpArray,source);
        if(cmd.equals("out")){
            sendToClient("Server receive tuple.",source);
            rcvTuple.printRcvMessage();
            boolean forward = false;
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
                    System.out.println("Server forward the tuple to "+source+" to solve the suspended list.");
                    forward = true;
                    break;
                }
            }
            if(!forward){
                space.add(rcvTuple);
            }
            showSpace();
        }else if(cmd.equals("read") || cmd.equals("in")){
            boolean suspended = true;       //儲存是否找不到
            for(int i=0;i<space.size();i++){
                if(rcvTuple.compareTo(space.get(i)) == 1){
                    suspended = false;
                    String ret_str = space.get(i).getSendMessage(); //已經是可以傳回去的String了
                    String ret_target = source;
                    //如果是 in 就要拿走 tuple
                    if(cmd.equals("in")){
                        space.remove(i);
                        showSpace();
                    }
                    //把這個tuple send 給 clinet
                    sendToClient(ret_str,ret_target);
                    break;
                }
            }
            if(suspended){  //如果找不到
                suspendedList.add(rcvTuple);
                System.out.println("Can not find target tuple , The process of "+source+" suspended.");
            }
        }else{
            sendToClient(source,"Server get error input.");
            System.out.println("Server get error input.");
        }
    }
    public static void main(String[] args)  throws JMSException {
        System.out.println("Hello! I'm Server ya !");
        space = new Vector<Tuple>();    //Linda space
        suspendedList = new Vector<Tuple>();   //用來儲存那些想 in 和 read 但是沒有成功的 Operation
        /*
            這個assigned 裡面 key value
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
