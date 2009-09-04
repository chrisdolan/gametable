/*
 * WebServer.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.applet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * TODO: comment
 */
public class WebServer1 {
    private final ServerSocket m_socket;
    private final int m_numThreads;
    private final List<Thread> m_threads;

    public WebServer1(int port, int numThreads) throws IOException {
        m_numThreads = numThreads;
        m_socket = new ServerSocket(port);
        m_threads = new ArrayList<Thread>(numThreads);
    }
    public void start() {
        synchronized (m_threads) {
            for (int i=0; i<m_numThreads; ++i) {
                Thread t = new Thread(new Runnable() {
                    public void run()
                    {
                        try
                        {
                            Socket clientSocket = m_socket.accept();
                            OutputStream outputStream = clientSocket.getOutputStream();
                            try {
                                InputStream inputStream = clientSocket.getInputStream();
                                try {
                                    Response response = processRequest(new Request(inputStream));
                                    writeResponse(outputStream, response);
                                } finally {
                                    inputStream.close();
                                }
                            } finally {
                                outputStream.close();
                            }
                        }
                        catch (IOException exception)
                        {
                            // TODO Auto-generated catch block
                            exception.printStackTrace();
                        }
                    }
                });
                t.setDaemon(true);
                t.setName("HTTP Accept " + i);
                t.start();
                m_threads.add(t);
            }
        }
    }
    public void stop() {
        synchronized (m_threads) {
            for (Thread t : m_threads)
                t.interrupt();
            m_threads.clear();
        }
    }

    private void writeResponse(OutputStream outputStream, Response response)
    {
        // TODO Auto-generated method stub
        
    }

    private Response processRequest(Request request)
    {
        // TODO Auto-generated method stub
        return null;
    }

    private final class Response
    {

    }
    private final class Request
    {

        /**
         * @param inputStream
         */
        public Request(InputStream inputStream)
        {
            Reader reader;
            // TODO Auto-generated constructor stub
            
        }

    }
}
