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


/**
 * This class specifies the RUDP parameters of a socket.
 *
 * @author Adrian Granados
 * @see    net.rudp.ReliableSocket
 */
public class ReliableSocketProfile
{
    public final static int MAX_SEND_QUEUE_SIZE    = 32;
    public final static int MAX_RECV_QUEUE_SIZE    = 32;

    public final static int MAX_SEGMENT_SIZE       = 128;
    public final static int MAX_OUTSTANDING_SEGS   = 3;
    public final static int MAX_RETRANS            = 3;
    public final static int MAX_CUMULATIVE_ACKS    = 3;
    public final static int MAX_OUT_OF_SEQUENCE    = 3;
    public final static int MAX_AUTO_RESET         = 3;
    public final static int NULL_SEGMENT_TIMEOUT   = 2000;
    public final static int RETRANSMISSION_TIMEOUT = 600;
    public final static int CUMULATIVE_ACK_TIMEOUT = 300;

    /**
     * Creates a profile with the default RUDP parameter values.
     *
     * Note: According to the RUDP protocol's draft, the default
     * maximum number of retransmissions is 3. However, if packet
     * drops are too high, the connection may get stall unless
     * the sender continues to retransmit packets that have not been
     * unacknowledged. We will use 0 instead, which means unlimited.
     *
     */
    public ReliableSocketProfile()
    {
        this(MAX_SEND_QUEUE_SIZE,
             MAX_RECV_QUEUE_SIZE,
             MAX_SEGMENT_SIZE,
             MAX_OUTSTANDING_SEGS,
             0/*MAX_RETRANS*/,
             MAX_CUMULATIVE_ACKS,
             MAX_OUT_OF_SEQUENCE,
             MAX_AUTO_RESET,
             NULL_SEGMENT_TIMEOUT,
             RETRANSMISSION_TIMEOUT,
             CUMULATIVE_ACK_TIMEOUT);
    }

    /**
     * Creates an profile with the specified RUDP parameter values.
     *
     * @param maxSendQueueSize      maximum send queue size (packets).
     * @param maxRecvQueueSize      maximum receive queue size (packets).
     * @param maxSegmentSize        maximum segment size (octets) (must be at least 22).
     * @param maxOutstandingSegs    maximum number of outstanding segments.
     * @param maxRetrans            maximum number of consecutive retransmissions (0 means unlimited).
     * @param maxCumulativeAcks     maximum number of unacknowledged received segments.
     * @param maxOutOfSequence      maximum number of out-of-sequence received segments.
     * @param maxAutoReset          maximum number of consecutive auto resets (not used).
     * @param nullSegmentTimeout    null segment timeout (ms).
     * @param retransmissionTimeout retransmission timeout (ms).
     * @param cumulativeAckTimeout  cumulative acknowledge timeout (ms).
     */
    public ReliableSocketProfile(int maxSendQueueSize,
                                 int maxRecvQueueSize,
                                 int maxSegmentSize,
                                 int maxOutstandingSegs,
                                 int maxRetrans,
                                 int maxCumulativeAcks,
                                 int maxOutOfSequence,
                                 int maxAutoReset,
                                 int nullSegmentTimeout,
                                 int retransmissionTimeout,
                                 int cumulativeAckTimeout)
    {
        checkValue("maxSendQueueSize",      maxSendQueueSize,      1,   255);
        checkValue("maxRecvQueueSize",      maxRecvQueueSize,      1,   255);
        checkValue("maxSegmentSize",        maxSegmentSize,        22,  65535);
        checkValue("maxOutstandingSegs",    maxOutstandingSegs,    1,   255);
        checkValue("maxRetrans",            maxRetrans,            0,   255);
        checkValue("maxCumulativeAcks",     maxCumulativeAcks,     0,   255);
        checkValue("maxOutOfSequence",      maxOutOfSequence,      0,   255);
        checkValue("maxAutoReset",          maxAutoReset,          0,   255);
        checkValue("nullSegmentTimeout",    nullSegmentTimeout,    0,   65535);
        checkValue("retransmissionTimeout", retransmissionTimeout, 100, 65535);
        checkValue("cumulativeAckTimeout",  cumulativeAckTimeout,  100, 65535);

        _maxSendQueueSize      = maxSendQueueSize;
        _maxRecvQueueSize      = maxRecvQueueSize;
        _maxSegmentSize        = maxSegmentSize;
        _maxOutstandingSegs    = maxOutstandingSegs;
        _maxRetrans            = maxRetrans;
        _maxCumulativeAcks     = maxCumulativeAcks;
        _maxOutOfSequence      = maxOutOfSequence;
        _maxAutoReset          = maxAutoReset;
        _nullSegmentTimeout    = nullSegmentTimeout;
        _retransmissionTimeout = retransmissionTimeout;
        _cumulativeAckTimeout  = cumulativeAckTimeout;
    }

