package io.beekeeper.bots.pizza.shell

import io.beekeeper.bots.pizza.extensions.logger
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.function.Consumer

object ProcessExecutor {

    private val log = logger()

    private class StreamGobbler internal constructor(private val inputStream: InputStream, private val consumer: Consumer<String>) : Runnable {

        override fun run() {
            BufferedReader(InputStreamReader(inputStream)).lines()
                    .forEach { line ->
                        log.info(line)
                        consumer.accept(line + "\n")
                    }
        }
    }

    class CommandResult(val stdout: String, val stderr: String, val exitCode: Int)

    @Throws(IOException::class, InterruptedException::class)
    fun executeCommand(command: List<String>): CommandResult {
        val process = ProcessBuilder()
                .command(command)
                .start()

        val stdout = StringBuilder()
        val stderr = StringBuilder()

        val stdoutGobbler = Thread(StreamGobbler(process.inputStream, Consumer { stdout.append(it) }))
        val stderrGobbler = Thread(StreamGobbler(process.errorStream, Consumer { stderr.append(it) }))

        stdoutGobbler.start()
        stderrGobbler.start()

        val exitCode = process.waitFor()

        stdoutGobbler.join()
        stderrGobbler.join()

        return CommandResult(stdout.toString(), stderr.toString(), exitCode)
    }
}
