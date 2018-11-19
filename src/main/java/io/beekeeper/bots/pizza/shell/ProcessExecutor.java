package io.beekeeper.bots.pizza.shell;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

public class ProcessExecutor {

    private static class StreamGobbler implements Runnable {

        private final InputStream inputStream;
        private final Consumer<String> consumer;

        StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                    .forEach(line -> {
                        System.out.println(line);
                        consumer.accept(line + "\n");
                    });
        }
    }

    public static class CommandResult {
        private final String stdout;
        private final String stderr;
        private final int exitCode;

        public CommandResult(String stdout, String stderr, int exitCode) {
            this.stdout = stdout;
            this.stderr = stderr;
            this.exitCode = exitCode;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    public static CommandResult executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        Process process = builder.start();

        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();

        Thread stdoutGobbler = new Thread(new StreamGobbler(process.getInputStream(), stdout::append));
        Thread stderrGobbler = new Thread(new StreamGobbler(process.getErrorStream(), stderr::append));

        stdoutGobbler.start();
        stderrGobbler.start();

        int exitCode = process.waitFor();

        stdoutGobbler.join();
        stderrGobbler.join();

        return new CommandResult(stdout.toString(), stderr.toString(), exitCode);
    }
}
