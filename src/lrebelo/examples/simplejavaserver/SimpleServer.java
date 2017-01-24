package lrebelo.examples.simplejavaserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple socket server example
 * 	It receives a connection and replies to the client App
 *
 *	@author Luis Monteiro Rebelo
 *			l.rebelo@kingston.ac.uk
 *	@since October 2012
 */

public class SimpleServer
{
	
	ServerSocket server = null;
	boolean keepgoing = true;
	
	public static void main(String[] args)
	{
		SimpleServer siplServ = new SimpleServer();
		siplServ.listenSocket();	//call the function that will handle the setting up of the receiving port
	}
	
	SimpleServer() //empty constructor for great justice!
	{
	}
	
	public void listenSocket()
	{
		
		ServerThread sckTh;
			
        try{
        	server = new ServerSocket(8888); //starts listening to specefied port!
            System.out.println("\n Server Starts listening on port 8888");

        } catch (IOException e) { //if port not availibel then trow exception and display msg!
            System.out.println("Could not listen on port 8888");
            System.exit(-1);//and then quit application!!
        }
     
        do{ 
            try{
                Socket sock = server.accept(); //if serversock stuff went ok then try and accept the indevidual connections!
				
                sckTh = new ServerThread(sock, this);
				Thread serverT = new Thread(sckTh);
				serverT.start(); //this creates a thread for each of the 
            } catch (IOException e) {
                System.out.println("Accept failed: 8888");
                System.exit(-1);
            }
        }while(keepgoing);
     }
	
    protected void finalize()
	{
        try{
            server.close();
        } catch (IOException e) {
        	System.out.println("Could not close socket");
        	System.exit(-1); 
        }
    }
}

class ServerThread implements Runnable 
{
	Socket thrSocket;
	SimpleServer me;
	ServerThread(Socket tmpSock, SimpleServer meTmp)
	{
		this.thrSocket = tmpSock;
		this.me = meTmp;
	}

	public void run()
	{
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(thrSocket.getInputStream()));
			PrintWriter out = new PrintWriter(thrSocket.getOutputStream());
			String inp = in.readLine();
			
			if(inp.equals("hello")){
				System.out.println("received msg: "+inp);
				out.print("back at you!");
			}else{
				if((inp.substring(0, 3)).equals("db;")){
					String msg = inp.substring(3);
					
					String[] msgB = msg.split(";");
					String a = msgB[0];
					String b = msgB[1];
					
					System.out.println("db connect! msg 1: "+a +" - msg 2: "+b);
					dbAccess sdb = new dbAccess(a, b);
					String rtnmsg = sdb.conn();
					out.print(rtnmsg);
					
				}else{
					if(inp.equals("quit")){
						me.keepgoing = false;
						System.out.println("I was told to "+inp +" drinking..");
						out.print("quiting...");
						out.flush();
						System.exit(-1);
					}else{
						System.out.println("dont know what to do! ");
						out.print("Come again!?");
					}
				}
			}

			out.flush();

		}catch (IOException e){
			System.out.println("problems in reading/writing stuff to socket: "+e);
			System.exit(-1);
		}
		
		finally
		{
			try{
				thrSocket.close();
				//System.exit(-1);
			}catch (IOException e) {
				System.out.println("Could not close socket");
				System.exit(-1);
			}
		}
	}
}

class dbAccess
{
	private String name = null;
	private String msg = null;
	
	dbAccess(String a, String b){
		name = a;
		msg = b;
	}
	
	String conn() {
		String tmp = "returned";
        try {
        	
        	Connection connect = null;
    		Statement stat = null;
    		
	        try {
	    		Class.forName("com.mysql.jdbc.Driver");
				connect = DriverManager.getConnection("jdbc:mysql://localhost/msg", "root", "");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
	        
	        stat = connect.createStatement();
	   	   	stat.execute("INSERT INTO msgTable ( name, msg) VALUES ( '" +name +"', '" +msg +"' )");
	
	   	   	stat.close();
	   	   	connect.close();
	   	   	
	   	 tmp = "Values saved to DB";
	   	   	
        } catch (SQLException e) {
			e.printStackTrace();
			tmp = "Failed to saved on to DB";
		}
        
        return tmp;
	}
}