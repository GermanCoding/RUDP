# RUDP
A (slightly) modified version of the RUDP libary written by Adrian Granados (not me, copyright for this goes to him. See RUDP/license.txt for details).

R-UDP means "Reliable UDP". A description of the protocol can be found here http://www.ietf.org/proceedings/44/I-D/draft-ietf-sigtran-reliable-udp-00.txt or https://en.wikipedia.org/wiki/Reliable_User_Datagram_Protocol

Download
--
You can download a up to date pre-compiled version of this API here (Compiled with Java 8):
https://build.germancoding.com/job/RUDP/lastSuccessfulBuild/artifact/RUDP/rudp-SNAPSHOT.jar

Of course you can also download/clone the source and compile it for yourself. There are no dependencies, the project was tested working on Java 6/7/8.

Usage
--
The design of the library is very similar (if not equal) to the Java socket API. To create a new connection with RUDP, do the same thing you would do with Java TCP/IO. The library will handle most stuff in the background, just like TCP.

```
// For the server
ReliableServerSocket serverSocket = new ReliableServerSocket(0);
Socket someRUDPClient = serverSocket.accept(); 

// For the client
ReliableSocket client = new ReliableSocket();
client.connect(serverSocket.getLocalSocketAddress()); 
/* This is just an example, for a real tutorial look up Java sockets */
```
I also added an option to use pre-existing (and already bound) UDP sockets for the RUDP connection, just pass them as parameter to the constructor.

Javadoc
--
The javadoc of this project is available here:: https://build.germancoding.com/job/RUDP/javadoc/
