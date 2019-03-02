package NeuralNet;
import java.util.Random;

/**
 * The NeuralNetworkImpl class implements a Neural Network.  It runs
 * as a separate thread to Train and Test the network, returning
 * the Training RMS error and Test percent correct statitics.
 * The NeuralNetworkImpl allows any number neurons and hidden layers. 
 */
public class NeuralNetworkImpl extends Thread  {
  private InputNeuron[] inputLayer;
  private Neuron[] outputLayer;
  private Neuron[][] hiddenLayer;
  private int numHiddenLayers;
  private NeuralNetApplet applet;
  private NeuralNetSampleSet sampleset = null;
  private NeuralNetSampleSet testset = null;
  private int numEpochs = 1;

  // Constructor
  public NeuralNetworkImpl( int inputNodeNum, int[] hiddenLayerNodeNum,
                            int outputNodeNum, Class nodeClass,
                            NeuralNetApplet app) {
    applet = app;
    buildNetwork(inputNodeNum, hiddenLayerNodeNum, outputNodeNum, nodeClass);
  }


  public void run() {
    double[] rms_errors = null;

    // Train the network
    if (!applet.cancelled()) {
      applet.print("\nTraining Network...");
      rms_errors = train(sampleset, numEpochs);
    }
    if (!applet.cancelled()) { 
      applet.println("Training error (RMS): " + rms_errors[rms_errors.length-1]);
      applet.setTrainingError(rms_errors[rms_errors.length-1]);
    }

    // Test the network
    if (!applet.cancelled()) {
      applet.print("Testing the network...");
      double percentCorrect = test();
      applet.println("Percent Correct: " + percentCorrect);
      applet.setPercentCorrect(percentCorrect);
    }
    applet.endRun();
    applet.println("Operation Complete...Elapsed time: " + applet.stopTimer() + " milliseconds\n");
  } // method run


  // Called by the constructor, this method builds the NeuralNetwork
  // using the configuration parameter supplied.
  // The connection between neurons is represented by a NeuralConnection
  // object.  The NeuralConnection object simply stores a reference to
  // both Neurons it connects.
  private void buildNetwork(int inputNodeNum, int[] hiddenLayerNodeNum,
                            int outputNodeNum, Class nodeClass) {

    // Create an array of references to the input neurons
    inputLayer = new InputNeuron[inputNodeNum];

    // Create a 2-dimensional array of references to the hidden layer neurons
    numHiddenLayers = hiddenLayerNodeNum.length;
    hiddenLayer = new Neuron[numHiddenLayers][];
    for (int i=0; i < numHiddenLayers; i++) {
      hiddenLayer[i] = new Neuron[hiddenLayerNodeNum[i]];
    }

    // Create an array of references to the output neurons
    outputLayer = new Neuron[outputNodeNum];

    // Build the input neurons
    for (int i=0; i < inputLayer.length; i++) {
      inputLayer[i] = new InputNeuron();
    }

    // Build the hidden layer neurons
    for (int layer=0; layer < numHiddenLayers; layer++) {
      for (int node=0; node < hiddenLayer[layer].length; node++) {
        try {
          hiddenLayer[layer][node] = (Neuron)nodeClass.newInstance();
        }
        catch (Exception ex) {
          System.out.println(ex.toString());
          System.exit(0);
        }
      }
    }

    // Build the output neurons
    for (int i=0; i < outputLayer.length; i++) {
      try {
        outputLayer[i] = (Neuron)nodeClass.newInstance();
        outputLayer[i].setOutputNeuron();
      }
      catch (Exception ex) {
        System.out.println(ex.toString());
        System.exit(0);
      }
    }

    // Declare a Random object to generate the random connection weights
    Random rand = new Random();
    double weight = 0.0;

    // Connect the input neurons to the first hidden layer
    for (int inode=0; inode < inputLayer.length; inode++) {
      for (int hnode=0; hnode < hiddenLayer[0].length; hnode++) {
        // For each input node, register each hidden layer node
        // in the first hidden layer
        new NeuralConnection(inputLayer[inode], hiddenLayer[0][hnode], rand);
      }
    }

    // Connect the hidden layer neurons to other hidden layers (if any)
      for (int layer=0; layer < numHiddenLayers-1; layer++) {
        for (int outnode=0; outnode < hiddenLayer[layer].length; outnode++) {
          for (int innode=0; innode < hiddenLayer[layer+1].length; innode++) {
            // For each hidden layer that is connected to another hidden layer
            // connect the one layer (innode) to the next layer (outnode)
            new NeuralConnection(hiddenLayer[layer][outnode], hiddenLayer[layer+1][innode], rand);
          }
        }
      }

    // Connect the last hidden layer to the output neurons
    for (int hnode=0; hnode < hiddenLayer[numHiddenLayers-1].length; hnode++) {
      for (int onode=0; onode < outputLayer.length; onode++) {
        // For each hidden layer node in the last hidden layer, register 
        // each output node      
        new NeuralConnection(hiddenLayer[numHiddenLayers-1][hnode], outputLayer[onode], rand);
      }
    }
  } // method buildNetwork


