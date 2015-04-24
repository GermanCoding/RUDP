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
 *  SYN Segment
 *
 *   0             7 8             15
 *  +-+-+-+-+-+-+-+-+---------------+
 *  | |A| | | | | | |               |
 *  |1|C|0|0|0|0|0|0|       22      |
 *  | |K| | | | | | |               |
 *  +-+-+-+-+-+-+-+-+---------------+
 *  +  Sequence #   +   Ack Number  |
 *  +---------------+---------------+
 *  | Vers  | Spare | Max # of Out  |
 *  |       |       | standing Segs |
 *  +---------------+---------------+
 *  | Option Flags  |     Spare     |
 *  +---------------+---------------+
 *  |     Maximum Segment Size      |
 *  +---------------+---------------+
 *  | Retransmission Timeout Value  |
 *  +---------------+---------------+
 *  | Cumulative Ack Timeout Value  |
 *  +---------------+---------------+
 *  |   Null Segment Timeout Value  |
 *  +---------------+---------------+
 *  |  Max Retrans  | Max Cum Ack   |
 *  +---------------+---------------+
 *  | Max Out of Seq| Max Auto Reset|
 *  +---------------+---------------+
 *  |           Checksum            |
 *  +---------------+---------------+
 *
 */
public class SYNSegment extends Segment
{
    protected SYNSegment()
    {
    }

    public SYNSegment(int seqn, int maxseg, int maxsegsize, int rettoval,
            int cumacktoval, int niltoval, int maxret,
            int maxcumack, int maxoutseq, int maxautorst)
    {
        init(SYN_FLAG, seqn, SYN_HEADER_LEN);

        _version = RUDP_VERSION;
        _maxseg = maxseg;
        _optflags = 0x01; /* no options */
        _maxsegsize = maxsegsize;
        _rettoval = rettoval;
        _cumacktoval = cumacktoval;
        _niltoval = niltoval;
        _maxret = maxret;
        _maxcumack = maxcumack;
        _maxoutseq = maxoutseq;
        _maxautorst = maxautorst;
    }

    public String type()
    {
        return "SYN";
    }

    public int getVersion()
    {
        return _version;
    }

    public int getMaxOutstandingSegments()
    {
        return _maxseg;
    }

    public int getOptionFlags()
    {
        return _optflags;
    }

    public int getMaxSegmentSize()
    {
        return _maxsegsize;
    }

    public int getRetransmissionTimeout()
    {
        return _rettoval;
    }

    public int getCummulativeAckTimeout()
    {
        return _cumacktoval;
    }

    public int getNulSegmentTimeout()
    {
        return _niltoval;
    }

    public int getMaxRetransmissions()
    {
        return _maxret;
    }

    public int getMaxCumulativeAcks()
    {
        return _maxcumack;
    }

    public int getMaxOutOfSequence()
    {
        return _maxoutseq;
    }

    public int getMaxAutoReset()
    {
        return _maxautorst;
    }

    public byte[] getBytes()
    {
        byte[] buffer = super.getBytes();
        buffer[4] = (byte) ((_version << 4) & 0xFF);
        buffer[5] = (byte) (_maxseg & 0xFF);
        buffer[6] = (byte) (_optflags & 0xFF);
        buffer[7] = 0; /* spare */
        buffer[8] = (byte) ((_maxsegsize >>> 8) & 0xFF);
        buffer[9] = (byte) ((_maxsegsize >>> 0) & 0xFF);
        buffer[10] = (byte) ((_rettoval >>> 8) & 0xFF);
        buffer[11] = (byte) ((_rettoval >>> 0) & 0xFF);
        buffer[12] = (byte) ((_cumacktoval >>> 8) & 0xFF);
        buffer[13] = (byte) ((_cumacktoval >>> 0) & 0xFF);
        buffer[14] = (byte) ((_niltoval >>> 8) & 0xFF);
        buffer[15] = (byte) ((_niltoval >>> 0) & 0xFF);
        buffer[16] = (byte) (_maxret & 0xFF);
        buffer[17] = (byte) (_maxcumack & 0xFF);
        buffer[18] = (byte) (_maxoutseq & 0xFF);
        buffer[19] = (byte) (_maxautorst & 0xFF);

        return buffer;
    }

    protected void parseBytes(byte[] buffer, int off, int len)
    {
        super.parseBytes(buffer, off, len);

        if (len < (SYN_HEADER_LEN)) {
            throw new IllegalArgumentException("Invalid SYN segment");
        }

        _version = ((buffer[off+4] & 0xFF) >>> 4);
        if (_version != RUDP_VERSION) {
            throw new IllegalArgumentException("Invalid RUDP version");
        }

        _maxseg      =  (buffer[off+ 5] & 0xFF);
        _optflags    =  (buffer[off+ 6] & 0xFF);
        // spare     =  (buffer[off+ 7] & 0xFF);
        _maxsegsize  = ((buffer[off+ 8] & 0xFF) << 8) | ((buffer[off+ 9] & 0xFF) << 0);
        _rettoval    = ((buffer[off+10] & 0xFF) << 8) | ((buffer[off+11] & 0xFF) << 0);
        _cumacktoval = ((buffer[off+12] & 0xFF) << 8) | ((buffer[off+13] & 0xFF) << 0);
        _niltoval    = ((buffer[off+14] & 0xFF) << 8) | ((buffer[off+15] & 0xFF) << 0);
        _maxret      =  (buffer[off+16] & 0xFF);
        _maxcumack   =  (buffer[off+17] & 0xFF);
        _maxoutseq   =  (buffer[off+18] & 0xFF);
        _maxautorst  =  (buffer[off+19] & 0xFF);
    }

    private int  _version;
    private int  _maxseg;
    private int  _optflags;
    private int  _maxsegsize;
    private int  _rettoval;
    private int  _cumacktoval;
    private int  _niltoval;
    private int  _maxret;
    private int  _maxcumack;
    private int  _maxoutseq;
    private int  _maxautorst;

    private static final int SYN_HEADER_LEN = RUDP_HEADER_LEN + 16;
}

