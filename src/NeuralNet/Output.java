package NeuralNet;

/**
 * The Output interface defines the basic methods that a Sample must
 * have to be used as an output of the NeuralNetwork.
 */
public interface Output  {
  public abstract double[] getOutput();

  public abstract void setOutput(double[] output);
}