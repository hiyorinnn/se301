# JDK 25 Setup Guide for VM

## Quick Answer: Running JAR on Linux VM

**If you're only running the JAR file (not building):**
- ✅ You need: **JDK 25** (or JRE 25) installed on the Linux VM
- ❌ You do NOT need: Maven, build tools, or source code
- 📦 Just transfer the JAR file: `jdk25_test_v01-jar-with-dependencies.jar`

## Two Scenarios

### Scenario 1: Build on Windows, Run on Linux VM (Most Common)
If you build the JAR on Windows and transfer it to Linux:
1. **Windows machine:** Needs Maven + JDK 25 (to build)
2. **Linux VM:** Only needs JDK 25 (to run)
3. Transfer the JAR file to Linux VM

### Scenario 2: Build and Run on Linux VM
If you build everything on Linux:
1. **Linux VM:** Needs Maven + JDK 25 (to build and run)

---

## Prerequisites Check

1. **Check Current Java Version:**
   ```bash
   java -version
   ```
   Should show: `java version "25.0.1"` or similar

2. **Check Maven Version:**
   ```bash
   mvn -version
   ```

## Step 1: Install JDK 25 (if not already installed)

### Option A: Download from Oracle
1. Visit: https://www.oracle.com/java/technologies/downloads/#java25
2. Download JDK 25 for your OS (Windows/Linux/Mac)
3. Install following the installer instructions

### Option B: Using Package Manager

**On Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-25-jdk
```

**On CentOS/RHEL:**
```bash
sudo yum install java-25-openjdk-devel
```

**On Windows (using Chocolatey):**
```powershell
choco install openjdk25
```

## Step 2: Set JAVA_HOME Environment Variable

### On Windows:
1. Open System Properties → Environment Variables
2. Create/Edit `JAVA_HOME` variable:
   - Path: `C:\Program Files\Java\jdk-25` (or your JDK installation path)
3. Add to `Path` variable:
   - `%JAVA_HOME%\bin`

Or using PowerShell (run as Administrator):
```powershell
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-25", "Machine")
[System.Environment]::SetEnvironmentVariable("Path", $env:Path + ";%JAVA_HOME%\bin", "Machine")
```

### On Linux/Mac:
Add to `~/.bashrc` or `~/.zshrc`:
```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64  # Adjust path as needed
export PATH=$JAVA_HOME/bin:$PATH
```

Then reload:
```bash
source ~/.bashrc
```

## Step 3: Verify Installation

```bash
# Check Java version
java -version

# Check Java compiler
javac -version

# Check JAVA_HOME (Linux/Mac)
echo $JAVA_HOME

# Check JAVA_HOME (Windows PowerShell)
echo $env:JAVA_HOME
```

Expected output:
```
java version "25.0.1" 2025-10-21 LTS
Java(TM) SE Runtime Environment (build 25.0.1+8-LTS-27)
Java HotSpot(TM) 64-Bit Server VM (build 25.0.1+8-LTS-27, mixed mode, sharing)
```

## Step 4: Configure Maven for JDK 25

The `pom.xml` in the `jdk25` folder is already configured, but verify:

```xml
<properties>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
</properties>
```

And in the compiler plugin:
```xml
<configuration>
    <release>25</release>
    <compilerArgs>
        <arg>--add-modules</arg>
        <arg>jdk.incubator.vector</arg>
    </compilerArgs>
</configuration>
```

## Step 5: Build the Application

Navigate to the jdk25 folder:
```bash
cd jdk25
```

Clean and build:
```bash
mvn clean package
```

Expected output: `BUILD SUCCESS`

## Step 6: Run the Application

### Test with small dataset:
```bash
cd target
java -jar jdk25_test_v01-jar-with-dependencies.jar ../../datasets/small/in.txt ../../datasets/small/dictionary.txt ../../datasets/small/out.txt
```

### Test with large dataset:
```bash
java -jar jdk25_test_v01-jar-with-dependencies.jar ../../datasets/large/in.txt ../../datasets/large/dictionary.txt ../../datasets/large/out.txt
```

Expected output:
```
[HH:MM:SS] 100.00% complete | Passwords Found: X | Tasks Remaining: 0
Starting attack with X total tasks...
Cracked password details have been written to [output path]
Total passwords found: X
Total hashes computed: X
Total time spent (ms): XXX
```

## Step 7: Verify JDK 25 Features Are Working

The application uses JDK 25 features:
1. **Vector API** (JEP 508) - in `HexVectorEncoder.java`
2. **ScopedValue** (JEP 506) - in `ExecutorProvider.java`

To verify these are working, check that:
- The application compiles without errors
- The application runs successfully
- Performance is good (Vector API should optimize hex encoding)

## Troubleshooting

### Issue: `java: error: release version 25 not supported`
**Solution:** Your Maven is using an older JDK. Check:
```bash
mvn -version
```
Update `JAVA_HOME` to point to JDK 25.

### Issue: `UnsupportedClassVersionError`
**Solution:** The compiled classes were built with a different Java version. Run:
```bash
mvn clean package
```

### Issue: Maven uses wrong JDK
**Solution:** Set `JAVA_HOME` explicitly or configure in Maven's `settings.xml`:
```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>25</version>
    </provides>
    <configuration>
      <jdkHome>C:\Program Files\Java\jdk-25</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

