#!/usr/bin/env bash

if ! command -v svg-term &> /dev/null; then
    echo "Command 'svg-term' not found. Please install with 'npm install -g svg-term-cli'."
    exit
fi

set -e

REPO_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Ensure example binaries are available
"$REPO_DIR/gradlew" -q --console plain -p "$REPO_DIR" installDist

for example in $REPO_DIR/examples/*/; do
	example_name=$(basename "$example")
	echo "Capturing $example_name..."

	command="'$example/build/install/$example_name/bin/$example_name' 2>/dev/null && sleep 2 && echo"
	if [ -f "$example/input.sh" ]; then
		command="'$example/input.sh' | $command"
	fi

	svg-term "--command=$command" "--out=$example/demo.svg" --from=50 --window --width=60 --height=16 --no-cursor
	cat > "$example/README.md" <<EOL
# Example: $example_name

<img src="demo.svg">
EOL
done

echo "Done"
