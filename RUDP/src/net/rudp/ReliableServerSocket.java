/*
 * Simple Reliable UDP (rudp)
 * Copyright (c) 2009, Adrian Granados (agranados@ihmc.us)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the copyright holder nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package net.rudp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

import net.rudp.impl.SYNSegment;
import net.rudp.impl.Segment;

/**
 * This class implements server sockets that use
 * the Simple Reliable UDP (RUDP) protocol.
 *
 * @author Adrian Granados
 * @see java.net.ServerSocket
 */
public class ReliableServerSocket extends ServerSocket
{
    /**
     * Creates an unbound RUDP server socket.
     *
     * @throws IOException if an I/O error occurs when opening
     *         the underlying UDP socket.
     * @see java.net.ServerSocket#ServerSocket()
     */
    public ReliableServerSocket()
        throws IOException
    {
        this(0, 0, null);
    }

    /**
     * Creates a RUDP server socket, bound to the specified port. A port
     * of <code>0</code> creates a socket on any free port.
     * </p>
     * The maximum queue length for incoming connection indications (a
     * request to connect) is set to <code>50</code>. If a connection
     * indication arrives when the queue is full, the connection is refused.
     *
     * @param  port    the port number, or <code>0</code> to use any free port.
     * @throws IOException if an I/O error occurs when opening
     *         the underlying UDP socket.
     * @see java.net.ServerSocket#ServerSocket(int)
     */
    public ReliableServerSocket(int port)
        throws IOException
    {
        this(port, 0, null);
    }

    /**
     * Creates a RUDP server socket and binds it to the specified local port, with
     * the specified backlog. A port of <code>0</code> creates a socket on any
     * free port.
     *
     * @param port      the port number, or <code>0</code> to use any free port.
     * @param backlog   the listen backlog.
     * @throws IOException if an I/O error occurs when opening
     *         the underlying UDP socket.
     * @see java.net.ServerSocket#ServerSocket(int, int)
     */
    public ReliableServerSocket(int port, int backlog)
        throws IOException
    {
        this(port, backlog, null);
    }

    /**
     * Creates a RUDP server socket and binds it to the specified local port and
     * IP address, with the specified backlog. The <i>bindAddr</i> argument
     * can be used on a multi-homed host for a ReliableServerSocket that
     * will only accept connect requests to one of its addresses.
     * If <i>bindAddr</i> is null, it will default accepting
     * connections on any/all local addresses.
     * A port of <code>0</code> creates a socket on any free port.
     *
     * @param port      the port number, or <code>0</code> to use any free port.
     * @param backlog   the listen backlog.
     * @param bindAddr  the local InetAddress the server will bind to.
     * @throws IOException if an I/O error occurs when opening
     *         the underlying UDP socket.
     * @see java.net.ServerSocket#ServerSocket(int, int, InetAddress)
     */
    public ReliableServerSocket(int port, int backlog, InetAddress bindAddr)
        throws IOException
    {
        this(new DatagramSocket(new InetSocketAddress(bindAddr, port)), backlog);
    }

    /**
     * Creates a RUDP server socket attached to the specified UDP socket, with
     * the specified backlog.
     *
     * @param sock    the underlying UDP socket.
     * @param backlog the listen backlog.
     * @throws IOException if an I/O error occurs.
     */
    public ReliableServerSocket(DatagramSocket sock, int backlog)
        throws IOException
    {
        if (sock == null) {
            throw new NullPointerException("sock");
        }

        _serverSock = sock;
        _backlogSize = (backlog <= 0) ? DEFAULT_BACKLOG_SIZE : backlog;
        _backlog = new ArrayList<ReliableSocket>(_backlogSize);
        _clientSockTable = new HashMap<SocketAddress, ReliableClientSocket>();
        _stateListener = new StateListener();
        _timeout = 0;
        _closed = false;

        new ReceiverThread().start();
    }

    public Socket accept()
        throws IOException
    {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        synchronized (_backlog) {
            while (_backlog.isEmpty()) {
                try {
                    if (_timeout == 0) {
                        _backlog.wait();
                    }
                    else {
                        long startTime = System.currentTimeMillis();
                        _backlog.wait(_timeout);
                        if (System.currentTimeMillis() - startTime >= _timeout) {
                            throw new SocketTimeoutException();
                        }
                    }

                }
                catch (InterruptedException xcp) {
                    xcp.printStackTrace();
                }

                if (isClosed()) {
                    throw new IOException();
                }
            }

            return (Socket) _backlog.remove(0);
        }
    }

    public synchronized void bind(SocketAddress endpoint)
        throws IOException
    {
        bind(endpoint, 0);
    }

    public synchronized void bind(SocketAddress endpoint, int backlog)
        throws IOException
    {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        _serverSock.bind(endpoint);
    }

    public synchronized void close()
    {
        if (isClosed()) {
            return;
        }

        _closed = true;
        synchronized (_backlog) {
            _backlog.clear();
            _backlog.notify();
        }

        if (_clientSockTable.isEmpty()) {
            _serverSock.close();
        }
    }

    public InetAddress getInetAddress()
    {
        return _serverSock.getInetAddress();
    }

    public int getLocalPort()
    {
        return _serverSock.getLocalPort();
    }

    public SocketAddress getLocalSocketAddress()
    {
        return _serverSock.getLocalSocketAddress();
    }

