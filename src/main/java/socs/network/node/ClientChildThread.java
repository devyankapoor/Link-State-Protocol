package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import socs.network.message.SOSPFPacket;

public class ClientChildThread extends Thread
{
	Socket sock;
	short portNumber;
	boolean m_bRunThread = true; 
	String serverName;
	RouterDescription rdServer;
	String simulatedIP;
	SOSPFPacket spf;
	/*public ClientChildThread()
	{
		super();
	}*/

	public ClientChildThread(String simulatedIP,short portNumber,RouterDescription rd,SOSPFPacket p)
	{
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.portNumber=portNumber;
		sock=null;
		this.serverName=rd.simulatedIPAddress;
		this.rdServer=rd;
		this.simulatedIP=simulatedIP;
		this.spf=p;

	}

	public void run()
	{

		try
		{
			
			sock= new Socket("localhost", portNumber); 		
			spf.srcProcessPort= rdServer.processPortNumber;
			ObjectInputStream din= new ObjectInputStream(sock.getInputStream());
			ObjectOutputStream dout = new ObjectOutputStream(sock.getOutputStream());
			
			dout.writeObject(spf);
			dout.flush();
			sock.close();
			

		}
		catch(Exception e)
		{
			//e.printStackTrace();
			//System.out.println("Please increase time in child thread");
		}

	}


}

