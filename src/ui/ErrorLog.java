package ui;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  Captures everything written to System.err (including the stack traces
 *  printed by the exception handlers scattered through the UI and the
 *  engine) into errors.log, timestamped, while still echoing to the
 *  console. Also records exceptions that escape any thread, notably the
 *  Swing event dispatch thread.
 *
 *  Installed once at startup; failures to open the log are ignored so the
 *  game always runs.
 *
 *  @author Pau
 *  @version 1.0
 */
final class ErrorLog {

    private static final String FILE = "errors.log";

    /** Redirects System.err to a tee that also appends to errors.log, and
     *  registers a catch-all handler for uncaught exceptions. */
    static void install(){
        final PrintStream console = System.err;
        OutputStream file;
        try{
            file = new FileOutputStream(FILE, true);
        }
        catch(IOException e){
            return;     // No log file available: leave stderr untouched
        }

        System.setErr(new PrintStream(new TimestampingTee(console, file), true));

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler(){
            public void uncaughtException(Thread t, Throwable e){
                System.err.println("UNCAUGHT in thread " + t.getName() + ":");
                e.printStackTrace();
            }
        });

        System.err.println("--- Hexodus started ---");
    }

    /** Writes to the console unchanged and to the log one line at a time,
     *  each prefixed with a timestamp. */
    private static class TimestampingTee extends OutputStream {
        private final PrintStream console;
        private final OutputStream file;
        private final ByteArrayOutputStream line = new ByteArrayOutputStream();
        private final SimpleDateFormat stamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        TimestampingTee(PrintStream console, OutputStream file){
            this.console = console;
            this.file = file;
        }

        public void write(int b) throws IOException {
            console.write(b);
            line.write(b);
            if(b == '\n') flushLine();
        }

        public void write(byte[] b, int off, int len) throws IOException {
            for(int i = 0; i < len; i++) write(b[off + i]);
        }

        private void flushLine() throws IOException {
            file.write(("[" + stamp.format(new Date()) + "] ").getBytes());
            file.write(line.toByteArray());
            file.flush();
            line.reset();
        }

        public void flush() throws IOException {
            console.flush();
            file.flush();
        }
    }

    private ErrorLog(){
    }
}
