#!/bin/bash
# Usage: bash tests/run-fixture.sh <name> [--save]
#   <name>          run tests/fixtures/<name>.csv and compare with tests/expected/<name>.out/.err
#   <name> --save   run it and (over)write the expected files with the actual output
# Main reads src/task.csv, so the fixture is swapped in and the original is ALWAYS restored.

cd "$(dirname "$0")/.." || exit 1

name="${1:-}"
mode="${2:-}"
[ -n "$name" ] || { echo "Usage: bash tests/run-fixture.sh <fixture-name> [--save]"; exit 2; }

fixture="tests/fixtures/$name.csv"
[ -f "$fixture" ] || { echo "Fixture not found: $fixture"; exit 2; }

work=$(mktemp -d)
had_original=0
if [ -f src/task.csv ]; then cp src/task.csv "$work/task.csv.bak"; had_original=1; fi

cleanup() {
  if [ "$had_original" -eq 1 ]; then cp "$work/task.csv.bak" src/task.csv; else rm -f src/task.csv; fi
  rm -rf "$work"
}
trap cleanup EXIT

mkdir "$work/classes"
javac -d "$work/classes" $(find src/main -name '*.java') || { echo "Compile failed"; exit 2; }

cp "$fixture" src/task.csv
java -cp "$work/classes" main.java.Main > "$work/actual.out" 2> "$work/actual.err"

mkdir -p tests/expected
if [ "$mode" = "--save" ]; then
  cp "$work/actual.out" "tests/expected/$name.out"
  cp "$work/actual.err" "tests/expected/$name.err"
  echo "Saved tests/expected/$name.out and tests/expected/$name.err"
  exit 0
fi

[ -f "tests/expected/$name.out" ] || { echo "No expected files yet. Run with --save first."; exit 2; }

if diff --strip-trailing-cr "tests/expected/$name.out" "$work/actual.out" > /dev/null &&
   diff --strip-trailing-cr "tests/expected/$name.err" "$work/actual.err" > /dev/null; then
  echo "PASS $name"
else
  echo "FAIL $name"
  diff --strip-trailing-cr "tests/expected/$name.out" "$work/actual.out"
  diff --strip-trailing-cr "tests/expected/$name.err" "$work/actual.err"
  exit 1
fi