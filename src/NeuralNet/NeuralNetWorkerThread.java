package NeuralNet;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * The NeuralNetWorkerThread performs the network operations once
 * the user presses the "Train and Test Neural Network" button.
 * By placing this work in a separate thread, the GUI thread is free
 * to respond to user actions and is free to repaint the GUI as
 * necessary.
 */
public class NeuralNetWorkerThread extends Thread  {
  private NeuralNetApplet applet;
  private int[] hiddenLayers = null;
  private int inputNodes;
  private int outputNodes;
  private String neuronClassName = "NeuralNet.SigmoidNeuron";
  private int test_pcnt;
  private int numSamples;
  private int numEpochs;
  private Object datasource;

  // Constructor
  public NeuralNetWorkerThread(NeuralNetApplet a, int i, int o, int[] h, int smpls, int e, int tst, Object d) {
    applet = a;
    inputNodes = i;
    outputNodes = o;
    hiddenLayers = h;
    test_pcnt = tst;
    numSamples = smpls;
    numEpochs = e;
    datasource = d;
  }


  public void run () {
    // Load the Neuron class.
    // This was done to allow the NeuralNetwork to be constructed
    // using a variety of different Neuron types.  However, only the
    // SigmoidNeuron is used in this applet.
    Class neuronClass = null;
    try {
      neuronClass = Class.forName(neuronClassName);
    }
    catch (ClassNotFoundException ex) {
      System.out.println(ex.toString());
      System.exit(0);
    }

    // Load the sample data.
    // When the NeuralNetSampleSet is constructed, it reads in the data from
    // the comma-separated data file, normalizes the data values between 0 and 1
    // and loads the data set into memory in a linked-list.
    NeuralNetSampleSet sampleSet = null;
    if (!applet.cancelled()) {
      if (datasource instanceof URL) {
          sampleSet = new NeuralNetSampleSet((URL) datasource, numSamples, applet);
      }
      else if (datasource instanceof File) {
        sampleSet = new NeuralNetSampleSet((File) datasource, numSamples, applet);
      }
      else {
        applet.println("Could not determine datasource type (URL or File)");
      }
    }

    // Extract the test set from the sample set.  The remaining sample
    // set is the training set.
    NeuralNetSampleSet testSet = null;
    if (!applet.cancelled()) {    
      applet.print("Extracting test set " + test_pcnt + "%...");

      testSet = sampleSet.extractTestSet(test_pcnt);

      applet.print("Complete");
      applet.print(" (SampleSet = " + sampleSet.getSize());
      applet.println(", TestSet = " + testSet.getSize() + ")");
    }

    // Construct the network and launch it as a separate thread.
    // Once the network thread is started, it will train and test itself, and
    // return the results to the applet GUI.
    if (!applet.cancelled()) {    
      applet.print("Constructing NeuralNetwork...");
      NeuralNetworkImpl neuralNet = new NeuralNetworkImpl(inputNodes, hiddenLayers, outputNodes, neuronClass, applet);
      applet.println("...Network constructed.");
      neuralNet.setSampleSet(sampleSet);
      neuralNet.setTestSet(testSet);
      neuralNet.setTrainingEpochs(numEpochs);
      applet.print("Starting NeuralNetwork in separate thread...");
      neuralNet.start();
      applet.println("Started");
    }
  } // method run
} // class NeuralNetWorkerThread