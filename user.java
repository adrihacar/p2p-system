
class User {
    private String userName;
    private String ip;
    private int port;

    //Default constructor
    public User (){
        this.userName = "null";
        this.ip = "0.0.0.0";
        this.port = -1;
    }

    public User(String name, String ipAddr, int portNum){
        this.userName = name;
        this.ip = ipAddr;
        this.port = portNum;
    }

    public static User findUserByName(User[] list, String uname){
        User target = null;
        for(int i = 0; i < list.length; i++){
            if(list[i].userName.equals(uname)){
                target = list[i];
                break;
            }
        }
        return target;
    }


    public String get_userName(){
        return this.userName;
    }

    public String get_ip(){
        return this.ip;
    }

    public int get_port(){
        return this.port;
    }

    public void set_userName(String name){
        this.userName = name;
    }

    public void set_ip(String ipAddr){
        this.ip = ipAddr;
    }

    public void set_port(int portNum){
        this.port = portNum;
    }

    public String toString(){
        String str = "";
        str += this.userName + "\t\t";
        str += this.ip + "\t";
        str += String.valueOf(this.port) + "\t";

        return str;
    }
}