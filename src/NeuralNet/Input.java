package NeuralNet;

/**
 * The Input Interface defines the basic methods that a Sample must 
 * have to be used as an input to the Neural Network.
 */
public interface Input  {
  public abstract double[] getInput();

  public abstract void setInput(double[] input);
}