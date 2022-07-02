package io.socket;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class Fiddle {

    public static void main(String[] argz) throws Exception {
        BigInteger ms = BigInteger.valueOf(10).pow(23);
        long value = ms.longValue();
        System.out.println(value);
        System.out.println(Long.MAX_VALUE);
    }
}
