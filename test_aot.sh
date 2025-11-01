#!/bin/bash

# === CONFIGURATION ===
TRAINING_RUNS=5      # how many training runs to generate AOT cache (not measured)
WARMUP_RUNS=10      # how many warm-up runs to perform (not measured)
RUNS=100            # how many runs to measure

# === ASK FOR JAR FILE ===
read -p "Enter the JAR file name (e.g. jdk25_test_v01-jar-with-dependencies.jar): " JAR_NAME

# Check if file exists
if [ ! -f "$JAR_NAME" ]; then
  echo "Error: File '$JAR_NAME' not found!"
  exit 1
fi

# === AOT CACHE FILE ===
AOT_CACHE_FILE="aot_cache_${JAR_NAME%.jar}.aot"

# === COMMAND SETUP ===
# Baseline: Without AOT
BASELINE_CMD="java -jar $JAR_NAME datasets/large/in.txt datasets/large/dictionary.txt datasets/large/out.txt"
# Phase 1: AOT Method Profiling (Training - generates cache) - JDK 25 JEP 515
TRAINING_CMD="java -XX:AOTCacheOutput=$AOT_CACHE_FILE -jar $JAR_NAME datasets/large/in.txt datasets/large/dictionary.txt datasets/large/out.txt"
# Phase 2: AOT Method Profiling (Using cache - fastest) - JDK 25 JEP 515
CACHE_CMD="java -XX:AOTCache=$AOT_CACHE_FILE -jar $JAR_NAME datasets/large/in.txt datasets/large/dictionary.txt datasets/large/out.txt"

# Check if AOT is available (test with a simple command)
echo "Checking if AOT Method Profiling (JEP 515) is available..."
AOT_TEST_OUTPUT=$(java -XX:AOTCacheOutput=/tmp/test_aot_check.aot -version 2>&1)
if echo "$AOT_TEST_OUTPUT" | grep -q "Unrecognized VM option\|Error: Could not create the Java Virtual Machine"; then
  AOT_AVAILABLE=0
  echo "⚠ AOT Method Profiling is NOT available in this JDK build"
  echo "  AOT requires JDK 25 with AOT support"
  echo "  Will run baseline-only comparison test"
else
  AOT_AVAILABLE=1
  echo "✓ AOT Method Profiling (JEP 515) is available"
  # Clean up test cache file
  rm -f /tmp/test_aot_check.aot /tmp/test_aot_check.aot.config 2>/dev/null
fi
echo

echo
echo "=========================================="
echo "JDK 25 Performance Test: AOT Method Profiling (JEP 515)"
echo "=========================================="
echo "JDK Version: Requires JDK 25 with AOT support"
echo "Feature: AOT Method Profiling - JDK 25 ONLY"
echo "Using JAR: $JAR_NAME"
echo "Warm-up runs: $WARMUP_RUNS"
echo "Measured runs: $RUNS"
echo "=========================================="
echo

# ============================================================
# PHASE 1: BASELINE (Without AOT)
# ============================================================
echo "=========================================="
echo "PHASE 1: BASELINE (Without AOT)"
echo "=========================================="
echo

echo "Warming up baseline ($WARMUP_RUNS runs, not measured)..."
for i in $(seq 1 $WARMUP_RUNS); do
  echo " Warm-up #$i"
  $BASELINE_CMD >/dev/null 2>&1
done
echo "Baseline warm-up complete."
echo

echo "Running baseline benchmark ($RUNS measured runs)..."
baseline_times=()

for i in $(seq 1 $RUNS); do
  echo "Baseline Run #$i"
  start=$(date +%s%N)
  $BASELINE_CMD >/dev/null 2>&1
  end=$(date +%s%N)
  duration=$(( (end - start) / 1000000 ))
  baseline_times+=("$duration")
done

# Calculate baseline stats
baseline_sorted=($(printf '%s\n' "${baseline_times[@]}" | sort -n))
baseline_discard_count=$((RUNS / 5))
baseline_keep_count=$((RUNS - baseline_discard_count))
baseline_trimmed=("${baseline_sorted[@]:0:$baseline_keep_count}")

baseline_avg_all=$(printf '%s\n' "${baseline_times[@]}" | awk '{sum+=$1} END {print sum/NR}')
baseline_avg_trimmed=$(printf '%s\n' "${baseline_trimmed[@]}" | awk '{sum+=$1} END {print sum/NR}')
baseline_stddev=$(printf '%s\n' "${baseline_times[@]}" | awk -v mean="$baseline_avg_all" '{sum+=($1-mean)^2} END {print sqrt(sum/NR)}')

echo
echo "Baseline Results:"
echo "  Average (all): $baseline_avg_all ms"
echo "  Average (trimmed): $baseline_avg_trimmed ms"
echo "  Standard deviation: $baseline_stddev ms"
echo

# ============================================================
# PHASE 2: AOT TRAINING
# ============================================================
echo "=========================================="
echo "PHASE 2: AOT TRAINING (Generate Cache)"
echo "=========================================="
echo

