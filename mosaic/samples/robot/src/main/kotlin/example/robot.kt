package example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.runMosaic
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.jline.terminal.TerminalBuilder

private const val width = 20
private const val height = 10

fun main() = runMosaic {
	// TODO https://github.com/JakeWharton/mosaic/issues/3
	var x by mutableStateOf(0)
	var y by mutableStateOf(0)

	setContent {
		Column {
			Text("Use arrow keys to move the face. Press “q” to exit.")
			Text("Position: $x, $y   World: $width, $height")
			Text("")
			// TODO https://github.com/JakeWharton/mosaic/issues/11
			Text(buildString {
				// TODO https://github.com/JakeWharton/mosaic/issues/7
				repeat(y) { append('\n') }
				repeat(x) { append(' ') }
				append("^_^")

				repeat(height - y) { append('\n') }
			})
		}
	}

	withContext(IO) {
		val terminal = TerminalBuilder.terminal()
		terminal.enterRawMode()
		val reader = terminal.reader()

		while (true) {
			// TODO https://github.com/JakeWharton/mosaic/issues/10
			when (reader.read()) {
				'q'.code -> break
				27 -> {
					when (reader.read()) {
						91 -> {
							when (reader.read()) {
								65 -> y = (y - 1).coerceAtLeast(0)
								66 -> y = (y + 1).coerceAtMost(height)
								67 -> x = (x + 1).coerceAtMost(width)
								68 -> x = (x - 1).coerceAtLeast(0)
							}
						}
					}
				}
			}
		}
	}
}
