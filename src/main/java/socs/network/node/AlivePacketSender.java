package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import socs.network.message.AlivePacket;

public class AlivePacketSender extends Thread
{
	Socket sock;
	short portNumber;
	boolean m_bRunThread = true; 
	String serverName;
	RouterDescription rdServer;
	String simulatedIP;
	AlivePacket spf;
	/*public ClientChildThread()
	{
		super();
	}*/

	public AlivePacketSender(String simulatedIP,short portNumber,RouterDescription rd,AlivePacket p)
	{
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
			
		}

	}


}

