1.
In order to connect to a java app through VisualVM, you need to run jstatd process on a remote server.
This can be done by executing the following command:
./jstatd -J-Djava.security.policy=jstatd.all.policy -J-Xms2g -J-Xmx2g


2.
How to enable JMX on Tomcat running on EC2 (behind a firewall)

Step 1: Copy catalina-jmx-remote.jar into Tomcat’s lib folder. This jar file is available in the extras folder of Tomcat’s binary distribution.

Step 2: Modify Tomcat’s server.xml by adding a new Listener entry.

<Listener className="org.apache.catalina.mbeans.JmxRemoteLifecycleListener" 
rmiRegistryPortPlatform="9090" rmiServerPortPlatform="9091"/>

Step 3: Open both of these ports in your firewall. Ports 9090 and 9091 and just examples, you can choose to use any port numbers you like.

Step 4: Add the following options to Tomcat startup script.

-Dcom.sun.management.jmxremote 
-Dcom.sun.management.jmxremote.authenticate=false 
-Dcom.sun.management.jmxremote.ssl=false 
-Djava.rmi.server.hostname=public-ip (or public dns)


3. 
Reserved ports for JMX
   Test : 9000 9001
   Main : 9010 9011