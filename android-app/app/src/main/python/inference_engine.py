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
        interpreter = mnn.Interpreter(model_path)
        session_config = mnn.SessionConfig()
        interpreter.createSession(session_config)

        # 2. Load Input Tensor
        input_data = np.fromfile(input_path, dtype=np.float32)
        
        # Dynamic Tensor Selection: Try default names, then fallback to index 0
        graph = interpreter.getCurrentGraph()
        input_tensor = None
        try:
            input_tensor = graph.getVariable("input")
        except:
            # Fallback: Get the first input tensor available
            input_tensor = graph.getVariable(graph.get_input_node_name(0))
        
        if input_tensor is None:
            return False, "Could not find a valid input tensor in the model"
            
        input_tensor.setData(input_data)

        # 3. Execute Forward Pass
        interpreter.run()

        # 4. Extract and Save Output Tensor
        output_tensor = None
        try:
            output_tensor = graph.getVariable("output")
        except:
            # Fallback: Get the first output tensor available
            output_tensor = graph.getVariable(graph.get_output_node_name(0))
            
        if output_tensor is None:
            return False, "Could not find a valid output tensor in the model"

        output_data = output_tensor.getData()
        output_data.tofile(output_path)
        
        logger.info(f"Inference completed successfully. Output saved to {output_path}")
        return True, "Inference successful"

    except Exception as e:
        logger.error(f"Inference failed: {str(e)}")
        return False, str(e)
