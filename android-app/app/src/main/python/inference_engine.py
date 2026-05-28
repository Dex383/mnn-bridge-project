import mnn
import numpy as np
import os
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("MNN_Engine")

# Global cache for MNN interpreters to avoid reloading models on every request
_model_cache = {}

def run_inference(model_path, input_path, output_path):
    """
    Executes inference using MNN with model caching:
    1. Retrieves or loads the .mnn model.
    2. Reads the input binary tensor.
    3. Performs the forward pass on NPU/GPU.
    4. Writes the resulting tensor to a binary file.
    """
    try:
        logger.info(f"Starting inference: Model={model_path}")
        
        # 1. Load or retrieve Model from Cache
        if model_path in _model_cache:
            interpreter = _model_cache[model_path]
        else:
            logger.info(f"Loading model into cache: {model_path}")
            interpreter = mnn.Interpreter(model_path)
            session_config = mnn.SessionConfig()
            # Optimization: Use NPU/GPU if available (default MNN behavior usually handles this, 
            # but session_config can be further tuned here)
            interpreter.createSession(session_config)
            _model_cache[model_path] = interpreter

        # 2. Load Input Tensor
        input_data = np.fromfile(input_path, dtype=np.float32)
        
        # Dynamic Tensor Selection
        graph = interpreter.getCurrentGraph()
        input_tensor = None
        try:
            # Try standard "input" name first
            input_tensor = graph.getVariable("input")
        except Exception:
            try:
                # Fallback: Get the first input tensor available by index
                input_node_name = graph.get_input_node_name(0)
                input_tensor = graph.getVariable(input_node_name)
            except Exception as e:
                return False, f"Could not find a valid input tensor: {str(e)}"
        
        if input_tensor is None:
            return False, "Input tensor is null"
            
        input_tensor.setData(input_data)

        # 3. Execute Forward Pass
        interpreter.run()

        # 4. Extract and Save Output Tensor
        output_tensor = None
        try:
            # Try standard "output" name first
            output_tensor = graph.getVariable("output")
        except Exception:
            try:
                # Fallback: Get the first output tensor available by index
                output_node_name = graph.get_output_node_name(0)
                output_tensor = graph.getVariable(output_node_name)
            except Exception as e:
                return False, f"Could not find a valid output tensor: {str(e)}"
            
        if output_tensor is None:
            return False, "Output tensor is null"

        output_data = output_tensor.getData()
        output_data.tofile(output_path)
        
        logger.info(f"Inference completed successfully. Output saved to {output_path}")
        return True, "Inference successful"

    except Exception as e:
        logger.error(f"Inference failed: {str(e)}")
        return False, str(e)

def clear_cache():
    """Clears the model cache to free memory."""
    global _model_cache
    _model_cache.clear()
    logger.info("Model cache cleared")
