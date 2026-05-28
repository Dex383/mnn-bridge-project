import numpy as np
from mnn_bridge import MNNBridge
import os

# Configuration
MODEL_ALIAS = "test_model"
MODEL_PATH = "/sdcard/models/test.mnn" # Change to a real path on your device
DATA_DIR = "/sdcard/Android/data/com.mnn.bridge/files"

def main():
    # Initialize bridge
    bridge = MNNBridge(data_dir=DATA_DIR)
    
    # 1. Check connection
    if not bridge.check_connection():
        print("❌ Bridge is offline. Please start the Android app.")
        return

    print("✅ Bridge is online!")

    # 2. Register model
    print(f"Registering model {MODEL_ALIAS} from {MODEL_PATH}...")
    success, msg = bridge.register_model(MODEL_ALIAS, MODEL_PATH)
    if not success:
        print(f"❌ Registration failed: {msg}")
        return
    print(f"✅ Model registered: {msg}")

    # 3. Prepare dummy input data (e.g., 1x224x224x3 for ImageNet)
    # In a real scenario, use cv2 or PIL to load an image
    print("Preparing input tensor...")
    input_tensor = np.random.rand(1, 224, 224, 3).astype(np.float32)
    
    # 4. Perform inference
    print(f"Running inference using {MODEL_ALIAS}...")
    output, msg = bridge.infer(MODEL_ALIAS, input_tensor)
    
    if output is not None:
        print(f"✅ Inference successful!")
        print(f"Output shape: {output.shape}")
        print(f"First 5 values: {output[:5]}")
    else:
        print(f"❌ Inference failed: {msg}")

if __name__ == "__main__":
    main()
