package com.jakewharton.mosaic

import org.fusesource.jansi.AnsiConsole
import java.nio.charset.StandardCharsets.UTF_8
import java.util.concurrent.TimeUnit.NANOSECONDS

internal interface Output {
	fun display(output: String)
}

internal object DebugOutput : Output {
	private var lastRenderNanos = 0L

	override fun display(output: String) {
		println(buildString {
			val renderNanos = System.nanoTime()

			if (lastRenderNanos != 0L) {
				repeat(50) { append('~') }
				append(" +")
				val nanoDiff = renderNanos - lastRenderNanos
				append(NANOSECONDS.toMillis(nanoDiff))
				appendLine("ms")
			}
			lastRenderNanos = renderNanos

			appendLine(output)
		})
	}
}

internal object AnsiOutput : Output {
	private var lastHeight = 0

	override fun display(output: String) {
		val rendered = buildString {
			val lines = output.split("\n")

			repeat(lastHeight) {
				append("\u001B[F") // Cursor up line.
			}

			for (line in lines) {
				append(line)
				append("\u001B[K") // Clear rest of line.
				append('\n')
			}

			// If the new output contains fewer lines than the last output, clear those old lines.
			for (i in 0 until lastHeight - lines.size) {
				if (i > 0) {
					append('\n')
				}
				append("\u001B[K") // Clear line.
			}

			lastHeight = lines.size
		}

		with(AnsiConsole.out()) {
			// Write a single byte array to stdout to create an atomic visual change. If you instead write
			// the string, it will be UTF-8 encoded using an intermediate buffer that appears to be
			// periodically flushed to the underlying byte stream. This will cause fraction-of-a-second
			// flickers of broken content. Note that this only occurs with the AnsiConsole stream, but
			// there's no harm in doing it unconditionally.
			write(rendered.toByteArray(UTF_8))

			// Explicitly flush to ensure the trailing line clear is sent. Empirically, this appears to be
			// buffered and not processed until the next frame, or not at all on the final frame.
			flush()
		}
	}
}
