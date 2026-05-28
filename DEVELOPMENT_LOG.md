# 📜 MNN Bridge Development Log

This document serves as the authoritative physical record of all changes, architectural decisions, and debugging milestones for the MNN Bridge project. It is designed to ensure project continuity and facilitate hand-offs between different AI agents or developers.

## 🛠 Project Core Mandates
- **Hardware Target**: Qualcomm Snapdragon 888 (NPU Hexagon / GPU Adreno).
- **No Manual C++**: All MNN interactions must use the MNN Python API.
- **Architecture**: Termux (Python) $\longleftrightarrow$ HTTP $\longleftrightarrow$ Android App (daemon) $\longleftrightarrow$ Hardware.
- **Licensing**: Non-Commercial (Source Available).

---

## 📈 Change History

### Phase 0: Infrastructure & Governance
- **Repository Setup**: Initialized Git repository and connected to GitHub (`Dex383/mnn-bridge-project`).
- **Governance**: 
    - Implemented `.gitignore` for Android and Python.
    - Created `LICENSE` (Non-Commercial).
    - Created `README.md` and `PROJECT_GUIDE.md`.
    - Setup `.env.example` for configuration.
- **Environment**: Created Python virtual environment in `python-client/` with `numpy` and `requests`.
- **Android Base**: Configured `build.gradle` and `settings.gradle` using **Chaquopy** to enable Python runtime within the APK. Target SDK 34, min SDK 26, and `arm64-v8a` ABI filters.

### Phase 1: Connectivity & Pulse
- **Android Server**:
    - Implemented `MNNBridgeServer` using `NanoHTTPD` for a lightweight REST API.
    - Created `/status` endpoint returning system health and hardware info.
- **Daemon Logic**:
    - Implemented `MNNBridgeService` as a **Foreground Service** to prevent Android from killing the process.
    - Added persistent notification for service visibility.
- **Python Client**:
    - Created `mnn_bridge/bridge.py` with a `MNNBridge` class.
    - Implemented `check_connection()` method to verify the pulse of the Android bridge.

### Debugging & Stability Passes (Combined)
- **Syntax & Basic Errors**: Fixed JSON escaping in Java, removed Java syntax from Python files, and added missing Android layout files (`activity_main.xml`).
- **Android 14 Compatibility**: Added `android:foregroundServiceType="special"` to the manifest to comply with API 34 requirements.
- **Network Robustness**:
    - Implemented `stopServer()` in the service to prevent "Address already in use" errors during restarts.
    - Updated Python client to use `requests.Session()` for TCP connection reuse (Keep-Alive).
    - Added `json.JSONDecodeError` handling in the Python client.
- **Logging**: Enhanced server logs to include the remote client's IP address.
- **Resource Management**: Created `themes.xml` and updated the manifest to prevent resource-not-found crashes.

### Phase 3: The Inference Loop & Deep Debugging
- **Python Client Inference**:
    - Implemented `MNNBridge.infer()` method in the Python client.
    - Integrated binary data plane handling (saving/reading `input.bin` and `output.bin`).
- **Race Condition Fix**:
    - Implemented a synchronization mutex in `MNNBridgeServer.java` for the `/infer` endpoint to prevent concurrent binary file corruption.
- **Performance Optimization**:
    - Implemented a global model cache in `inference_engine.py` to avoid reloading the MNN interpreter and session on every request.
- **Storage Optimization**:
    - Updated `ModelManager.java` to avoid redundant internal copies if the model already exists and has the same size.
    - Implemented automatic deletion of internal `.mnn` files when a model is unloaded via `/unload`.
- **Integration Testing**:
    - Added `python-client/examples/inference_test.py` to verify the end-to-end inference flow.

---

## 🚀 Current State
- **Connectivity**: Fully operational and debugged.
- **Model Orchestration**: Fully operational with internal storage cleanup.
- **Inference Loop**: Fully operational, synchronized, and optimized.
- **Next Step**: Phase 4 (Optimization & Stability), focusing on thermal monitoring, hardware backend prioritization (NPU > GPU > CPU), and advanced error handling.

## ⚠️ Known Technical Hurdles
- **MNN Binaries**: The project relies on the MNN Python library. If the standard `pip` install fails on Android, a local `.whl` binary for `arm64-v8a` must be placed in `app/libs/`.
