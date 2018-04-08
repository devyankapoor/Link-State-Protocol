package socs.network.message;

import java.io.Serializable;

public class AlivePacket implements Serializable {

  public short srcProcessPort;
  public short dstProcessPort;
  public String srcIP;
  public String dstIP;

 

}
