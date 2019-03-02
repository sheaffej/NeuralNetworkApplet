package NeuralNet;
import java.util.Random;

/**
 * The NeuralConnection class represents a connection between
 * two Neurons.  The class simply stores a reference to the
 * connected Neurons and provides accessor methods so each
 * Neuron can retrieve a reference to the other Neuron.
 */
public class NeuralConnection  {
  static int index = 0;

  protected double weight;
  // The input/output neurons are from the point of view of the 
  // neuron using the connection
  protected Neuron inputNeuron;  // The Neuron sending the activation
  protected Neuron outputNeuron; // The Neuron receiving the activation

  // Constructor
  public NeuralConnection (Neuron in, Neuron out, Random rand) {
    inputNeuron = in;
    outputNeuron = out;

    // Register this connection to both Neurons.
    // Registering simply tells the Neruon to add this NeuralConnection
    // to the Neuron's internal list of Input or Output connections
    inputNeuron.registerOutputLink(this);
    outputNeuron.registerInputLink(this);

    // Generate a random initial weight (0.1 to 1.0) for this connection
    weight = (rand.nextInt(99) + 1) / 100.0;
  }

  public Neuron getInputNeuron() {
    return inputNeuron;
  }

  public Neuron getOutputNeuron() {
    return outputNeuron;
  }

  public double getWeight() {
    return weight;
  }

  public void setWeight(double newWeight) {
    weight = newWeight;
  }
}