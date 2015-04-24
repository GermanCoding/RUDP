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
import java.io.InputStream;

/**
 * This class extends InputStream to implement a ReliableSocketInputStream.
 * Note that this class should <b>NOT</b> be public.
 *
 * @author Adrian Granados
 *
 */
class ReliableSocketInputStream extends InputStream {
	/**
	 * Creates a new ReliableSocketInputStream.
	 * This method can only be called by a ReliableSocket.
	 *
	 * @param sock
	 *            the actual RUDP socket to read bytes on.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public ReliableSocketInputStream(ReliableSocket sock) throws IOException {
		if (sock == null) {
			throw new NullPointerException("sock");
		}

		_sock = sock;
		_buf = new byte[_sock.getReceiveBufferSize()];
		_pos = _count = 0;
	}

	public synchronized int read() throws IOException {
		if (readImpl() < 0) {
			return -1;
		}

		return (_buf[_pos++] & 0xFF);
	}

	public synchronized int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public synchronized int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		}

		if (off < 0 || len < 0 || (off + len) > b.length) {
			throw new IndexOutOfBoundsException();
		}

		if (readImpl() < 0) {
			return -1;
		}

		int readBytes = Math.min(available(), len);
		System.arraycopy(_buf, _pos, b, off, readBytes);
		_pos += readBytes;

		return readBytes;
	}

	public synchronized int available() {
		return (_count - _pos);
	}

	public boolean markSupported() {
		return false;
	}

	public void close() throws IOException {
		_sock.shutdownInput();
	}

	private int readImpl() throws IOException {
		if (available() == 0) {
			_count = _sock.read(_buf, 0, _buf.length);
			_pos = 0;
		}

		return _count;
	}

	protected ReliableSocket _sock;
	protected byte[] _buf;
	protected int _pos;
	protected int _count;
}