### Issue: Vector API not found
**Solution:** The `--add-modules jdk.incubator.vector` flag should be in `pom.xml` (already included).

## Quick Verification Script

Create `verify_jdk25.sh` (Linux/Mac) or `verify_jdk25.bat` (Windows):

**Windows (`verify_jdk25.bat`):**
```batch
@echo off
echo Checking Java version...
java -version
echo.
echo Checking JAVA_HOME...
echo %JAVA_HOME%
echo.
echo Building application...
cd jdk25
mvn clean package
echo.
echo Running application...
cd target
java -jar jdk25_test_v01-jar-with-dependencies.jar ..\..\datasets\small\in.txt ..\..\datasets\small\dictionary.txt ..\..\datasets\small\out.txt
```

**Linux/Mac (`verify_jdk25.sh`):**
```bash
#!/bin/bash
echo "Checking Java version..."
java -version
echo ""
echo "Checking JAVA_HOME..."
echo $JAVA_HOME
echo ""
echo "Building application..."
cd jdk25
mvn clean package
echo ""
echo "Running application..."
cd target
java -jar jdk25_test_v01-jar-with-dependencies.jar ../../datasets/small/in.txt ../../datasets/small/dictionary.txt ../../datasets/small/out.txt
```

Make executable (Linux/Mac):
```bash
chmod +x verify_jdk25.sh
./verify_jdk25.sh
```

## Summary

Once configured:
- ✅ JDK 25 installed and JAVA_HOME set
- ✅ Application compiles successfully
- ✅ Application runs without errors
- ✅ JDK 25 features (Vector API, ScopedValue) are active
- ✅ Test datasets produce correct output

Your application is ready to use JDK 25 features!

---

## Linux VM Setup (Only Running JAR - No Maven Needed)

If you're just running the JAR file on Linux VM:

### Step 1: Install JDK 25 on Linux VM

**⚠️ If you DON'T have sudo access (like student user):**

Use the **portable installation** method below (no sudo needed):

```bash
# 1. Check current Java version (if any)
java -version

# 2. Create a directory for JDK in your home folder
mkdir -p ~/java

# 3. Download JDK 25 (choose one method):

# Method A: Download from Oracle
cd ~/java
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz

# Method B: If wget doesn't work, download manually from browser:
# Visit: https://www.oracle.com/java/technologies/downloads/#java25
# Download: Linux x64 Compressed Archive
# Then upload to VM using SCP/WinSCP to ~/java/

# 4. Extract JDK
tar -xzf jdk-25_linux-x64_bin.tar.gz
# This creates a folder like: jdk-25.0.1/

# 5. Set JAVA_HOME to your user directory
echo 'export JAVA_HOME=~/java/jdk-25.0.1' >> ~/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc

# 6. Reload bash configuration
source ~/.bashrc

# 7. Verify installation
java -version
javac -version
```

**If you DO have sudo access:**

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install openjdk-25-jdk
```

**Or download from Oracle and install system-wide:**
```bash
wget https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz
tar -xzf jdk-25_linux-x64_bin.tar.gz
sudo mv jdk-25 /opt/java-25
```

### Step 2: Set JAVA_HOME (Already done above if no sudo, or do manually here)

**If you installed in home directory (~/java):**
Already set in Step 1 above.

**If you installed system-wide with sudo:**
Add to `~/.bashrc`:
```bash
export JAVA_HOME=/usr/lib/jvm/java-25-openjdk-amd64  # Or /opt/java-25 if using Oracle JDK
export PATH=$JAVA_HOME/bin:$PATH
```

Reload:
```bash
source ~/.bashrc
```

### Step 3: Transfer JAR File to Linux VM

From Windows, copy the JAR:
```powershell
# Using SCP (if you have OpenSSH on Windows)
scp jdk25\target\jdk25_test_v01-jar-with-dependencies.jar user@vm-ip:/home/user/

# Or use WinSCP, FileZilla, or shared folder
```

### Step 4: Transfer Dataset Files

Copy the datasets folder:
```powershell
scp -r datasets user@vm-ip:/home/user/datasets
```

### Step 5: Run on Linux VM

SSH into Linux VM:
```bash
ssh user@vm-ip
```

Run the application:
```bash
# Make sure JDK 25 is active
java -version

# Run the application
java -jar jdk25_test_v01-jar-with-dependencies.jar datasets/small/in.txt datasets/small/dictionary.txt datasets/small/out.txt
```

### Quick Verification on Linux VM

```bash
# Check Java version
java -version

# Should show: openjdk version "25" or java version "25.0.1"

# Run test
java -jar jdk25_test_v01-jar-with-dependencies.jar datasets/small/in.txt datasets/small/dictionary.txt datasets/small/out.txt
```

**That's it!** No Maven needed on the Linux VM - just JDK 25 and the JAR file.