  // This method executes the network and returns the Output object that
  // results.
  // The Output object is simply an array of doubles, each double as the
  // output of a single output neuron.
  // In this applet, there is only one output neuron, so this is a bit overkill
  // but it makes the NeuralNetwork reusable.
  public Output execute(Input input) {
    // Check input for the proper number of input values
    double[] inValues = input.getInput();
    if (inValues.length != inputLayer.length) {
      applet.println("Incorrect number of input values");
      System.exit(0);
    }

    // Set the input
    for (int i=0; i < inValues.length; i++) {
      inputLayer[i].setInput(inValues[i]);
    }

    // Activate the hidden layers
    for (int layer=0; layer < numHiddenLayers; layer++) {
      for (int node=0; node < hiddenLayer[layer].length; node++) {
        hiddenLayer[layer][node].activate();
      }
    }
      
    // Activate the output layer
    for (int node=0; node < outputLayer.length; node++) {
      outputLayer[node].activate();
    }

    // Retrieve the output values
    double[] outValues = new double[outputLayer.length];
    for (int node=0; node < outputLayer.length; node++) {
      outValues[node] = outputLayer[node].getActivation();
    }

    // Return the output as an NeuralNet.Output object
    Output theOutput = new NeuralNetSample();
    theOutput.setOutput(outValues);
    return theOutput;
  } // method execute
  

  // This method trains the network using the Back-Propogation algorithm.
  // Training occurs by executing the network for a single sample, and
  // adjusting the connection weights based on the difference between the
  // expected and actual output.
  public double[] train(NeuralNetSampleSet sampleSet, int epochs) {
    double lr_interval = 1.0 / epochs;
    double[] rms_errors = new double[epochs];

    // Initialize the progress bars (Training and Epoch)
    applet.setProgressMax(epochs);
    applet.setEpochProgressMax(sampleSet.getSize());
    applet.setProgress(0);

    // For each Epoch train for all the samples in the trainig set
    for (int epoch=0; epoch < epochs; epoch++) {
      double learning_rate = 1.0 - lr_interval * epoch;
      double square_sum = 0.0;

      // Reset the Epoch progress bar since a new Epoch is starting
      applet.setEpochProgress(0);

      // For each example, execute the network and back-propogate the error
      for (int smpl=0; smpl < sampleSet.getSize(); smpl++) {
        // Get a sample from the training set
        Sample aSample = sampleSet.get(smpl);
      
        // Execute the network for the sample
        Output output = execute(aSample);

        // Compute the "Error" difference = Target output - Observed output
        double[] target = aSample.getOutput();
        double[] observed = output.getOutput();
        double[] error = new double[target.length];
        for (int i=0; i < error.length; i++) {
          error[i] = target[i] - observed[i];
        }

        // Store the error in the output neuron
        // and adjust the output neuron's input weights
        for (int onode=0; onode < outputLayer.length; onode++) {
          ((SigmoidNeuron)outputLayer[onode]).setDeltaI(error[onode]);
          ((SigmoidNeuron)outputLayer[onode]).backPropogateError(learning_rate);
        }

        // For each hidden layer in network propogate the error
        // and adjust the input weights
        for (int layer=hiddenLayer.length-1; layer >= 0; layer--){
         for (int hnode=0; hnode < hiddenLayer[layer].length; hnode++) {
           ((SigmoidNeuron)hiddenLayer[layer][hnode]).backPropogateError(learning_rate);
         }
        }

        // Sum the errors from each output neuron, square the sum, and add
        // the result to a running total of squared error for the epoch
        double error_sum = 0.0;
        for (int i=0; i < error.length; i++) {
          error_sum += error[i];
        }
        square_sum += Math.pow(error_sum, 2);

        // Update epoch progress
        applet.setEpochProgress(smpl+1);

        // Check to see if the user cancelled the run after every 100 samples
        if (smpl % 100 == 0) {
          if (applet.cancelled()) {
            applet.printCancelled();
            return null;
          }
        }        
      } // for each example

      // Compute the Root-Mean-Square for the epoch
      rms_errors[epoch] = Math.sqrt(square_sum / sampleSet.getSize());

      // Update training progress
      applet.setProgress(epoch+1);
      
    } // for each epoch
    
    return rms_errors;
  } // method train


  // This method tests the network.
  // It simply executes the network for each sample in the Test set
  // and records how many samples yeild the expected results.
  // The result is returned as a percentage of correct executions
  // using a tolerance of 0.1 (each output has the range of 0.0 to 1.0)
  public double test () {
    int correct = 0;
    for (int smpl=0; smpl < testset.getSize(); smpl++) {

      // Retreive the sample from the set
      Sample aSample = testset.get(smpl);

      // Execute the network on the sample
      Output output = execute(aSample);

      // Test output for correctness
      double[] actual = output.getOutput();
      double[] expected = aSample.getOutput();
      if (Math.abs(expected[0] - actual[0]) < 0.1) {
        correct++;
      }      
    }

    // Return the percentage of correct executions
    return correct / (double)testset.getSize();
  } // method test


  // Associates the network with a Training set
  public void setSampleSet(NeuralNetSampleSet s) {
    sampleset = s;
  }

  // Associates the network with a Test set
  public void setTestSet(NeuralNetSampleSet t) {
    testset = t;
  }


  // Set the number of training epochs to use
  public void setTrainingEpochs (int e) {
    numEpochs = e;
  }

} // class NeuralNetworkImpl