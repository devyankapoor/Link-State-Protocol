package socs.network.node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Map;
import java.util.Vector;

import socs.network.message.AlivePacket;
import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;

public class ServerThread extends Thread
{
	ServerSocket ss;
	static Socket s;
	static ObjectInputStream din;
	static ObjectOutputStream dout;
	static String simulatedIP;
	static short processPortNumber;
	static ClientChildThread[] cliThread= new ClientChildThread[4];
	static RouterDescription rdServer;
	static RouterDescription[] rdCLients=new RouterDescription[4];
	static Link[] ports;
	static int count=0;
	static int flag=0;
	public static Socket sarr[]=new Socket[4];
	public int notlisten;
	boolean f;


	public ServerThread(String simulatedIP,short processPortNumber,RouterDescription rd,Link[] ports,int notlisten)
	{

		this.processPortNumber=processPortNumber;
		this.simulatedIP=simulatedIP;
		this.rdServer=rd;
		this.ports=ports;
		this.notlisten=notlisten;

	}

	public void run()

	{
		try {
			ss=new ServerSocket(processPortNumber);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		try{

		while(true)
			{
				
				if(Router.ports.length<5)
					s= ss.accept();
					
				
				dout=new ObjectOutputStream(s.getOutputStream());
				din= new ObjectInputStream(s.getInputStream());
				
				 Object obj =din.readObject();

			     if(obj instanceof SOSPFPacket){
				SOSPFPacket spf= (SOSPFPacket) 	obj;
				

				if(spf.sospfType==0)
				{
					System.out.println("received Hello from "+spf.srcIP);
					System.out.println("set "+spf.srcIP+" state to INIT");
					//Router.neighbors.add(spf.srcIP);

					for(int i=0; i<4; i++)
					{

						if( Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(spf.srcIP)){
							f=false;
							Router.ports[i].router2.status=RouterStatus.TWO_WAY;	
						}
					}

					for(int i=0; i<4; i++)
					{


						if(Router.ports[i]==null && !f)
						{
							RouterDescription rd_2  = new RouterDescription();
							rd_2.simulatedIPAddress=spf.srcIP;
							rd_2.processPortNumber=spf.srcProcessPort;

							Router.ports[i]=new Link(rdServer,rd_2);
							Router.ports[i].weight=spf.weight;
							break;
						}

					}

					spf.sospfType=1;
					String temp=spf.srcIP;
					spf.srcIP=spf.dstIP;
					spf.dstIP=temp;		

					short temp1=spf.srcProcessPort;
					spf.srcProcessPort=spf.dstProcessPort;
					spf.dstProcessPort=temp1;
					notlisten=0;
					ClientChildThread cl = new ClientChildThread(spf.dstIP,spf.dstProcessPort,rdServer,spf);
					cl.start();					
					cl.setName("Client "+spf.srcIP);
				}
				
				else if(spf.sospfType==1){

					System.out.println("received Hello from "+spf.srcIP);
					System.out.println("set "+spf.srcIP+" state to TWO_WAY");
					LSA temp_lsa=new LSA();
					temp_lsa.linkStateID=rdServer.simulatedIPAddress;
					for(int i=0; i<4; i++)
					{
						if(Router.ports[i]!=null){
							LinkDescription ld= new LinkDescription();
							ld.linkID=Router.ports[i].router2.simulatedIPAddress;
							ld.tosMetrics=Router.ports[i].weight;
							ld.portNum=Router.ports[i].router2.processPortNumber;

							temp_lsa.links.add(ld);					
						}
					}///Server send LSUpdate first to client only

					Router.current_seq_nbr=Router.incrementCount();
					temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
					Router.lsd._store.remove(rdServer.simulatedIPAddress);
					Router.lsd._store.put(rdServer.simulatedIPAddress,temp_lsa);

					for(int i=0; i<4; i++)
					{

						if( Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(spf.srcIP))
							Router.ports[i].router2.status=RouterStatus.TWO_WAY;
					}
					
					// Changing address of packet read (swapping)
					spf.sospfType=2;
					//Router.neighbors.add(spf.srcIP);
					String temp1=spf.srcIP;
					spf.srcIP=spf.dstIP;
					spf.dstIP=temp1;
					short temp2=spf.srcProcessPort;
					spf.srcProcessPort=spf.dstProcessPort;
					spf.dstProcessPort=temp2;

					Router.current_seq_nbr=Router.incrementCount();
					temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
					Router.lsd._store.remove(rdServer.simulatedIPAddress);
					Router.lsd._store.put(rdServer.simulatedIPAddress,temp_lsa);
					
					spf.lsa= new Vector<LSA>();
					spf.lsa.add(temp_lsa);
					
					ClientChildThread cl1 = new ClientChildThread(spf.dstIP,spf.dstProcessPort,rdServer,spf);
					cl1.start();

				}

				else if(spf.sospfType==2)
				{
					System.out.println("received Hello from "+spf.srcIP);
					System.out.println("set "+spf.srcIP+" state to TWO_WAY");

                    if(spf.lsa!=null)
                    {
                    	Router.lsd._store.put(spf.srcIP,spf.lsa.firstElement());
                    }
					
					/** Sending LSAupdate packet By  **/


					// Devise LSA 
					LSA temp_lsa=new LSA();
					temp_lsa.linkStateID=rdServer.simulatedIPAddress;
					for(int i=0; i<4; i++)
					{
						if(Router.ports[i]!=null){
							LinkDescription ld= new LinkDescription();
							ld.linkID=Router.ports[i].router2.simulatedIPAddress;
							ld.tosMetrics=Router.ports[i].weight;
							ld.portNum=Router.ports[i].router2.processPortNumber;  // change to i 

							temp_lsa.links.add(ld);					
						}
					}

					Router.current_seq_nbr=Router.incrementCount();
					temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
					Router.lsd._store.remove(rdServer.simulatedIPAddress);
					Router.lsd._store.put(rdServer.simulatedIPAddress,temp_lsa);
//////////////////////////////////////////////////////////////////////
					RouterDescription rd=null;
				//	 Router.status.put(spf.srcIP,(LocalDateTime.now ( ).toString ()));
			    	 for(int i=0;i<4;i++)
			    	 {
			    		 if(Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(spf.srcIP))
			    		 {
			    			 rd=Router.ports[i].router2;
			    			 
			    			 break;
			    		 }
			    	 }
					
			    	 if(rd!=null)
			    	 {
					 HeartBeatAgentClientThread dd= new HeartBeatAgentClientThread(rdServer, rd);
			    	 dd.start();
			    	 }
	/////////////////////////////////////////////////////////////////////////////////////////////		
					
					for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
					{
						
						spf.lsa.add(entry.getValue());

					}

  
					for(int i=0; i<4; i++)
					{


						if(Router.ports[i]!=null)
						{
							spf.dstIP=Router.ports[i].router2.simulatedIPAddress;
							spf.srcIP=rdServer.simulatedIPAddress;
							spf.dstProcessPort= Router.ports[i].router2.processPortNumber;
							spf.srcProcessPort=rdServer.processPortNumber;
							spf.routerID=rdServer.simulatedIPAddress;
							
							spf.sospfType=3;
							//LSA to be added

							ClientChildThread cl1 = new ClientChildThread(spf.dstIP,spf.dstProcessPort,rdServer,spf);
							cl1.start();

						}

					}


				}
				
				
				/****************************************************************************/
				else if(spf.sospfType==10)
				{
					
					
					Boolean flooding=false;
					for(LSA l : spf.lsa)
					{
						// LSA l=itr.next();
						LSA lsa_prev= Router.lsd._store.get(l.linkStateID);
						//LSA lsa_server=Router.lsd._store.get(rdServer.simulatedIPAddress);
						if(lsa_prev==null)
						{
							Router.lsd._store.put(l.linkStateID, l);             
							flooding=true;
						}
						else if (lsa_prev.lsaSeqNumber < l.lsaSeqNumber)
						{


							Router.lsd._store.remove(l.linkStateID);
							Router.lsd._store.put(l.linkStateID, l);
							flooding=true; 

						}
						else 
							flooding=false;


					}
					
					int source=0;
					for(int i=0; i<4; i++)
					{
						if(Router.ports[i]!=null )
						{
							if(Router.ports[i].router2.simulatedIPAddress.equals(spf.srcIP))
							source=i;
							break;
						}
						
					}
					
					LSA temp_lsa=new LSA();
					temp_lsa.linkStateID=rdServer.simulatedIPAddress;
					for(int i=0; i<4; i++)
					{
						if(Router.ports[i]!=null && i!=source){
							
							LinkDescription ld= new LinkDescription();
							ld.linkID=Router.ports[i].router2.simulatedIPAddress;
							ld.tosMetrics=Router.ports[i].weight;
							ld.portNum=Router.ports[i].router2.processPortNumber;  // change to i 

							temp_lsa.links.add(ld);	
							
						}
					}

					Router.current_seq_nbr=Router.incrementCount();
					temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
					Router.lsd._store.remove(rdServer.simulatedIPAddress);
					Router.lsd._store.put(rdServer.simulatedIPAddress,temp_lsa);
					
					SOSPFPacket spf1= new SOSPFPacket();
					spf1.lsa= new Vector<LSA>();
				//	spf1.lsa.add(temp_lsa);
					for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
					{
						
						spf1.lsa.add(entry.getValue());

					}
					
					for(int i=0; i<4; i++)
					{


						if(Router.ports[i]!=null)
						{
							spf1.dstIP=Router.ports[i].router2.simulatedIPAddress;
							
							spf1.dstProcessPort= Router.ports[i].router2.processPortNumber;
							if(i==source){
								Router.ports[i]=null;}
							spf1.srcIP=rdServer.simulatedIPAddress;
							spf1.srcProcessPort=rdServer.processPortNumber;
							spf1.routerID=rdServer.simulatedIPAddress;
							spf1.sospfType=3;
							//LSA to be added
									
							ClientChildThread cl1 = new ClientChildThread(spf1.dstIP,spf1.dstProcessPort,rdServer,spf1);
							cl1.start();

						}

					}
					
				}
				
				
				
				else if(spf.sospfType==3)
				{
					

					Boolean flooding=false;


			
					for(LSA l : spf.lsa)
					{
				
						LSA lsa_prev= Router.lsd._store.get(l.linkStateID);
					
						if(lsa_prev==null)
						{
							Router.lsd._store.put(l.linkStateID, l);             
							flooding=true;
						}
						else if (lsa_prev.lsaSeqNumber < l.lsaSeqNumber)
						{


							Router.lsd._store.remove(l.linkStateID);
							Router.lsd._store.put(l.linkStateID, l);
							flooding=true; 

						}
						else 
							flooding=false;


					}

					if(flooding)
					{


						SOSPFPacket spf1= new SOSPFPacket();
						spf.lsa = new Vector<LSA>();
						for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
						{
							spf.lsa.add(entry.getValue());

						}

						spf1.lsa=spf.lsa;
						for(int i=0; i<4; i++)
						{

							if(Router.ports[i]!=null)
							{

								spf1.srcIP= rdServer.simulatedIPAddress;
								spf1.dstIP= Router.ports[i].router2.simulatedIPAddress;
								spf1.dstProcessPort=Router.ports[i].router2.processPortNumber;
								spf1.srcProcessPort=rdServer.processPortNumber;
								spf1.sospfType=3;

								spf1.routerID=spf.routerID;
								ClientChildThread cl1 = new ClientChildThread(spf1.dstIP,spf1.dstProcessPort,rdServer,spf1);
								cl1.start();
							}

						}
					}


				}
				
				else if(spf.sospfType==12)  // packet to remove entry on server getting quit 
					//receiving the packet
				{
					String s=spf.routerID;

					Boolean flooding=false;


			
					for(LSA l : spf.lsa)
					{
				
						LSA lsa_prev= Router.lsd._store.get(l.linkStateID);
					
						if(lsa_prev==null)
						{
							Router.lsd._store.put(l.linkStateID, l);             
							flooding=true;
						}
						else if (lsa_prev.lsaSeqNumber < l.lsaSeqNumber)
						{


							Router.lsd._store.remove(l.linkStateID);
							Router.lsd._store.put(l.linkStateID, l);
							flooding=true; 

						}
						else 
							flooding=false;


					}
					
					if(Router.lsd._store.containsKey(spf.routerID))
						Router.lsd._store.remove(spf.routerID);

					if(flooding)
					{


						SOSPFPacket spf1= new SOSPFPacket();
						spf.lsa = new Vector<LSA>();
						for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
						{
							spf.lsa.add(entry.getValue());

						}

						spf1.lsa=spf.lsa;
						for(int i=0; i<4; i++)
						{

							if(Router.ports[i]!=null)
							{

								spf1.srcIP= rdServer.simulatedIPAddress;
								spf1.dstIP= Router.ports[i].router2.simulatedIPAddress;
								spf1.dstProcessPort=Router.ports[i].router2.processPortNumber;
								spf1.srcProcessPort=rdServer.processPortNumber;
								spf1.sospfType=3;

								spf1.routerID=spf.routerID;
								ClientChildThread cl1 = new ClientChildThread(spf1.dstIP,spf1.dstProcessPort,rdServer,spf1);
								cl1.start();
							}

						}
					}
				
				} // else if sostype =12
				
			     }// end of SOSPFPACKET
			     
			     else if (obj instanceof AlivePacket)
			     {
			    	 AlivePacket ap= (AlivePacket) obj;
			    	// Router.status.put(ap.srcIP,(LocalDateTime.now ( ).toString ()));
			    	 if(!Router.status.containsKey(ap.srcIP))
			    	 Router.status.put(ap.srcIP,(LocalDateTime.now ( ).toString ()));
			    	 else
			    		 Router.status.replace(ap.srcIP,(LocalDateTime.now ( ).toString ())); 
		
			    	 
			    	 RouterDescription rd=null;
			    	 
			    	 for(int i=0;i<4;i++)
			    	 {
			    		 if(Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(ap.srcIP))
			    		 {
			    			 rd=Router.ports[i].router2;
			    			 
			    			 break;
			    		 }
			    	 }
			    	 if(rd!=null){
			    		 Thread.sleep(500);
			    	 HeartBeatAgentClientThread cl= new HeartBeatAgentClientThread(rdServer,rd );
			    	 cl.start();
			    	 }
			     }
			      
			     
			}// while
			
			}  //try
		

		catch (Exception e)
		{
			e.printStackTrace();				
		}




	}

	public static void addClient(Link[] ports)
	{
		for(int i=0;i <4;i++)
		{
			if(ports[i]!=null && ports[i].router2.processPortNumber!=0 && (ports[i].router2.status==RouterStatus.NO))
			{
				SOSPFPacket spf =new SOSPFPacket();
				spf.srcIP=rdServer.simulatedIPAddress;
				spf.dstIP=ports[i].router2.simulatedIPAddress;
				spf.dstProcessPort=ports[i].router2.processPortNumber;
				spf.srcProcessPort=rdServer.processPortNumber;
				spf.weight=ports[i].weight;
				spf.sospfType=0;
				cliThread[i] = new ClientChildThread(ports[i].router2.simulatedIPAddress,ports[i].router2.processPortNumber,rdServer,spf);
				cliThread[i].setName("Client "+ports[i].router2.simulatedIPAddress);
				try{
					cliThread[i].start();

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				rdCLients[i]=ports[i].router2;
				rdCLients[i].simulatedIPAddress=ports[i].router2.simulatedIPAddress;


			}

		}

	}

}

