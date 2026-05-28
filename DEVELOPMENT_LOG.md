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

### Phase 2: Model Orchestration (In Progress)
- **Model Management**:
    - Implemented `ModelManager.java` to handle model aliases.
    - **Internal Storage Strategy**: Added logic to copy `.mnn` models from shared storage to the app's internal private directory for stability and performance.
    - **Persistence**: Alias mappings are persisted in `model_aliases.json`.
- **API Extension**:
    - Added `/register` endpoint to the server to allow dynamic model registration from the client.
- **Storage Permissions (Critical Fix)**:
    - Integrated `MANAGE_EXTERNAL_STORAGE` permission to bypass Android's Scoped Storage restrictions.
    - Implemented an automatic redirect in `MainActivity` to the system settings page if the permission is missing.

---

## 🚀 Current State
- ** connectivity**: Fully operational and debugged.
- **Model Orchestration**: Registration and Internal Copying implemented.
- **Next Step**: Implement `register_model` in the Python client and move towards Phase 3 (Inference).

## ⚠️ Known Technical Hurdles
- **MNN Binaries**: The project relies on the MNN Python library. If the standard `pip` install fails on Android, a local `.whl` binary for `arm64-v8a` must be placed in `app/libs/`.
