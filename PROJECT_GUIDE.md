# 🚀 MNN Bridge: Project Blueprint

## 📌 Overview
**MNN Bridge** is a specialized hardware acceleration system designed for Android devices (specifically optimized for the **Snapdragon 888**). Its primary purpose is to act as a high-performance bridge between a **Termux** environment and the device's **NPU (Neural Processing Unit)** and **GPU (Adreno)**.

Since Termux operates in a restricted sandbox without direct access to low-level hardware drivers, this project implements a native Android Application (APK) that serves as a "Hardware Driver Daemon". This allows Python scripts in Termux to execute AI models with native hardware acceleration instead of relying on the CPU.

---

## 🛠 Architectural Design

### 1. High-Level Topology
`Termux (Python Client)` $\longleftrightarrow$ `Local HTTP API` $\longleftrightarrow$ `Android App (MNN Bridge)` $\longleftrightarrow$ `NPU/GPU (Snapdragon 888)`

### 2. Component Breakdown

#### A. The Android App (The Bridge)
*   **Role:** A headless native Android daemon.
*   **Core Engine:** **MNN (Mobile Neural Network)**. The app utilizes the **MNN Python Library** to interact with the hardware, avoiding manual C++ implementation.
*   **Responsibilities:**
    *   Manage the MNN runtime environment.
    *   Host a lightweight HTTP server (REST API) to receive commands.
    *   Manage model lifecycles (loading, unloading, and aliasing).
    *   Access the NPU/GPU for tensor computation.
    *   Read and write binary tensors to the shared filesystem.
*   **Runtime:** Runs as a Foreground Service to prevent Android from killing the process.

#### B. The Termux Client (The Controller)
*   **Role:** The "brain" of the operation.
*   **Language:** Python.
*   **Core Tool:** A custom Python library (`mnn_bridge`) that abstracts the communication with the APK.
*   **Responsibilities:**
    *   Handle data pre-processing (using standard AI libraries like NumPy, etc.).
    *   Coordinate model loading and inference via the `mnn_bridge` library.
    *   Manage the binary input/output files.
    *   Handle post-processing of the results returned by the APK.

#### C. Data Exchange Layer (The Tunnel)
*   **Control Plane:** HTTP REST requests over `localhost`. Used for commands (e.g., `load_model`, `infer`).
*   **Data Plane:** Shared binary files in the Android app's internal data folder: `/sdcard/Android/data/[package_name]/files/`.
    *   **`input.bin`**: Binary tensor sent from Termux $ightarrow$ read by APK.
    *   **`output.bin`**: Binary tensor written by APK $ightarrow$ read by Termux.

---

## ⚙️ Functional Specifications

### 1. Model Management
*   **Dynamic Loading:** Ability to load `.mnn` models from the device storage.
*   **Aliasing System:** Capability to map a long file path to a short alias (e.g., `/sdcard/models/yolo_v8_int8.mnn` $ightarrow$ `"yolo"`).
*   **Internal Storage:** The APK creates a local copy of registered models in its internal directory to ensure stability and performance.

### 2. Inference Flow
1.  **Pre-process:** Termux prepares a tensor using Python $ightarrow$ saves as `input.bin`.
2.  **Trigger:** Termux sends an HTTP request to the APK: `infer(model_alias, input_file, output_file)`.
3.  **Execute:** APK loads the tensor into the NPU/GPU $ightarrow$ computes $ightarrow$ saves result to `output.bin`.
4.  **Post-process:** Termux reads `output.bin` $ightarrow$ converts to human-readable result.

### 3. Hardware Target
*   **Device:** Qualcomm Snapdragon 888.
*   **Acceleration:** Primary target is the **Hexagon NPU**, with **Adreno GPU (via OpenCL/Vulkan)** as a fallback.

---

## 📋 Development Roadmap

### Phase 1: Connectivity & Pulse
*   Establish the HTTP server in the APK.
*   Implement hardware verification to confirm NPU/GPU access via MNN Python.
*   Create the basic Python `MNNBridge` client in Termux.

### Phase 2: Model Orchestration
*   Implement the `register_model` and `load_model` logic.
*   Create the internal alias database and file copying system.
*   Develop the `unload_model` memory cleanup.

### Phase 3: The Inference Loop
*   Implement the binary file read/write pipeline in the APK.
*   Connect the MNN inference engine to the binary pipeline.
*   Create the Python wrapper for seamless `infer()` calls.

### Phase 4: Optimization & Stability
*   Implement thermal monitoring and hardware status reporting.
*   Optimize tensor transfer speeds.
*   Add error handling for incompatible models or memory overflows.

---

## ⚠️ Technical Constraints & Mandates
*   **No Manual C++:** All interactions with MNN must be done through the **MNN Python Library/API**.
*   **Headless Build:** The APK must be buildable via Gradle CLI and NDK without requiring Android Studio.
*   **Zero-Overhead Data:** Massive tensors must never be sent via HTTP; only via binary files.
*   **Battery Efficiency:** The APK must use the most efficient hardware backend (NPU > GPU > CPU).
