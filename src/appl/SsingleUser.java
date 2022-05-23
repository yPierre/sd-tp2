package appl;

import core.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class SsingleUser {


    public static void main(String[] args) {
        // TODO Auto-generated method stub
        new SsingleUser();
    }

    public SsingleUser(){
        Scanner reader = new Scanner(System.in);  // Reading from System.in
        System.out.print("Enter the Broker port number: ");
        int brokerPort = reader.nextInt();

        //System.out.print("Enter the Broker address: ");
        //String brokerAdd = reader.next();

        System.out.print("Enter the User name: ");
        String userName = reader.next();

        System.out.print("Enter the User port number: ");
        int userPort = reader.nextInt();

        //System.out.print("Enter the User address: ");
        //String userAdd = reader.next();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        PubSubClient user = new PubSubClient("localhost", userPort);

        user.subscribe("localhost", brokerPort);

        startTP2(user, userName, brokerPort, "localhost");
    }

    private void startTP2 (PubSubClient user, String userName, int brokerPort, String brokerAdd){
        System.out.println("User " + userName + " entered the system!\n");
        String[] resources = {"var X", "var Y", "var Z"};

        Random seed = new Random();

        for(int i =0; i<100; i++){
            boolean proceed = false;
            //fazendo um pub no broker
            String oneResource = resources[seed.nextInt(resources.length)];
            Thread sendOneMsg = new ThreadWrapper(user, userName+":access:"+"*", brokerAdd, brokerPort);

            sendOneMsg.start();

            try{
                sendOneMsg.join();
            }catch (Exception e){
                e.printStackTrace();
            }

            while(!proceed) {
                List<Message> logUser = user.getLogMessages();
                proceed = treatLog(logUser, userName);

                try {
                    Thread.sleep(500);
                    System.out.println(".");
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            //CONTENT
            sendOneMsg = new ThreadWrapper(user, userName + ":acquire:" + "*", brokerAdd, brokerPort);
            sendOneMsg.start();

            try{
                sendOneMsg.join();
            }catch (Exception e){
                e.printStackTrace();
            }

            System.out.println("The resource " + "*" + " is available for you.");
            try {
                int wait = (int) ((Math.random() * (5000 - 1000)) + 1000);
                Thread.sleep(wait);
                System.out.println("The resource " + "*" + " was used for " + wait / 1000 + " seconds.");
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            System.out.println("User Release resource!");
            sendOneMsg = new ThreadWrapper(user, userName + ":release:" + "*", brokerAdd, brokerPort);


            sendOneMsg.start();

            try{
                sendOneMsg.join();
            } catch (Exception e){
                e.printStackTrace();
            }

            try {
                int wait = (int) ((Math.random() * (5000 - 1000)) + 1000);
                System.out.println("Wait " + wait / 1000 + " seconds.");
                Thread.sleep(wait);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            //fazendo a obtencao dos notifies do broker
            /*
            List<Message> logUser = user.getLogMessages();

            treatLog(logUser);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
            */
        }


        user.unsubscribe(brokerAdd, brokerPort);

        user.stopPubSubClient();

    }

    private boolean treatLog(List<Message> logUser, String userName){
        //aqui existe toda a lógica do protocolo do TP2
        //se permanece neste método até que o acesso a VAR X ou VAR Y ou VAR Z ocorra
        Iterator<Message> it = logUser.iterator();
        int countAcquires = 0, countRelease = 0;
        System.out.print("Log User itens: ");
        while(it.hasNext()) {
            Message aux = it.next();
            System.out.print(aux.getContent() + aux.getLogId() + " | ");
            //Aqui fara a filtragem do log
            if (aux.getContent().contains("acquire:" + "*"))
                countAcquires++;
            if (aux.getContent().contains("release:" + "*"))
                countRelease++;
        }


        if(countRelease == countAcquires){
            Iterator<Message> isNow = logUser.iterator();
            while (isNow.hasNext() && countRelease >= 0){
                Message aux = isNow.next();

                if(aux.getContent().contains("access:" + "*")){
                    if(aux.getContent().contains(userName) && countRelease == 0)
                        return true;
                    else
                        countRelease--;
                }
            }
        }
        return false;
    }


    class ThreadWrapper extends Thread{
        PubSubClient c;
        String msg;
        String host;
        int port;

        public ThreadWrapper(PubSubClient c, String msg, String host, int port){
            this.c = c;
            this.msg = msg;
            this.host = host;
            this.port = port;
        }
        public void run(){
            c.publish(msg, host, port);
        }
    }
}
