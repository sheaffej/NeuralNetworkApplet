package NeuralNet;
import java.util.LinkedList;

/**
 * InputNeuron
 * 
 * This class implements the Input nodes of a Neural Network.
 * The only function of an InputNeuron is to set its input,
 * and access the input as an activation.  This class implements
 * the Neuron interface so Neurons that use its output don't
 * need to differeniate between input neurons and regular neurons.
 * 
 * The registerInputLink() and activate() methods do nothing,
 * since InputNeurons have no feeding Neurons and the activation
 * is simply set by the setInput() method.
 */
public class InputNeuron implements Neuron  {
  private double activation = 0;
  private LinkedList outputLinks;

  public InputNeuron () {
    outputLinks = new LinkedList();
  }

  public void setInput (double input) {
    activation = input;
  }

  public void registerInputLink(NeuralConnection nc) {
    // This is an input neuron, so this method should never be called
    // Just ignore if called
  }

  public void registerOutputLink(NeuralConnection nc) {
    outputLinks.add(nc);
  }

  public double getActivation() {
    return activation;
  }

  public void activate() {
    // This is an input neuron, so this method should never be called
    // Just ignore if called
  }

  public void setOutputNeuron() {
  }

  public boolean isOutputNeuron() {
    return false;
  }
}