package NeuralNet;

/**
 * The Neuron interface defines the basic methods that all Neurons must
 * have.  The use of this interface allows multiple types of Neurons
 * (Sigmoid, Step, Sign, and Input) to be used in a NeuralNetwork.
 */
public interface Neuron  {
  void registerInputLink(NeuralConnection nc);

  void registerOutputLink(NeuralConnection nc);

  double getActivation();

  void activate();

  void setOutputNeuron();

  boolean isOutputNeuron();
}