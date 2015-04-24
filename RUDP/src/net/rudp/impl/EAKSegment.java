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
 *  EACK Segment
 *
 *   0 1 2 3 4 5 6 7 8            15
 *  +-+-+-+-+-+-+-+-+---------------+
 *  |0|1|1|0|0|0|0|0|     N + 6     |
 *  +-+-+-+-+-+-+-+-+---------------+
 *  | Sequence #    |   Ack Number  |
 *  +---------------+---------------+
 *  |1st out of seq |2nd out of seq |
 *  |  ack number   |   ack number  |
 *  +---------------+---------------+
 *  |  . . .        |Nth out of seq |
 *  |               |   ack number  |
 *  +---------------+---------------+
 *  |            Checksum           |
 *  +---------------+---------------+
 *
 */
public class EAKSegment extends ACKSegment
{
    protected EAKSegment()
    {
    }

    public EAKSegment(int seqn, int ackn,  int[] acks)
    {
        init(EAK_FLAG, seqn, RUDP_HEADER_LEN + acks.length);
        setAck(ackn);
        _acks = acks;
    }

    public String type()
    {
        return "EAK";
    }

    public int[] getACKs()
    {
        return _acks;
    }

    public byte[] getBytes()
    {
        byte[] buffer = super.getBytes();

        for (int i = 0; i < _acks.length; i++) {
            buffer[4+i] = (byte) (_acks[i] & 0xFF);
        }

        return buffer;
    }

    protected void parseBytes(byte[] buffer, int off, int len)
    {
        super.parseBytes(buffer, off, len);
        _acks = new int[len - RUDP_HEADER_LEN];
        for (int i = 0; i < _acks.length; i++) {
            _acks[i] = (buffer[off + 4 + i] & 0xFF);
        }
    }

    private int[] _acks;
}