    public boolean isBound()
    {
        return _serverSock.isBound();
    }

    public boolean isClosed()
    {
        return _closed;
    }

    public void setSoTimeout(int timeout)
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeout < 0");
        }

        _timeout = timeout;
    }

    public int getSoTimeout()
    {
        return _timeout;
    }

    /**
     * Registers a new client socket with the specified endpoint address.
     *
     * @param endpoint    the new socket.
     * @return the registered socket.
     */
    private ReliableClientSocket addClientSocket(SocketAddress endpoint)
    {
        synchronized (_clientSockTable) {
            ReliableClientSocket sock = (ReliableClientSocket) _clientSockTable.get(endpoint);

            if (sock == null) {
                try {
                    sock = new ReliableClientSocket(_serverSock, endpoint);
                    sock.addStateListener(_stateListener);
                    _clientSockTable.put(endpoint, sock);
                }
                catch (IOException xcp) {
                    xcp.printStackTrace();
                }
            }

            return sock;
        }
    }

    /**
     * Deregisters a client socket with the specified endpoint address.
     *
     * @param endpoint     the socket.
     * @return the deregistered socket.
     */
    private ReliableClientSocket removeClientSocket(SocketAddress endpoint)
    {
        synchronized (_clientSockTable) {
            ReliableClientSocket sock = (ReliableClientSocket) _clientSockTable.remove(endpoint);

            if (_clientSockTable.isEmpty()) {
                if (isClosed()) {
                    _serverSock.close();
                }
            }

            return sock;
        }
    }

    private DatagramSocket _serverSock;
    private int            _timeout;
    private int            _backlogSize;
    private boolean        _closed;

    /*
     * The listen backlog queue.
     */
    private ArrayList<ReliableSocket>      _backlog;

    /*
     * A table of active opened client sockets.
     */
    private HashMap<SocketAddress, ReliableClientSocket>   _clientSockTable;

    private ReliableSocketStateListener _stateListener;

    private static final int DEFAULT_BACKLOG_SIZE = 50;

    private class ReceiverThread extends Thread
    {
        public ReceiverThread()
        {
            super("ReliableServerSocket");
            setDaemon(true);
        }

        public void run()
        {
            byte[] buffer = new byte[65535];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                ReliableClientSocket sock = null;

                try {
                    _serverSock.receive(packet);
                    SocketAddress endpoint = packet.getSocketAddress();
                    Segment s = Segment.parse(packet.getData(), 0, packet.getLength());

                    synchronized (_clientSockTable) {

                        if (!isClosed()) {
                            if (s instanceof SYNSegment) {
                                if (!_clientSockTable.containsKey(endpoint)) {
                                    sock = addClientSocket(endpoint);
                                }
                            }
                        }

                        sock = (ReliableClientSocket) _clientSockTable.get(endpoint);
                    }

                    if (sock != null) {
                        sock.segmentReceived(s);
                    }
                }
                catch (IOException xcp) {
                    if (isClosed()) {
                        break;
                    }
                    xcp.printStackTrace();
                }
            }
        }
    }

    public class ReliableClientSocket extends ReliableSocket
    {
        public ReliableClientSocket(DatagramSocket sock,
                                    SocketAddress endpoint)
            throws IOException
        {
            super(sock);
            _endpoint = endpoint;
        }

        protected void init(DatagramSocket sock, ReliableSocketProfile profile)
        {
            _queue = new ArrayList<Segment>();
            super.init(sock, profile);
        }

        protected Segment receiveSegmentImpl()
        {
            synchronized (_queue) {
                while (_queue.isEmpty()) {
                    try {
                        _queue.wait();
                    }
                    catch (InterruptedException xcp) {
                        xcp.printStackTrace();
                    }
                }

                return (Segment) _queue.remove(0);
            }
        }

        protected void segmentReceived(Segment s)
        {
            synchronized (_queue) {
                _queue.add(s);
                _queue.notify();
            }
        }

        protected void closeSocket()
        {
            synchronized (_queue) {
                _queue.clear();
                _queue.add(null);
                _queue.notify();
            }
        }

        protected void log(String msg)
        {
            System.out.println(getPort() + ": " + msg);
        }

        private ArrayList<Segment> _queue;
    }

    private class StateListener implements ReliableSocketStateListener
    {
        public void connectionOpened(ReliableSocket sock)
        {
            if (sock instanceof ReliableClientSocket) {
                synchronized (_backlog) {
                    while (_backlog.size() > DEFAULT_BACKLOG_SIZE) {
                        try {
                            _backlog.wait();
                        }
                        catch (InterruptedException xcp) {
                            xcp.printStackTrace();
                        }
                    }

                    _backlog.add(sock);
                    _backlog.notify();
                }
            }
        }

        public void connectionRefused(ReliableSocket sock)
        {
            // do nothing.
        }

        public void connectionClosed(ReliableSocket sock)
        {
            // Remove client socket from the table of active connections.
            if (sock instanceof ReliableClientSocket) {
                removeClientSocket(sock.getRemoteSocketAddress());
            }
        }

        public void connectionFailure(ReliableSocket sock)
        {
            // Remove client socket from the table of active connections.
            if (sock instanceof ReliableClientSocket) {
                removeClientSocket(sock.getRemoteSocketAddress());
            }
        }

        public void connectionReset(ReliableSocket sock)
        {
            // do nothing.
        }
    }
}
