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

package net.rudp.impl;


/*
 *  Data Segment
 *
 *   0 1 2 3 4 5 6 7 8            15
 *  +-+-+-+-+-+-+-+-+---------------+
 *  |0|1|0|0|0|0|0|0|       6       |
 *  +-+-+-+-+-+-+-+-+---------------+
 *  | Sequence #    |   Ack Number  |
 *  +---------------+---------------+
 *  |           Checksum            |
 *  +---------------+---------------+
 *  | ...                           |
 *  +-------------------------------+
 *
 */
public class DATSegment extends Segment
{
    protected DATSegment()
    {
    }

    public DATSegment(int seqn, int ackn, byte[] b, int off, int len)
    {
        init(ACK_FLAG, seqn, RUDP_HEADER_LEN);
        setAck(ackn);
        _data = new byte[len];
        System.arraycopy(b, off, _data, 0, len);
    }

    public int length()
    {
        return _data.length + super.length();
    }

    public String type()
    {
        return "DAT";
    }

    public byte[] getData()
    {
        return _data;
    }

    public byte[] getBytes()
    {
        byte[] buffer = super.getBytes();
        System.arraycopy(_data, 0, buffer, RUDP_HEADER_LEN, _data.length);
        return buffer;
    }

    public void parseBytes(byte[] buffer, int off, int len)
    {
        super.parseBytes(buffer, off, len);
        _data = new byte[len - RUDP_HEADER_LEN];
        System.arraycopy(buffer, off+RUDP_HEADER_LEN, _data, 0, _data.length);
    }

    private byte[] _data;
}
