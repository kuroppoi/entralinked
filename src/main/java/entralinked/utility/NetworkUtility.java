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
            InetAddress address = socket.getLocalAddress();
            
            // Fall back to the network interface method if this is not a private IP address
            if(!address.isSiteLocalAddress()) {
                return getLocalHostFromNetworkInterfaces();
            }
            
            return address;
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
                    
                    // Return this address if it is a local IPv4 address
                    if(address.isSiteLocalAddress() && address instanceof Inet4Address) {
                        return address;
                    }
                }
            }
        } catch(IOException e) {
            logger.error("Could not determine local host - falling back to loopback address", e);
        }
        
        logger.warn("No local host candidate could be found - loopback address will be used");
        return InetAddress.getLoopbackAddress();
    }
}
