#!/bin/bash

# === CONFIGURATION ===
JAR_PATH="jdk25_test_v01-jar-with-dependencies.jar"
INPUT="../datasets/small/in.txt"
DICT="../datasets/small/dictionary.txt"
OUTPUT="../datasets/small/out.txt"
RUNS=100
WARMUP=10
PROFILE_FILE="app-profile.jfr"

echo "Collecting AOT profile (JEP 515)..."
java -XX:+ProfileVM -XX:ProfileOutput="$PROFILE_FILE" -jar "$JAR_PATH" "$INPUT" "$DICT" "$OUTPUT" >/dev/null 2>&1

echo "Running performance test (with JEP 515)..."

times=()

for ((i=1; i<=WARMUP; i++)); do
    java -XX:+UseAOTProfile -XX:AOTProfile="$PROFILE_FILE" -jar "$JAR_PATH" "$INPUT" "$DICT" "$OUTPUT" >/dev/null 2>&1
done

for ((i=1; i<=RUNS; i++)); do
    START=$(date +%s%N)
    java -XX:+UseAOTProfile -XX:AOTProfile="$PROFILE_FILE" -jar "$JAR_PATH" "$INPUT" "$DICT" "$OUTPUT" >/dev/null 2>&1
    END=$(date +%s%N)
    ELAPSED_MS=$(( (END - START) / 1000000 ))
    times+=($ELAPSED_MS)
done

echo
echo "Collected times (ms): ${times[@]}"
echo

sorted=($(printf '%s\n' "${times[@]}" | sort -n))

sum=0
for t in "${sorted[@]}"; do
    sum=$((sum + t))
done
avg_all=$(awk "BEGIN {printf \"%.3f\", $sum / ${#sorted[@]}}")

discard=$((RUNS * 20 / 100))
keep=$((RUNS - discard))
trimmed=("${sorted[@]:0:$keep}")

sum_trimmed=0
for t in "${trimmed[@]}"; do
    sum_trimmed=$((sum_trimmed + t))
done
avg_trimmed=$(awk "BEGIN {printf \"%.3f\", $sum_trimmed / ${#trimmed[@]}}")

mean=$avg_all
sum_sq=0
for t in "${sorted[@]}"; do
    diff=$(awk "BEGIN {print $t - $mean}")
    sum_sq=$(awk "BEGIN {print $sum_sq + ($diff * $diff)}")
done
stddev=$(awk "BEGIN {printf \"%.3f\", sqrt($sum_sq / ${#sorted[@]})}")
consistency=$(awk "BEGIN {printf \"%.5f\", ($stddev / $avg_all) * 100}")

echo "=== RESULTS (milliseconds) ==="
echo "Warm-up runs: $WARMUP"
echo "Measured runs: $RUNS"
echo "Discarded (top 20%): $discard"
echo "Kept for trimmed average: $keep"
echo "Average (all): $avg_all ms"
echo "Average (trimmed): $avg_trimmed ms"
echo "Standard deviation: $stddev ms"
echo "Consistency (stddev / avg_all): $consistency%"
