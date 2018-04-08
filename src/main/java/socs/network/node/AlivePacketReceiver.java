package socs.network.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.Vector;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;

public class AlivePacketReceiver extends TimerTask   //used for monitoring
{
	RouterDescription rdServer;
	short processPortNumber;
	 ObjectInputStream din;
	 ObjectOutputStream dout;
	String clientAddress=null;
	Link ports[]=null;
	Socket sock=null;
	int count=0;
	String client_quit_address;
	//HashMap<String,String> Router.temp_status= new  HashMap<String,String>();
	AlivePacketReceiver(short processPortNumber,RouterDescription rdServer, Link ports[])
	{
		this.rdServer=rdServer;
		this.processPortNumber=processPortNumber;
		this.ports=ports;
	}



	public void run()
	{
		/*if(count==0)
		{
			Router.temp_status=Router.status;
			count++;
		}
		else*/ if (!Router.status.isEmpty())
		{
		
	label1:	for(Map.Entry<String,String> status: Router.status.entrySet())
		{
			 if (!Router.temp_status.containsKey(status.getKey()))  //if it does nt contain key
	    	{
	    	Router.temp_status.put(status.getKey(),status.getValue());
	    	continue label1;
	    	}
			 
			 else //contains key
			 {
			label : for(Map.Entry<String,String> temp_status1: Router.temp_status.entrySet())  // check in temp status
			{	
			    if(Router.temp_status.containsKey(status.getKey()) && status.getKey().equals(temp_status1.getKey()) )
			    {
			    	if(!status.getValue().equals(temp_status1.getValue()))
			    			{
			    					
			    		           Router.temp_status.remove(temp_status1.getKey());
			    		           Router.temp_status.put(temp_status1.getKey(),status.getValue());
			    		           break label;
			    			}
			    	else
			    	{
			    		for(int i=0;i<4;i++)
			    		{
			    			if(ports[i]!=null && ports[i].router2.simulatedIPAddress.equals(status.getKey()))
			    			{
			    				//Router.status.remove(status.getKey());
			    				
			    				LSA temp_lsa=new LSA();
								temp_lsa.linkStateID=rdServer.simulatedIPAddress;
								for(int j=0; j<4; j++)
								{
									if(Router.ports[j]!=null && !Router.ports[j].router2.simulatedIPAddress.equals(status.getKey())){
										LinkDescription ld= new LinkDescription();
										ld.linkID=Router.ports[j].router2.simulatedIPAddress;
										ld.tosMetrics=Router.ports[j].weight;
										ld.portNum=Router.ports[j].router2.processPortNumber;

										temp_lsa.links.add(ld);					
									}
								}///Server send LSUpdate first to client only

								Router.current_seq_nbr=Router.incrementCount();
								temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
								Router.lsd._store.remove(rdServer.simulatedIPAddress);
								Router.lsd._store.put(rdServer.simulatedIPAddress,temp_lsa);
								Router.lsd._store.remove(status.getKey());
								SOSPFPacket spf= new SOSPFPacket();
								spf.lsa= new Vector<LSA>();		
								for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
								{
									
									spf.lsa.add(entry.getValue());

								}
								Router.ports[i]=null;
								ports[i]=null;
								for(int k=0; k<4; k++)
								{


									if(Router.ports[k]!=null)
									{
										spf.dstIP=Router.ports[k].router2.simulatedIPAddress;
										
										spf.dstProcessPort= Router.ports[k].router2.processPortNumber;
									
										spf.srcIP=rdServer.simulatedIPAddress;
										spf.srcProcessPort=rdServer.processPortNumber;
										spf.routerID=status.getKey();
										spf.sospfType=12;
										//LSA to be added
										
										ClientChildThread cl1 = new ClientChildThread(spf.dstIP,spf.dstProcessPort,rdServer,spf);
										cl1.start();

									}

								}
			    				break label;
			    			}
			    		}
			    		
			    		
			    	
			    	}
			    	
			    	//break label1 ;
			    }  // if it contains key
			    
			    
			    
			}
			 }
			}
		}
	}
	
	
	
	
}
