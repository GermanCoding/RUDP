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
import java.io.OutputStream;

/**
 * This class extends OutputStream to implement a ReliableSocketOutputStream.
 * Note that this class should <b>NOT</b> be public.
 *
 * @author Adrian Granados
 *
 */
class ReliableSocketOutputStream extends OutputStream
{
    /**
     * Creates a new ReliableSocketOutputStream.
     * This method can only be called by a ReliableSocket.
     *
     * @param sock    the actual RUDP socket to writes bytes on.
     * @throws IOException if an I/O error occurs.
     */
    public ReliableSocketOutputStream(ReliableSocket sock)
        throws IOException
    {
        if (sock == null) {
            throw new NullPointerException("sock");
        }

        _sock = sock;
        _buf = new byte[_sock.getSendBufferSize()];
        _count = 0;
    }

    public synchronized void write(int b)
        throws IOException
    {
        if (_count >= _buf.length) {
            flush();
        }

        _buf[_count++] = (byte) (b & 0xFF);
    }

    public synchronized void write(byte[] b)
        throws IOException
    {
        write(b, 0, b.length);
    }

    public synchronized void write(byte[] b, int off, int len)
        throws IOException
    {
        if (b == null) {
            throw new NullPointerException();
        }

        if (off < 0 || len < 0 || (off+len) > b.length) {
            throw new IndexOutOfBoundsException();
        }

        int buflen;
        int writtenBytes = 0;

        while (writtenBytes < len) {
            buflen = Math.min(_buf.length, len-writtenBytes);
            if (buflen > (_buf.length - _count)) {
                flush();
            }
            System.arraycopy(b, off+writtenBytes, _buf, _count, buflen);
            _count += buflen;
            writtenBytes += buflen;
        }
    }

    public synchronized void flush()
        throws IOException
    {
        if (_count > 0) {
            _sock.write(_buf, 0, _count);
            _count = 0;
        }
    }

    public synchronized void close()
        throws IOException
    {
        flush();
        _sock.shutdownOutput();
    }

    protected ReliableSocket _sock;
    protected byte[]         _buf;
    protected int            _count;
}
