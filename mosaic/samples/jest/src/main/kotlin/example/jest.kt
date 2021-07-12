package example

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.jakewharton.mosaic.Color.Companion.Black
import com.jakewharton.mosaic.Color.Companion.Green
import com.jakewharton.mosaic.Color.Companion.Red
import com.jakewharton.mosaic.Color.Companion.Yellow
import com.jakewharton.mosaic.Column
import com.jakewharton.mosaic.Row
import com.jakewharton.mosaic.Text
import com.jakewharton.mosaic.TextStyle.Companion.Bold
import com.jakewharton.mosaic.runMosaic
import example.TestState.Fail
import example.TestState.Pass
import example.TestState.Running
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun main() = runMosaic {
	// TODO https://github.com/JakeWharton/mosaic/issues/3
	val tests = mutableStateListOf<Test>()

	setContent {
		val (done, running) = tests.partition { it.state != Running }
		Column {
			if (done.isNotEmpty()) {
				for (test in done) {
					TestRow(test)
				}
				Text("") // Blank line
			}

			if (running.isNotEmpty()) {
				for (test in running) {
					TestRow(test)
				}
				Text("") // Blank line
			}

			Summary(tests)
		}
	}

	val paths = ArrayDeque(listOf(
		"tests/login.kt",
		"tests/signup.kt",
		"tests/forgot-password.kt",
		"tests/reset-password.kt",
		"tests/view-profile.kt",
		"tests/edit-profile.kt",
		"tests/delete-profile.kt",
		"tests/posts.kt",
		"tests/post.kt",
		"tests/comments.kt",
	))
	repeat(4) { // Number of test workers.
		launch {
			while (true) {
				val path = paths.removeFirstOrNull() ?: break
				val index = Snapshot.withMutableSnapshot {
					val nextIndex = tests.size
					tests += Test(path, Running)
					nextIndex
				}
				delay(random.nextLong(2_500L, 4_000L))

				// Flip a coin biased 60% to pass to produce the final state of the test.
				val newState = if (random.nextFloat() < .6f) Pass else Fail
				tests[index] = tests[index].copy(state = newState)
			}
		}
	}
}

@Composable
fun TestRow(test: Test) {
	Row {
		val bg = when (test.state) {
			Running -> Yellow
			Pass -> Green
			Fail -> Red
		}
		// TODO use start/end padding of 1 to achieve spacing
		val state = when (test.state) {
			Running -> " RUNS "
			Pass -> " PASS "
			Fail -> " FAIL "
		}
		Text(state, color = Black, background = bg)

		val dir = test.path.substringBeforeLast('/')
		val name = test.path.substringAfterLast('/')
		Text(" $dir/")
		Text(name, style = Bold)
	}
}

@Composable
private fun Summary(tests: SnapshotStateList<Test>) {
	Row {
		Text("Tests: ")

		val failed = tests.count { it.state == Fail }
		if (failed > 0) {
			Text("$failed failed", color = Red)
			Text(", ")
		}

		val passed = tests.count { it.state == Pass }
		if (passed > 0) {
			Text("$passed passed", color = Green)
			Text(", ")
		}

		Text("${tests.size} total")
	}

	var elapsed by remember { mutableStateOf(0) }
	LaunchedEffect(Unit) {
		while (true) {
			delay(1_000)
			elapsed++
		}
	}
	Text("Time:  ${elapsed}s")
}

data class Test(
	val path: String,
	val state: TestState,
)

enum class TestState {
	Running,
	Pass,
	Fail,
}

// Use a random with a fixed seed for deterministic output.
private val random = Random(1234)
