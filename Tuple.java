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
        System.out.println( this.source );
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
        String ret = "";
        System.out.print("Send tuple (");
        for(int i=0;i<this.msg.size();i++){
            ret += this.msg.get(i);
            System.out.print(this.msg.get(i));
            if(i != this.msg.size()-1){
                ret+=" ";
                System.out.print(",");
            }
        }
        System.out.println(") back to client "+this.source+".");
        return ret;
    }
    public int compareTo(Tuple rcv){
        if(this.msg.equals(rcv.msg))
            return 1;
        else
            return 0;
    }
}
