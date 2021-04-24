package com.gui.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import static com.gui.http.HttpStatus.*;
import static org.junit.jupiter.api.Assertions.*;

public class ResponseTest {

    private ByteArrayOutputStream output;

    @BeforeEach
    public void setup() {
        output = new ByteArrayOutputStream();
    }

    @Test
    public void whenResponseIsSent_shouldWriteToOutputStream() throws IOException {
        new Response(NOT_FOUND).send(output);
        assertEquals("HTTP/1.1 404 Not Found\r\n", output.toString());
    }

}
