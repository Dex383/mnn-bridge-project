import requests
import json
import logging
import os

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("MNNBridge")

class MNNBridge:
    def __init__(self, host=None, port=None):
        # Use provided values or fallback to environment variables, then to defaults
        self.host = host or os.getenv("BRIDGE_HOST", "127.0.0.1")
        self.port = port or int(os.getenv("BRIDGE_PORT", 8080))
        self.base_url = f"http://{self.host}:{self.port}"
        logger.info(f"Initialized MNNBridge client connecting to {self.base_url}")

    def check_connection(self):
        """Verify if the Android Bridge is online."""
        try:
            response = requests.get(f"{self.base_url}/status", timeout=5)
            if response.status_code == 200:
                data = response.json()
                logger.info(f"Bridge Status: ONLINE | Hardware: {data.get('hardware')}")
                return True
            else:
                logger.warning(f"Bridge returned error: {response.status_code}")
                return False
        except requests.exceptions.RequestException as e:
            logger.error(f"Connection failed: {e}")
            return False

if __name__ == "__main__":
    # Simple test pulse
    bridge = MNNBridge()
    if bridge.check_connection():
        print("✅ Pulse detected: MNN Bridge is online!")
    else:
        print("❌ Pulse failed: Bridge is offline or unreachable.")
