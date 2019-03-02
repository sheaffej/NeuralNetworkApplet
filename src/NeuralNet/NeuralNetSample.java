package NeuralNet;
import java.io.Serializable;

/**
 * The NeuralNetSample class encapsulates the input elements
 * and the output elements.  It therefore can be use as a source of both 
 * input and output values.  It is also the only implementation
 * of the Output interface (through the Sample interface) and
 * therefore is used to pass output values back from the Network.
 */
public class NeuralNetSample implements Sample, Serializable {
  protected double[] input;
  protected double[] output;

  public void setOutput(double[] output) {
    this.output = (double[])output.clone();
  }

  public double[] getOutput() {
    return (double[])output.clone();
  }

  public void setInput(double[] input) {
    this.input = (double[])input.clone();
  }

  public double[] getInput() {
    return (double[])input.clone();
  }

}