package entralinked.utility;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkUtility {
    
    private static final Logger logger = LogManager.getLogger();
    
    public static InetAddress getLocalHost() {
        try(Socket socket = new Socket()){
            socket.connect(new InetSocketAddress("github.com", 80));
            return socket.getLocalAddress();
        } catch(IOException e) {
            logger.error("Couldn't get local host using socket - falling back to the network interface method", e);
            return getLocalHostFromNetworkInterfaces();
        }
    }
    
    private static InetAddress getLocalHostFromNetworkInterfaces() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            
            while(networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                
                // Skip if loopback interface
                if(networkInterface.isLoopback()) {
                    continue;
                }
                
                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                
                while(addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    
                    // Return if IPv4
                    if(address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
        } catch(IOException e) {
            logger.error("Could not determine local host - falling back to loopback address", e);
        }
        
        return InetAddress.getLoopbackAddress();
    }
}
