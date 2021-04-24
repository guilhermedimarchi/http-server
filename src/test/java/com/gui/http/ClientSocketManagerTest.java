package com.gui.http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientSocketManagerTest {

    @Mock
    private Socket socket;

    @Test
    public void shouldHandleClientConnection() throws Exception {
        InputStream input = new ByteArrayInputStream("GET /123 HTTP/1.1".getBytes());
        when(socket.getInputStream()).thenReturn(input);

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(response);

        ClientSocketManager manager = new ClientSocketManager(socket, new StaticHandler("./"));
        manager.handleClientConnection();

        assertEquals("HTTP/1.1 404 Not Found\r\n", response.toString());
    }


}