if [ "$AOT_AVAILABLE" = "0" ]; then
  echo "⚠ Skipping AOT training - AOT not available"
  echo "  Exiting as AOT Method Profiling is required for this test"
  exit 0
fi

echo "Training AOT profile ($TRAINING_RUNS runs, generates cache)..."
echo "Cache file: $AOT_CACHE_FILE"
# Remove existing cache if present
rm -f "$AOT_CACHE_FILE" "${AOT_CACHE_FILE}.config" 2>/dev/null

for i in $(seq 1 $TRAINING_RUNS); do
  echo " Training run #$i"
  # Capture output but don't suppress warnings (they're normal for AOT)
  $TRAINING_CMD 2>&1 | grep -v "^\[" | grep -v "Skipping" >/dev/null || true
done
echo "Training complete. AOT cache generated: $AOT_CACHE_FILE"

# Verify cache was created
if [ ! -f "$AOT_CACHE_FILE" ]; then
  echo "⚠ ERROR: AOT cache file was not created: $AOT_CACHE_FILE"
  echo "  Continuing anyway..."
fi
echo

# ============================================================
# PHASE 3: WITH AOT CACHE
# ============================================================
echo "=========================================="
echo "PHASE 3: WITH AOT CACHE"
echo "=========================================="
echo

echo "Warming up with AOT cache ($WARMUP_RUNS runs, not measured)..."
for i in $(seq 1 $WARMUP_RUNS); do
  echo " Warm-up #$i"
  $CACHE_CMD >/dev/null 2>&1
done
echo "AOT warm-up complete."
echo

echo "Running AOT cache benchmark ($RUNS measured runs)..."
feature_times=()

for i in $(seq 1 $RUNS); do
  echo "AOT Run #$i"
  start=$(date +%s%N)
  $CACHE_CMD >/dev/null 2>&1
  end=$(date +%s%N)
  duration=$(( (end - start) / 1000000 ))
  feature_times+=("$duration")
done

# Calculate feature stats
feature_sorted=($(printf '%s\n' "${feature_times[@]}" | sort -n))
feature_discard_count=$((RUNS / 5))
feature_keep_count=$((RUNS - feature_discard_count))
feature_trimmed=("${feature_sorted[@]:0:$feature_keep_count}")

feature_avg_all=$(printf '%s\n' "${feature_times[@]}" | awk '{sum+=$1} END {print sum/NR}')
feature_avg_trimmed=$(printf '%s\n' "${feature_trimmed[@]}" | awk '{sum+=$1} END {print sum/NR}')
feature_stddev=$(printf '%s\n' "${feature_times[@]}" | awk -v mean="$feature_avg_all" '{sum+=($1-mean)^2} END {print sqrt(sum/NR)}')

echo
echo "AOT Results:"
echo "  Average (all): $feature_avg_all ms"
echo "  Average (trimmed): $feature_avg_trimmed ms"
echo "  Standard deviation: $feature_stddev ms"
echo

# ============================================================
# COMPARISON
# ============================================================
echo
echo "=========================================="
echo "=== COMPARISON RESULTS ==="
echo "=========================================="
echo "Baseline (Without AOT):"
echo "  Average (trimmed): $baseline_avg_trimmed ms"
echo
echo "With AOT Method Profiling (Cache):"
echo "  Average (trimmed): $feature_avg_trimmed ms"
echo

# Calculate improvement
improvement=$(awk -v baseline="$baseline_avg_trimmed" -v feature="$feature_avg_trimmed" 'BEGIN {printf "%.2f", ((baseline - feature) / baseline) * 100}')
speedup=$(awk -v baseline="$baseline_avg_trimmed" -v feature="$feature_avg_trimmed" 'BEGIN {printf "%.3f", baseline / feature}')
is_faster=$(awk -v baseline="$baseline_avg_trimmed" -v feature="$feature_avg_trimmed" 'BEGIN {print (feature < baseline) ? 1 : 0}')

if [ "$is_faster" = "1" ]; then
  echo "✓ IMPROVEMENT: $improvement% faster with AOT"
  echo "✓ SPEEDUP: ${speedup}x faster"
else
  echo "⚠ SLOWER: $improvement% slower with AOT (may not be beneficial or cache not effective)"
  echo "⚠ SPEEDUP: ${speedup}x slower"
fi

echo
echo "=========================================="
echo "=== DETAILED RESULTS ==="
echo "=========================================="
echo "Baseline - Warm-up runs: $WARMUP_RUNS, Measured runs: $RUNS"
echo "Baseline - Average (all): $baseline_avg_all ms"
echo "Baseline - Average (trimmed): $baseline_avg_trimmed ms"
echo "Baseline - Standard deviation: $baseline_stddev ms"
echo
echo "AOT - Training runs: $TRAINING_RUNS, Warm-up runs: $WARMUP_RUNS, Measured runs: $RUNS"
echo "AOT - Average (all): $feature_avg_all ms"
echo "AOT - Average (trimmed): $feature_avg_trimmed ms"
echo "AOT - Standard deviation: $feature_stddev ms"
echo "=========================================="

