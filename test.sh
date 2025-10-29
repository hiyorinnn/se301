#!/bin/bash

# === CONFIGURATION ===
WARMUP_RUNS=10      # how many warm-up runs to perform (not measured)
RUNS=100            # how many runs to measure

# === ASK FOR JAR FILE ===
read -p "Enter the JAR file name (e.g. test_v01-jar-with-dependencies.jar): " JAR_NAME

# Check if file exists
if [ ! -f "$JAR_NAME" ]; then
  echo "Error: File '$JAR_NAME' not found!"
  exit 1
fi

# === COMMAND SETUP ===
CMD="java -jar $JAR_NAME datasets/large/in.txt datasets/large/dictionary.txt datasets/large/out.txt"
echo
echo "Using JAR: $JAR_NAME"
echo "Warm-up runs: $WARMUP_RUNS"
echo "Measured runs: $RUNS"
echo

# === WARM-UP PHASE ===
echo "Warming up ($WARMUP_RUNS runs, not measured)..."
for i in $(seq 1 $WARMUP_RUNS); do
  echo " Warm-up #$i"
  $CMD >/dev/null 2>&1
done
echo "Warm-up complete."
echo

# === MAIN BENCHMARK ===
echo "Running benchmark ($RUNS measured runs)..."
times=()

for i in $(seq 1 $RUNS); do
  echo "Run #$i"
  start=$(date +%s%3N)
  $CMD >/dev/null 2>&1
  end=$(date +%s%3N)
  duration=$((end - start))
  times+=("$duration")
done

echo
echo "Collected times (ms): ${times[@]}"

# === CALCULATIONS ===
sorted=($(printf '%s\n' "${times[@]}" | sort -n))
discard_count=$((RUNS / 5))
keep_count=$((RUNS - discard_count))
trimmed=("${sorted[@]:0:$keep_count}")

avg_all=$(printf '%s\n' "${times[@]}" | awk '{sum+=$1} END {print sum/NR}')
avg_trimmed=$(printf '%s\n' "${trimmed[@]}" | awk '{sum+=$1} END {print sum/NR}')
stddev=$(printf '%s\n' "${times[@]}" | awk -v mean="$avg_all" '{sum+=($1-mean)^2} END {print sqrt(sum/NR)}')

echo
echo "=== RESULTS (milliseconds) ==="
echo "Warm-up runs: $WARMUP_RUNS"
echo "Measured runs: $RUNS"
echo "Discarded (top 20%): $discard_count"
echo "Kept for trimmed average: $keep_count"
echo "Average (all): $avg_all ms"
echo "Average (trimmed): $avg_trimmed ms"
echo "Standard deviation: $stddev ms"
echo "Consistency (stddev / avg_all): $(awk -v s="$stddev" -v a="$avg_all" 'BEGIN {print (s/a)*100}')%"

