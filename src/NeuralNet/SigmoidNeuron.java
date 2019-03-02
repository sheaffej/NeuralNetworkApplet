package NeuralNet;
import java.util.LinkedList;

/**
 * The SigmoidNeuron class implements a Sigmoid Neuron node in a
 * Neural Network.  A Sigmoid Neuron uses a Sigmoid function as its
 * activation function.  Since a Sigmoid Function has an obtainable
 * derivative, the SigmoidNeuron can be used in the Back-Propogation
 * training algorithm.
 */
public class SigmoidNeuron implements Neuron  {
  protected double activation;
  protected double activation_gradient;
  protected double deltaI = 0.0;
  protected LinkedList inputLinks;
  protected LinkedList outputLinks;
  protected boolean outputNeuron = false;

  public SigmoidNeuron() {
    activation = 0;
    inputLinks = new LinkedList();
    outputLinks = new LinkedList();
  }

  public void activate() {
    activation = 0.0;

    // Sum the weighted inputs.  A "weighted input" is an input neuron's
    // activation value muliplied by the weight of the link.
    // Note: The first link is actually the representation of the Threshold
    for (int i = 0; i < inputLinks.size(); i++) {
      NeuralConnection nc = (NeuralConnection)inputLinks.get(i);
      double input = nc.getInputNeuron().getActivation();
      double weight = nc.getWeight();
      activation += input * weight;
    }

      // The Sigmoid function
      activation = 1.0 / (1.0 + Math.pow(Math.E, (-1.0 * activation)));

      // Compute the activation gradient if training
      // to speed back propogation
      activation_gradient = activation * (1.0 - activation);
  } // method activate


  public void backPropogateError(double learningRate) {
    double weightedDeltaI = 0.0;

    // If the Neuron is not an output neuron, compute the Delta I.
    // Output Neuron's get have their Delta I computed when the error is set.
    if (!outputNeuron) {
    
      // Gather weighted Delta I's from all Neurons connected to this
      // neuron's output
      for (int out=0; out < outputLinks.size(); out++) {
        NeuralConnection nc = (NeuralConnection)(outputLinks.get(out));
        double aWeight = nc.getWeight();
        double aDeltaI = ((SigmoidNeuron)nc.getOutputNeuron()).getDeltaI();
        weightedDeltaI += aWeight * aDeltaI;
      }      
      // Compute this neuron's Delta I
      deltaI = activation_gradient * weightedDeltaI;
    }

    // Update input weights
    for (int in=0; in < inputLinks.size(); in++) {
      NeuralConnection nc = (NeuralConnection)(inputLinks.get(in));
      double wght = nc.getWeight();
      double actv = nc.getInputNeuron().getActivation();
      nc.setWeight(wght + learningRate * actv * deltaI);
    }
  } // method backPropogateError


  // Given the error on a Output Neuron, compute the Delta I
  public void setDeltaI(double error) {
    deltaI = error * activation_gradient;
  }

  //////////////////////
  // Accessor methods //
  //////////////////////
  
  public double getActivation() {
    return activation;
  }

  public void registerInputLink(NeuralConnection nc) {
    inputLinks.add(nc);
  }

  public void registerOutputLink(NeuralConnection nc) {
    outputLinks.add(nc);
  }

  public double getDeltaI() {
    return deltaI;
  }

  public void setOutputNeuron() {
    outputNeuron = true;
  }

  public boolean isOutputNeuron() {
    return outputNeuron;
  }
} // class SigmoidNeuron