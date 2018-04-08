package socs.network.node;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;
import socs.network.util.Configuration;


public class Router {

  static LinkStateDatabase lsd;
  Link l;
  RouterDescription rd_c;
  static String mainserver=null;
  static Boolean Quitflag=false;
  static Boolean quitROuter=false;
 static Thread t1;
 Thread t2;
 static int count=0;
  Thread t3;
 static  Map<String,String> status= new  ConcurrentHashMap<String,String>();
 static  Map<String,String> temp_status= new  ConcurrentHashMap<String,String>();
 static int notlisten=0;
 static int current_seq_nbr=0;
 static int totalclient;
  public static ArrayList<String> neighbors=new ArrayList<String>();
  public RouterDescription rd = new RouterDescription();
boolean ff;
AlivePacketReceiver al=null;
  //assuming that all routers are with 4 ports
  static  Link[] ports = new Link[4];
  Timer timer=null;

  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    Thread.currentThread().setPriority(1);
    lsd=new LinkStateDatabase(rd);
    rd.processPortNumber=Short.parseShort(config.getString("port"));
    t1 = new ServerThread(rd.simulatedIPAddress,rd.processPortNumber,rd,ports,0);
	t1.start();
	t1.setName(rd.simulatedIPAddress);
 al= new AlivePacketReceiver(rd.processPortNumber,rd,ports);
	 timer = new Timer(true);
	timer.scheduleAtFixedRate(al, new Date(), 15000);
	
  }
  
  public static  int incrementCount() {
	  current_seq_nbr++;
	  
	  return current_seq_nbr;
	  
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {
      Router.lsd.getShortestPath(destinationIP);
	 // System.out.println(lsd.toString());
  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) 
  {

		  if(Router.ports[portNumber]!=null)
		  {
			 // ports[portNumber].router2.status=RouterStatus.DISCONNECT;
			  SOSPFPacket spf =new SOSPFPacket();
				spf.srcIP=rd.simulatedIPAddress;
				spf.dstIP=Router.ports[portNumber].router2.simulatedIPAddress;
				spf.dstProcessPort=Router.ports[portNumber].router2.processPortNumber;
				spf.srcProcessPort=rd.processPortNumber;
				spf.weight=Router.ports[portNumber].weight;
				spf.sospfType=10;
			 
				try{
					
					
					LSA temp_lsa=new LSA();
					temp_lsa.linkStateID=rd.simulatedIPAddress;
					for(int i=0; i<4; i++)
					{
						if(Router.ports[i]!=null && i!=portNumber){
							LinkDescription ld= new LinkDescription();
							ld.linkID=Router.ports[i].router2.simulatedIPAddress;
							ld.tosMetrics=Router.ports[i].weight;
							ld.portNum=Router.ports[i].router2.processPortNumber;  // change to i 

							temp_lsa.links.add(ld);					
						}
					}
					//Router.ports[portNumber]=null;
					Router.current_seq_nbr=Router.current_seq_nbr+1;
					temp_lsa.lsaSeqNumber=	Router.current_seq_nbr;
					Router.lsd._store.remove(rd.simulatedIPAddress);
					Router.lsd._store.put(rd.simulatedIPAddress,temp_lsa);
					spf.lsa = new Vector<LSA>();
				//	spf.lsa.add(temp_lsa);
					for(Map.Entry<String,LSA> entry: Router.lsd._store.entrySet())
					{
						spf.lsa.add(entry.getValue());

					}
					ClientChildThread	cliThread = new ClientChildThread(Router.ports[portNumber].router2.simulatedIPAddress,Router.ports[portNumber].router2.processPortNumber,rd,spf);
					 cliThread.setName("Client Disconnect "+Router.ports[portNumber].router2.simulatedIPAddress);
					 cliThread.start();
					 Router.ports[portNumber]=null;
					 Thread.sleep(1000);
					 /*for(int i=0; i<4; i++)
						{

							if(Router.ports[i]!=null)
							{

								spf.srcIP= rd.simulatedIPAddress;
								spf.dstIP= Router.ports[i].router2.simulatedIPAddress;
								spf.dstProcessPort=Router.ports[i].router2.processPortNumber;
								spf.srcProcessPort=rd.processPortNumber;
								spf.sospfType=3;

								spf.routerID=rd.simulatedIPAddress;
								ClientChildThread cl1 = new ClientChildThread(spf.dstIP,spf.dstProcessPort,rd,spf);
								cl1.start();
							}

						}*/
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		  }
		  else
			  System.out.println("Port number can have values 0 to 3");
	 

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to identify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
  private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
	  
	  for(int i=0; i<4; i++)
		{
		
		    if( Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(simulatedIP))
		    	ff=true;
		}
		
	    
	
      for(int i=0; i <4; i++)
      {
    	  /*if(ports[i].router1.simulatedIPAddress.equals(simulatedIP) && ports[i]!=null)
    	  {
    		  System.out.println("Invalid Client IP Address. IP Address cannot be same as that of server/host");
    		  break;
    	  }*/
      if(ff) 
    	  {
    		  System.out.println("Connection already exists");
    		  break;
    	  }
    	  
      else if(Router.ports[i]==null && processPort > 1023 && processPort<=65535)
    	  {
    	  
    	  rd_c = new RouterDescription();
		  rd_c.simulatedIPAddress= simulatedIP;
		  rd_c.processIPAddress=processIP;
		  rd_c.processPortNumber=processPort;
		  rd_c.status=RouterStatus.NO;
		  Router.ports[i] =new Link(rd,rd_c); 
		  Router.ports[i].weight=(int)weight;
    		mainserver=rd.simulatedIPAddress;
    		
    	      break;
    	  }
    	  else
    		  System.out.println("Port "+ i +" Not available");
      }
      totalclient++;
	  

  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() 
  {
	  
	  ServerThread.addClient(Router.ports);
     
   
  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to identify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link1
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,String simulatedIP, short weight) 
  {

	  
	  for(int i=0; i<4; i++)
		{
		
		    if( Router.ports[i]!=null && Router.ports[i].router2.simulatedIPAddress.equals(simulatedIP))
		    	ff=true;
		}
	  for(int i=0; i <4; i++)
      {
    	  /*if(ports[i].router1.simulatedIPAddress.equals(simulatedIP) && ports[i]!=null)
    	  {
    		  System.out.println("Invalid Client IP Address. IP Address cannot be same as that of server/host");
    		  break;
    	  }*/
      if(ff) 
    	  {
    		  System.out.println("Connection already exists");
    		  break;
    	  }
    	  
      else if(Router.ports[i]==null && processPort > 1023 && processPort<=65535)
    	  {
    	  
    	  rd_c = new RouterDescription();
		  rd_c.simulatedIPAddress= simulatedIP;
		  rd_c.processIPAddress=processIP;
		  rd_c.processPortNumber=processPort;
		  rd_c.status=RouterStatus.NO;
		  Router.ports[i] =new Link(rd,rd_c); 
		  Router.ports[i].weight=(int)weight;
    		mainserver=rd.simulatedIPAddress;
    	
    	      break;
    	  }
    	  else
    		  System.out.println("Port "+ i +" Not available");
      }
	  
		ServerThread.addClient(Router.ports);
    //  totalclient++;
      
  }

  /**
   * output the neighbors of the routers (ONLY 2WAY Connection)
   */
  private void processNeighbors() {
	  
	  for(int i=0; i<Router.ports.length; i++)
	  {
		      if(Router.ports[i]!=null ){
			  System.out.println("IP Address of the neighbour"+(i+1)+ " "+Router.ports[i].router2.simulatedIPAddress);
		      }
		      
		 
	  }

  }

  /**
   * disconnect with all neighbors and quit the program
   */  
  private void processQuit() 
  {
	
	  timer.cancel();
	  timer.purge();
	  System.exit(0);

  }

  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
    	 if (command.startsWith("detect ")) {
    	  String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.startsWith("connect ")) {
            String[] cmdLine = command.split(" ");
            processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                    cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
