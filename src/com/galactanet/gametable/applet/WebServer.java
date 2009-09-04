/*
 * WebServer.java: GameTable is in the Public Domain.
 */


package com.galactanet.gametable.applet;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Date;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * HTTP server that delivers GameTable as an applet embedded in a web page to clients.
 */
public final class WebServer
{

    private static final int STOP_DELAY = 5;
    private final HttpServer m_server;

    public WebServer(int port) throws IOException
    {
        m_server = HttpServer.create(new InetSocketAddress(port), 10);
        m_server.createContext("/", new AppletServer());
        m_server.createContext("/gametable.jar", new JarServer());
        m_server.createContext("/com/galactanet", new ClassServer());
    }
    public void start()
    {
        m_server.start();
    }
    public void stop()
    {
        m_server.stop(STOP_DELAY);
    }

    private final class AppletServer implements HttpHandler
    {
        /*
         * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
         */
        public void handle(HttpExchange exchange) throws IOException
        {
            URI requestURI = exchange.getRequestURI();
            System.out.println("Base -- " + new Date() + ": " + requestURI);
            InputStream request = exchange.getRequestBody();
            try {
                ignoreRequest(request);
            } finally {
                request.close();
            }
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.set("Content-Type", "text/html; charset=UTF-8");
            byte[] html = slurpClassResource("applet.html");
            exchange.sendResponseHeaders(200, html.length);
            OutputStream response = exchange.getResponseBody();
            try {
                response.write(html);
            } finally {
                response.close();
            }
        }
    }


    private final class JarServer implements HttpHandler
    {
        /*
         * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
         */
        public void handle(HttpExchange exchange) throws IOException
        {
            try {
                URI requestURI = exchange.getRequestURI();
                System.out.println("Jar -- " + new Date() + ": " + requestURI);
                InputStream request = exchange.getRequestBody();
                try {
                    ignoreRequest(request);
                } finally {
                    request.close();
                }
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "application/octet-stream");
                byte[] file = slurpResource("gametable.jar");
                System.out.println(new Date().toString() + ": " + requestURI + " --> " + file.length);
                exchange.sendResponseHeaders(200, file.length);
                OutputStream response = exchange.getResponseBody();
                try {
                    response.write(file);
                } finally {
                    response.close();
                }
            } catch (IOException e) {
                System.out.println(e);
                throw e;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new IOException(t);
            }
        }
    }

    private final class ClassServer implements HttpHandler
    {
        /*
         * @see com.sun.net.httpserver.HttpHandler#handle(com.sun.net.httpserver.HttpExchange)
         */
        public void handle(HttpExchange exchange) throws IOException
        {
            try {
                URI requestURI = exchange.getRequestURI();
                System.out.println("Class -- " + new Date() + ": " + requestURI);
                InputStream request = exchange.getRequestBody();
                try {
                    ignoreRequest(request);
                } finally {
                    request.close();
                }
                Headers responseHeaders = exchange.getResponseHeaders();
                responseHeaders.set("Content-Type", "application/octet-stream");
                byte[] html = slurpResource(requestURI.getPath());
                System.out.println(new Date().toString() + ": " + requestURI + " --> " + html.length);
                exchange.sendResponseHeaders(200, html.length);
                OutputStream response = exchange.getResponseBody();
                try {
                    response.write(html);
                } finally {
                    response.close();
                }
            } catch (IOException e) {
                System.out.println(e);
                throw e;
            } catch (Throwable t) {
                t.printStackTrace();
                throw new IOException(t);
            }
        }
    }

    private void ignoreRequest(InputStream request) throws IOException
    {
        byte[] buffer = new byte[2048];
        while (true)
        {
            int bytesRead = request.read(buffer);
            if (bytesRead < 0)
                break;
        }
    }

    private byte[] slurpRelativeResource(String name) throws IOException {
        String pathRoot = WebServer.class.getPackage().getName().replace(".", "/");
        String path = pathRoot + "/" + name;
        return slurpResource(path);
    }
    private byte[] slurpResource(String path) throws IOException {
        //String relPath = path.startsWith("/") ? path.substring(1) : path; 
        String relPath = path.replaceFirst("^/*((?:[a-z]+/)*[a-zA-Z0-9_$]+\\.class)$", "$1");
        System.out.println("loading " + relPath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream stream = getClass().getClassLoader().getResourceAsStream(relPath);
        if (null == stream)
            throw new FileNotFoundException(path);
        try {
            byte[] buffer = new byte[2048];
            while (true)
            {
                int bytesRead = stream.read(buffer);
                if (bytesRead < 0)
                    break;
                if (bytesRead > 0)
                    baos.write(buffer, 0, bytesRead);
            }
        } finally {
            stream.close();
        }
        return baos.toByteArray();
    }
    private byte[] slurpClassResource(String name) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream stream = getClass().getResourceAsStream(name);
        if (null == stream)
            throw new FileNotFoundException(name);
        try {
            byte[] buffer = new byte[2048];
            while (true)
            {
                int bytesRead = stream.read(buffer);
                if (bytesRead < 0)
                    break;
                if (bytesRead > 0)
                    baos.write(buffer, 0, bytesRead);
            }
        } finally {
            stream.close();
        }
        return baos.toByteArray();
    }
}
