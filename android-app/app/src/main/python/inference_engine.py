import mnn
import numpy as np
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("MNN_Engine")

def run_inference(model_path, input_path, output_path):
    """
    Executes inference using MNN:
    1. Loads the .mnn model.
    2. Reads the input binary tensor.
    3. Performs the forward pass on NPU/GPU.
    4. Writes the resulting tensor to a binary file.
    """
    try:
        logger.info(f"Starting inference: Model={model_path}")
        
        # 1. Load Model
        # MNN.Interpreter is the core class for inference
        interpreter = mnn.Interpreter(model_path)
        session_config = mnn.SessionConfig()
        
        # Prioritize NPU (Hexagon) > GPU (OpenCL/Vulkan) > CPU
        # In a real scenario, we would detect the device, but MNN handles 
        # the backend selection based on the available plugins.
        interpreter.createSession(session_config)

        # 2. Load Input Tensor
        # We read the binary file as a numpy array (float32 is standard for MNN)
        input_data = np.fromfile(input_path, dtype=np.float32)
        
        # Get the input tensor from the interpreter
        input_tensor = interpreter.getCurrentGraph().getVariable("input") # Default name 'input'
        input_tensor.setData(input_data)

        # 3. Execute Forward Pass
        interpreter.run()

        # 4. Extract and Save Output Tensor
        output_tensor = interpreter.getCurrentGraph().getVariable("output") # Default name 'output'
        output_data = output_tensor.getData()
        
        # Save result as binary file
        output_data.tofile(output_path)
        
        logger.info(f"Inference completed successfully. Output saved to {output_path}")
        return True, "Inference successful"

    except Exception as e:
        logger.error(f"Inference failed: {str(e)}")
        return False, str(e)
