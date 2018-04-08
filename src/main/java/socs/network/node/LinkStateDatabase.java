package socs.network.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;

public class LinkStateDatabase {

	//linkID => LSAInstance
	HashMap<String, LSA> _store = new HashMap<String, LSA>();

	private RouterDescription rd = null;

	public LinkStateDatabase(RouterDescription routerDescription) {
		rd = routerDescription;
		LSA l = initLinkStateDatabase();
		_store.put(l.linkStateID, l);
	}

	/**
	 * output the shortest path from this router to the destination with the given IP address
	 */
	void getShortestPath(String destinationIP)
	{
		Boolean rr=false;
		
			
			if(Router.lsd._store.containsKey(destinationIP) )
			{
				rr=true;
			}
		
		
		if(rr)
		{
		int previous_value_at_node=0; // initialize prev value at node
		int min_weight=1000; //maximum weight
		ArrayList<String> visited_nodes= new ArrayList<String>();
		ArrayList<String> temp_nodes;
		HashMap<String,Integer> unvisited_nodes= new  HashMap<String,Integer>();
		HashMap<String,HashMap> node_neighbor_weight= new HashMap<String,HashMap>();	//source with neighbor/weight
		HashMap<String,Integer> neighbor_w;  		//neighbor---weight
		String source= rd.simulatedIPAddress;   // simulated IP address of source or router on which we find shortest distance
		HashMap<String,String> node_prnode= new HashMap<String,String>();   //previous node--node
		String s_to_visit=source;
		node_prnode.put(rd.simulatedIPAddress,"XX");
		for(Map.Entry<String,LSA> entry: _store.entrySet())
		{

			if(entry.getKey().equals(source))
			{
				unvisited_nodes.put(entry.getKey(),0);
			}
			else
				unvisited_nodes.put(entry.getKey(),85242263);

			neighbor_w= new HashMap<String,Integer>();
			for(LinkDescription l: entry.getValue().links)
			{
				neighbor_w.put(l.linkID,l.tosMetrics);
			}

			node_neighbor_weight.put(entry.getKey(),neighbor_w);
		}
		// Stop using _store
		// Starting Process with source (i.e. A)


		/**********************************************************************************************************************************/
		// Printing the data taken from the database		
		/*System.out.println("Printing Univisited Nodes");
		for(Map.Entry<String,Integer> entry: unvisited_nodes.entrySet())
		{
			System.out.println(entry.getKey()+ "-->"+ entry.getValue());
		}
		System.out.println("Printing Links");
		for(Map.Entry<String,HashMap> ent1: node_neighbor_weight.entrySet())
		{
			System.out.println(ent1.getKey()+ "-->"+ ent1.getValue());
			System.out.println("Printing for this linkid");
			HashMap<String,Integer> temp=ent1.getValue();
			for(Map.Entry<String,Integer> ent2: temp.entrySet())
			{
				System.out.println(ent2.getKey()+ "-->"+ ent2.getValue());
			}
		}*/


		/**********************************************************************************************************************************/

/*** hashcode problem check  **/
outer:		while(s_to_visit!=null)
		{
			for(Map.Entry<String,HashMap> entry: node_neighbor_weight.entrySet())   // String, Hashmap
			{
				   temp_nodes=	new ArrayList<String>();
				if(unvisited_nodes.isEmpty() || unvisited_nodes.size()==1)  //breaking the loop (to avoid repetition)
				{
					break outer;    

				}
				
				//System.out.println(s_to_visit + " " + s_to_visit.hashCode() + " "+ entry.getKey()+ " "+ entry.getKey().hashCode());
				if(entry.getKey().equals(s_to_visit))	// Source condition to set values for neighbors of source
				{
					String previous_node=null;
					HashMap<String,Integer> temp=entry.getValue(); //according to s_visit

					/*if(temp.size()==1) // Breaking the loop  (if node is last one and no one is there ahead and prev node is done)
					{
						
						break outer;

					}*/
/*********************************************************/
					for(Map.Entry<String,Integer> entemp: unvisited_nodes.entrySet()) // to check previous value at node being visited at present
					{
						if(entemp.getKey().equals(s_to_visit))
						{
							previous_value_at_node=entemp.getValue() ;// to be added while comparing (2 functions)
							break;
						}


					}
/*********************************************************/
					for(Map.Entry<String,String> entemp: node_prnode.entrySet())   // to check previous node  for node being visited
					{
						if(entemp.getKey().equals(s_to_visit))
						{
							previous_node=entemp.getValue();
							break;
						}
	
					}


/************************************************************Main loop starts (links) to assign value and decide s_to_visit***********************/
					for(Map.Entry<String,Integer> ent1: temp.entrySet())  
					{
				
						if(!visited_nodes.contains(ent1.getKey())) // if link is not visited
						{
							String s= ent1.getKey();		// link id 
							int t= ent1.getValue();			// weight
								temp_nodes.add(s);					
							/*if(t < min_weight)
							{
								if(!visited_nodes.contains(s))
								{
									s_to_visit =s;
									min_weight=t;
								}
							}*/

							/*******************************Replacing logic ***********************************/	
							for(Map.Entry<String,Integer> ent2: unvisited_nodes.entrySet())
							{
								if(ent2.getKey().equals(s))
								{
									if(t + previous_value_at_node < ent2.getValue())  // Set value if less than t + previous
									{
										ent2.setValue(t + previous_value_at_node);		//neither change value neither change previous node
										node_prnode.remove(s);
										node_prnode.put(s,entry.getKey());
										break;
									}
								}
							}
							
							//node_prnode.put(s,entry.getKey());  
						
						} /**skip iteration if node is already visited and check other nodes **/
						
				
					}
/************************************************************Main loop end (links)***********************/
					for(Map.Entry<String,Integer> ent2: unvisited_nodes.entrySet())
					{
						if(temp_nodes.contains(ent2.getKey()) && !visited_nodes.contains(ent2.getKey()))   // present in unvisited but not in visited
						{
							if(min_weight > ent2.getValue())
							{
							min_weight=ent2.getValue();
							s_to_visit=ent2.getKey();		//replacing
							}
						}
						
					}
					
					visited_nodes.add(entry.getKey());  //node visited--> entry made because s_tovisit is changed
					unvisited_nodes.remove(entry.getKey()); //node visited--> entry removed because s_tovisit is changed
				} // if end for s_to_visit
  
				min_weight=10000;
				temp_nodes=null;
				//break;

		} // Big loop end
		} // while loop end


		String t_visit= destinationIP;
		ArrayList<String> sb_print =  new ArrayList<String>();
		ArrayList<Integer>we_print= new ArrayList<Integer>();
		while(t_visit!="XX"){
		for(Map.Entry<String,String> print: node_prnode.entrySet())
		{
		     HashMap<String,Integer> hm=null;
			if(print.getKey().equals(t_visit) )
			{
				for(Map.Entry<String,HashMap> print1: node_neighbor_weight.entrySet())
				{
					if(print1.getKey().equals(t_visit)){
				    hm= print1.getValue();
					break;
					}
				}
				if(hm!=null && !print.getValue().equals("XX")){
				for(Map.Entry<String,Integer> print2: hm.entrySet())
				{
					if(print2.getKey().equals(print.getValue()))
					{
						we_print.add(print2.getValue());
						break;
					}
					
				}
				}
			    sb_print.add(print.getKey()); 
				t_visit=print.getValue();
				break;
			}

		}
		
		

		}
		
		 for(int i=sb_print.size()-1;i>=0;i--)
		    {
		    
		    	if(i>0)
		    	{
		    		
		    	System.out.print(sb_print.get(i)+" ("+we_print.get(i-1)+") --> ");
		    	}
		    	else
		    		System.out.print(sb_print.get(i));
		    }

    
   /* for(int i=sb_print.size()-1;i>=0;i--)
    {
    
    	if(i>=1)
    	{
    		int c= i+1;
    	System.out.print(sb_print.get(i)+" ("+c+") --> ");
    	}
    	else
    		System.out.print(sb_print.get(i));
    }*/

	}
		else
			System.out.println(destinationIP+ " :Not found");
	}		

	//initialize the linkstate database by adding an entry about the router itself
	private LSA initLinkStateDatabase() {
		LSA lsa = new LSA();
		lsa.linkStateID = rd.simulatedIPAddress;
		lsa.lsaSeqNumber = Router.current_seq_nbr;
		LinkDescription ld = new LinkDescription();
		ld.linkID = rd.simulatedIPAddress;
		ld.portNum = rd.processPortNumber;
		ld.tosMetrics = 0;
		lsa.links.add(ld);
		return lsa;
	}


	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (LSA lsa: _store.values()) {
			sb.append(lsa.linkStateID).append("(" + lsa.lsaSeqNumber + ")").append(":\t");
			for (LinkDescription ld : lsa.links) {
				sb.append(ld.linkID).append(",").append(ld.portNum).append(",").
				append(ld.tosMetrics).append("\t");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
