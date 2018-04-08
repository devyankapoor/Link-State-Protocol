package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import socs.network.message.AlivePacket;

public class HeartBeatAgentClientThread extends Thread  //sends alive packet
{

	RouterDescription rdServer;
	short senderPort;
	String senderSimulatedIPAddress;
	RouterDescription router=null;
	short pp;
    String s=null;

	public HeartBeatAgentClientThread(RouterDescription rdServer,RouterDescription router)  //sends heart beat to particular client 	
	{

		this.rdServer=rdServer;
		this.senderPort=rdServer.processPortNumber;
		this.senderSimulatedIPAddress=rdServer.simulatedIPAddress;
		this.router=router;
		pp= router.processPortNumber;
	}


	public void run()
	{
      try {
		Socket sock= new Socket("localhost",pp);
		AlivePacket ap= new AlivePacket();
		//ap.dstIP=router.simulatedIPAddress;
		ap.dstProcessPort=router.processPortNumber;
		ap.srcIP=rdServer.simulatedIPAddress;
		ap.dstIP=router.simulatedIPAddress;
		ap.srcProcessPort= rdServer.processPortNumber;
		ObjectInputStream din= new ObjectInputStream(sock.getInputStream());
		ObjectOutputStream dout = new ObjectOutputStream(sock.getOutputStream());
		s=ap.dstIP;
		dout.writeObject(ap);
		dout.flush();
		sock.close();
		
		
		
		
		
		
	}  catch (Exception e) {
		// TODO Auto-generated catch block
		//e.printStackTrace();
		//System.out.println(s+" Not found");
	}
       


	}

	}
