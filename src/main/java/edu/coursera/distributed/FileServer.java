package edu.coursera.distributed;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * A basic and very limited implementation of a file server that responds to GET
 * requests from HTTP clients.
 */
public final class FileServer {
    /**
     * Main entrypoint for the basic file server.
     *
     * @param socket Provided socket to accept connections on.
     * @param fs     A proxy filesystem to serve files from. See the PCDPFilesystem
     *               class for more detailed documentation of its usage.
     * @param ncores The number of cores that are available to your
     *               multi-threaded file server. Using this argument is entirely
     *               optional. You are free to use this information to change
     *               how you create your threads, or ignore it.
     * @throws IOException If an I/O error is detected on the server. This
     *                     should be a fatal error, your file server
     *                     implementation is not expected to ever throw
     *                     IOExceptions during normal operation.
     */
    public void run(final ServerSocket socket, final PCDPFilesystem fs,
                    final int ncores) throws IOException {
        /*
         * Enter a spin loop for handling client requests to the provided
         * ServerSocket object.
         */

        ExecutorService executor = newFixedThreadPool(ncores);
        ThreadPoolExecutor pool = (ThreadPoolExecutor) executor;
        while (true) {
            Socket s = socket.accept();
            pool.submit(
                    () -> {
                        try (InputStream stream = s.getInputStream();
                             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                             PrintWriter printer = new PrintWriter(s.getOutputStream())) {

                            String line = bufferedReader.readLine();
                            if (line != null && line.startsWith("GET")) {
                                String path = line.split(" ")[1];
                                String data = fs.readFile(new PCDPPath(path));
                                if (data != null) {
                                    printer.write("HTTP/1.0 200 OK\r\n");
                                    printer.write("\r\n");
                                    printer.write("\r\n");
                                    printer.write(data + "\r\n");
                                } else {
                                    printer.write("HTTP/1.0 404 Not Found\r\n");
                                    printer.write("\r\n");
                                    printer.write("\r\n");
                                }
                            } else {
                                printer.write("HTTP/1.0 400 Bad Request\r\n");
                                printer.write("\r\n");
                                printer.write("\r\n");
                            }
                        } catch (IOException ignored) {

                        }
                    }
            );
        }
    }
}
