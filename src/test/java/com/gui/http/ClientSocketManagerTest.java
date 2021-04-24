package com.gui.http;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static com.gui.http.HttpStatus.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ClientSocketManagerTest {

    @Mock private Socket socket;
    @Mock private HttpHandler handler;
    private ByteArrayOutputStream output;
    private ClientSocketManager manager;

    @BeforeEach
    public void setup() throws IOException {
        output = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(output);
        manager = new ClientSocketManager(socket, handler);
    }

    @Test
    public void whenRequestIsValid_shouldHandleClientConnection() throws Exception {
        givenInput("HEAD / HTTP/1.1");
        when(handler.handle(any())).thenReturn(new Response(OK));

        manager.handleClientConnection();

        assertEquals("HTTP/1.1 200 Ok\r\n", output.toString());
        verify(handler, times(1)).handle(any());
    }

    @Test
    public void whenRequestIsMalFormed_shouldReturn400() throws Exception {
        givenInput("invalidRequest");

        manager.handleClientConnection();

        assertEquals("HTTP/1.1 400 Bad Request\r\n", output.toString());
        verify(handler, times(0)).handle(any());
    }

    @Test
    public void whenHandlerThrowsException_shouldReturn500() throws Exception {
        givenInput("HEAD / HTTP/1.1");
        when(handler.handle(any())).thenAnswer( invocation -> {
            throw new IOException("some error while reading content");
        });

        manager.handleClientConnection();

        assertEquals("HTTP/1.1 500 Internal Server Error\r\n", output.toString());
        verify(handler, times(1)).handle(any());
    }

    @Test
    public void whenWriteToOutputThrowsException_shouldLogAndReturn() throws Exception {
        givenInput("HEAD / HTTP/1.1");
        when(handler.handle(any())).thenReturn(new Response(OK));
        when(socket.getOutputStream()).thenThrow(new IOException("some error"));

        final TestAppender appender = new TestAppender();
        final Logger logger = Logger.getLogger(ClientSocketManager.class);
        logger.addAppender(appender);

        manager.handleClientConnection();

        assertFalse(appender.log.isEmpty());
        assertEquals("error writing response to socket output", appender.log.get(0).getMessage());
    }

    private void givenInput(String in) throws Exception {
        InputStream input = new ByteArrayInputStream(in.getBytes());
        when(socket.getInputStream()).thenReturn(input);
    }

    private class TestAppender extends AppenderSkeleton {
        private List<LoggingEvent> log = new ArrayList<>();
        @Override
        public boolean requiresLayout() {
            return false;
        }
        @Override
        protected void append(final LoggingEvent loggingEvent) {
            this.log.add(loggingEvent);
        }
        @Override
        public void close() {
        }
        public void clean() {
            this.log = new ArrayList<>();
        }
    }
}
