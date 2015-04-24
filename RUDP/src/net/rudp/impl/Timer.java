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

public class Timer extends Thread
{
    public Timer(String name, Runnable task)
    {
        super(name);
        setDaemon(true);

        _task = task;
        _delay = 0;
        _period = 0;
        start();
    }

    public void run()
    {
        while (!_stopped) {

            synchronized (this) {

                while (!_scheduled && !_stopped) {
                    try {
                        wait();
                    }
                    catch (InterruptedException xcp) {
                        xcp.printStackTrace();
                    }
                }

                if (_stopped) {
                    break;
                }
            }

            synchronized (_lock) {

                _reset = false;
                _canceled = false;

                if (_delay > 0) {
                    try {
                        _lock.wait(_delay);
                    }
                    catch (InterruptedException xcp) {
                        xcp.printStackTrace();
                    }
                }

                if (_canceled) {
                    continue;
                }
            }

            if (!_reset) {
                _task.run();
            }

            if (_period > 0) {

                while (true) {

                    synchronized (_lock) {

                        _reset = false;

                        try {
                            _lock.wait(_period);
                        }
                        catch (InterruptedException xcp) {
                            xcp.printStackTrace();
                        }

                        if (_canceled) {
                            break;
                        }

                        if (_reset) {
                            continue;
                        }
                    }

                    _task.run();

                }
            }
        }
    }

    public synchronized void schedule(long delay)
    {
        schedule(delay, 0);
    }

    public synchronized void schedule(long delay, long period)
    {
        _delay = delay;
        _period = period;


        if (_scheduled) {
            throw new IllegalStateException("already scheduled");
        }

        _scheduled = true;
        notify();

        synchronized (_lock) {
            _lock.notify();
        }
    }

    public synchronized boolean isScheduled()
    {
        return _scheduled;
    }

    public synchronized boolean isIdle()
    {
        return !isScheduled();
    }

    public synchronized void reset()
    {
        synchronized (_lock) {
            _reset = true;
            _lock.notify();
        }
    }

    public synchronized void cancel()
    {
        _scheduled = false;
        synchronized (_lock) {
            _canceled = true;
            _lock.notify();
        }
    }

    public synchronized void destroy()
    {
        cancel();
        _stopped = true;
        notify();
    }

    private Runnable _task;
    private long     _delay;
    private long     _period;
    private boolean  _canceled;
    private boolean  _scheduled;
    private boolean  _reset;
    private boolean  _stopped;
    private Object   _lock = new Object();
}
