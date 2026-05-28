# 🚀 MNN Bridge

MNN Bridge is a high-performance hardware acceleration system designed for Android devices (specifically optimized for the **Snapdragon 888**). It acts as a bridge between a **Termux** environment and the device's **NPU (Neural Processing Unit)** and **GPU (Adreno)**.

## 📌 Overview

Termux operates in a restricted sandbox without direct access to low-level hardware drivers. MNN Bridge solves this by implementing a native Android Application (APK) that serves as a "Hardware Driver Daemon", allowing Python scripts in Termux to execute AI models with native hardware acceleration.

### 🛠 Architecture

`Termux (Python Client)` $\longleftrightarrow$ `Local HTTP API` $\longleftrightarrow$ `Android App (MNN Bridge)` $\longleftrightarrow$ `NPU/GPU (Snapdragon 888)`

- **Android App:** Headless daemon using the MNN Python Library to host a REST API and manage model lifecycles.
- **Termux Client:** Python library (`mnn_bridge`) for pre-processing and coordinating inference.
- **Data Exchange:** HTTP for control, binary files (`input.bin`/`output.bin`) for massive tensor data.

## ⚙️ Features

- [ ] **Connectivity:** HTTP server in APK and basic Python client.
- [ ] **Model Orchestration:** Dynamic loading, aliasing system, and internal storage management.
- [ ] **Inference Loop:** Optimized binary file pipeline for NPU/GPU execution.
- [ ] **Optimization:** Thermal monitoring and hardware backend prioritization (NPU > GPU > CPU).

## 🚀 Getting Started

*(Instructions will be added as the project develops)*

## ⚖️ License

This project is provided under a **Non-Commercial License**. You are free to use, modify, and distribute the code for personal, educational, or research purposes, but **commercial use is strictly prohibited**. See `LICENSE` for full details.
