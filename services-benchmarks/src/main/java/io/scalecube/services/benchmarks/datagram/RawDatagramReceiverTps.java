package io.scalecube.services.benchmarks.datagram;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RawDatagramReceiverTps {

  public static void main(String[] args) throws Exception {
    Configurations.printSettings(RawDatagramReceiverTps.class);

    InetSocketAddress receiverAddress = new InetSocketAddress(8000);

    DatagramChannel receiver = DatagramChannel.open();
    DatagramSocket socket = receiver.socket();
    socket.setReuseAddress(true);
    socket.bind(receiverAddress);
    receiver.configureBlocking(false);
    System.out.println(
        "RawDatagramReceiverTps.receiver bound: " + receiver + " on " + receiverAddress);

    RateReporter reporter = new RateReporter();

    // receiver
    Thread receiverThread =
        new Thread(
            () -> {
              while (true) {
                ByteBuffer rcvBuffer = (ByteBuffer) Configurations.RECEIVER_BUFFER.position(0);
                SocketAddress srcAddress = Configurations.receive(receiver, rcvBuffer);
                if (srcAddress != null) {
                  ByteBuffer sndBuffer = (ByteBuffer) Configurations.SENDER_BUFFER.position(0);
                  sndBuffer.putLong(0, rcvBuffer.getLong(0)); // copy client time
                  sndBuffer.putLong(8, System.nanoTime()); // put server time
                  reporter.onMessage(1, rcvBuffer.capacity());
                }
              }
            });
    receiverThread.setUncaughtExceptionHandler((t, e) -> e.printStackTrace());
    receiverThread.start();
  }
}