    /**
     * Returns the maximum send queue size (packets).
     */
    public int maxSendQueueSize()
    {
        return _maxSendQueueSize;
    }

    /**
     * Returns the maximum receive queue size (packets).
     */
    public int maxRecvQueueSize()
    {
        return _maxRecvQueueSize;
    }

    /**
     * Returns the maximum segment size (octets).
     */
    public int maxSegmentSize()
    {
        return _maxSegmentSize;
    }

    /**
     * Returns the maximum number of outstanding segments.
     */
    public int maxOutstandingSegs()
    {
        return _maxOutstandingSegs;
    }

    /**
     * Returns the maximum number of consecutive retransmissions (0 means unlimited).
     */
    public int maxRetrans()
    {
        return _maxRetrans;
    }

    /**
     * Returns the maximum number of unacknowledged received segments.
     */
    public int maxCumulativeAcks()
    {
        return _maxCumulativeAcks;
    }

    /**
     * Returns the maximum number of out-of-sequence received segments.
     */
    public int maxOutOfSequence()
    {
        return _maxOutOfSequence;
    }

    /**
     * Returns the maximum number of consecutive auto resets.
     */
    public int maxAutoReset()
    {
        return _maxAutoReset;
    }

    /**
     * Returns the null segment timeout (ms).
     */
    public int nullSegmentTimeout()
    {
        return _nullSegmentTimeout;
    }

    /**
     * Returns the retransmission timeout (ms).
     */
    public int retransmissionTimeout()
    {
        return _retransmissionTimeout;
    }

    /**
     * Returns the cumulative acknowledge timeout (ms).
     */
    public int cumulativeAckTimeout()
    {
        return _cumulativeAckTimeout;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(_maxSendQueueSize).append(", ");
        sb.append(_maxRecvQueueSize).append(", ");
        sb.append(_maxSegmentSize).append(", ");
        sb.append(_maxOutstandingSegs).append(", ");
        sb.append(_maxRetrans).append(", ");
        sb.append(_maxCumulativeAcks).append(", ");
        sb.append(_maxOutOfSequence).append(", ");
        sb.append(_maxAutoReset).append(", ");
        sb.append(_nullSegmentTimeout).append(", ");
        sb.append(_retransmissionTimeout).append(", ");
        sb.append(_cumulativeAckTimeout);
        sb.append("]");
        return sb.toString();
    }

    private void checkValue(String param,
                                 int value,
                                 int minValue,
                                 int maxValue)
    {
        if (value < minValue || value > maxValue) {
            throw new IllegalArgumentException(param);
        }
    }

    private int _maxSendQueueSize;
    private int _maxRecvQueueSize;
    private int _maxSegmentSize;
    private int _maxOutstandingSegs;
    private int _maxRetrans;
    private int _maxCumulativeAcks;
    private int _maxOutOfSequence;
    private int _maxAutoReset;
    private int _nullSegmentTimeout;
    private int _retransmissionTimeout;
    private int _cumulativeAckTimeout;
}
