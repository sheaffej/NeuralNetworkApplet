package NeuralNet;
import java.util.LinkedList;
import java.io.FileReader;
import java.io.File;
import java.io.BufferedReader;
import java.util.StringTokenizer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.io.Serializable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.zip.ZipEntry;

/**
 * The NeuralNetSampleSet class is simply a linked list of
 * NeuralNetSample objects.  This class also contains the logic
 * to load the samples from the data file, and randomly extract a
 * portion of the samples into a separate NeuralNetSampleSet.
 */
public class NeuralNetSampleSet implements Serializable  {
  protected LinkedList set;
  protected NeuralNetApplet applet;

  // Constructor - no arguments
  public NeuralNetSampleSet () {
    set = new LinkedList();
  }

  // Constructor for use with a datafile on the local filesystem
  public NeuralNetSampleSet (File datafile, int maxsamples, NeuralNetApplet a) {
    set = new LinkedList();
    applet = a;
    if (datafile.canRead()) {
      try {
        // Normalize and Load the data into memory
        loadCSVDataFromFile (datafile.toURL(), maxsamples);
      }
      catch (MalformedURLException e) {
        applet.println(e.toString());
      }
    }
    else {
      applet.println("Can't read local file: " + datafile.toString());
    }
  }

  // Constructor for use with a datafile on an HTTP server
  public NeuralNetSampleSet (URL url, int maxsamples, NeuralNetApplet a) {
    set = new LinkedList();
    applet = a;
    // Normalize and Load the data into memory
    loadCSVDataFromFile (url, maxsamples);    
  }


  // Return the Sample at the specifice index
  public Sample get(int index) {
    return (Sample)(set.get(index));
  }

  // Return the number of Samples in this set
  public int getSize() {
    return set.size();
  }

  // Empty the set so that it contains zero Samples
  public void clear() {
    set.clear();
  }

  // Add a Sample to the set
  public void add(Sample sample) {
    set.add(sample);
  }

  // Loads the data into memory, in a normalized state.
  // The data file is roughly 15 MB uncompressed, and 371 KB compressed.
  // Therefore, the data file is stored compressed and passed through a
  // ZipInputStream to inflate the data for processing.
  // First the data is read through to determine the maximum values of each
  // attribute, then the data is read through loading it into memory while 
  // translating each value into a normalized value between 0.0 and 1.0.
  private void loadCSVDataFromFile (URL url, int maxsamples) {

    // Define some variables that will be used in this method
    StringTokenizer tokenizer = null;
    String dataline = null;
    String[] inputarray = null;
    String[] outputarray = null;
    double[] normalized_input = null;
    double[] normalized_output = null;
    int sampleSize = maxsamples;
    LinkedList protocol_type = new LinkedList();
    LinkedList service = new LinkedList();
    LinkedList flag = new LinkedList();
    LinkedList attack_type = new LinkedList();
    String[] data_names = {"duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes", 
                          "land", "wrong_fragment", "urgent", "hot", "num_failed_logins",
                          "logged_in", "num_compromised", "root_shell", "su_attempted",
                          "num_root", "num_file_creations", "num_shells", "num_access_files",
                          "num_outbound_cmds", "is_host_login", "is_guest_login", "count",
                          "srv_count", "serror_rate", "srv_serror_rate", "rerror_rate",
                          "srv_rerror_rate", "same_srv_rate", "diff_srv_rate",
                          "srv_diff_host_rate", "dst_host_count", "dst_host_srv_count",
                          "dst_host_same_srv_rate", "dst_host_diff_srv_rate",
                          "dst_host_same_src_port_rate", "dst_host_srv_diff_host_rate",
                          "dst_host_serror_rate", "dst_host_srv_serror_rate",
                          "dst_host_rerror_rate", "dst_host_srv_rerror_rate",
                          "attack_type"};
    boolean[] double_data = {true, false, false, false, true, true, true, true,
                            true, true, true, true, true, true, true, true,
                            true, true, true, true, true, true, true, true,
                            true, true, true, true, true, true, true, true,
                            true, true, true, true, true, true, true, true,
                            true, false};

    // Build an array to store the maximum values
    double[] maximums = new double[data_names.length];
    for (int i=0; i < maximums.length; i++) {
      maximums[i] = 0.0;
    }

    // Pass the input stream through the Zip inflator and into a BufferedReader
    BufferedReader bufferedReader = null;
    try {
      ZipInputStream zis = new ZipInputStream(url.openStream());
      ZipEntry entry = zis.getNextEntry();
      bufferedReader =
        new BufferedReader(new InputStreamReader(zis));
    }
    catch (IOException e) {
      applet.println("Exception creating data input stream.");
    }

    // Set progress bar to reflect two passes through the data
    applet.setDataProgressMax(sampleSize * 2);

    // Pass through data to determine the maximums to use when normalizing
    // the data.
    applet.print("Passing through sample data to determine maximums...");
    try {
      int count = 0;
      while ((count < sampleSize) && ((dataline = bufferedReader.readLine()) != null))  {

        // Build a String array for a single record
        tokenizer = new StringTokenizer(dataline, ",");
        int inputelements = tokenizer.countTokens();
        if (inputelements != 42) {
          applet.println("Input does not have 42 elements.");
          System.exit(1);
        }
        inputarray = new String[inputelements];
        for (int i=0; i < inputarray.length; i++) {
          inputarray[i] = tokenizer.nextToken();
        }

        // For each attribute in the record (element in the array),
        // determine the maximum or add the discrete value to a list
        for (int i=0; i < inputarray.length; i++) {
          // If the element is a number (continous) value
          if (double_data[i]) {
            if (Double.parseDouble(inputarray[i]) > maximums[i]) {
              maximums[i] = Double.parseDouble(inputarray[i]);
            }
          }
          // If the element is a discrete value (e.g. FTP, HTTP, etc)
          else {
            if (i == 1) {
              if (!protocol_type.contains(inputarray[i])) {
                protocol_type.add(inputarray[i]);
              }
            }
            if (i == 2) {
              if (!service.contains(inputarray[i])) {
                service.add(inputarray[i]);
              }
            }
            if (i == 3) {
              if (!flag.contains(inputarray[i])) {
                flag.add(inputarray[i]);
              }
            }
            if (i == inputarray.length-1) {
              if (!attack_type.contains(inputarray[i])) {
                attack_type.add(inputarray[i]);
              }
            }
          }
        }
        count++;

        // Check to see if the user cancelled the run
        if (count % 100 == 0) {
          if (applet.cancelled()) {
            applet.printCancelled();
            return;
          }
        }

        // Update the progress bar
        applet.setDataProgress(count);
      } // while

      // Close the input stream
      bufferedReader.close();
      applet.println("Complete");
    }
    catch (IOException e) {
      applet.println("IOException: ");
      e.printStackTrace();
      System.exit(1);
    }

    // Pass through data again, loading the set as normalized data
    applet.print("Loading data and normalizing...");
    // Pass the input stream through the Zip inflator and into a BufferedReader
    try {
      ZipInputStream zis = new ZipInputStream(url.openStream());
      ZipEntry entry = zis.getNextEntry();
      bufferedReader =
        new BufferedReader(new InputStreamReader(zis));
    }
    catch (IOException e) {
      applet.println("Exception creating data input stream.");
    }

    try {
      int count = 0;
      while ((set.size() < sampleSize) && ((dataline = bufferedReader.readLine()) != null)) {

        // Build a String array from the record
        tokenizer = new StringTokenizer(dataline, ",");
        int inputelements = tokenizer.countTokens();
        if (inputelements != 42) {
          applet.println("Input does not have 42 elements on second pass.");
          System.exit(1);
        }
        inputarray = new String[inputelements];
        for (int i=0; i < inputarray.length; i++) {
          inputarray[i] = tokenizer.nextToken();
        }

        // Using the String array, load the data as a normalized double array
        normalized_input = new double[inputelements-1];
        normalized_output = new double[1];
        for (int i=0; i < inputarray.length; i++) {
          // If the number is a number (continuous) value
          if (double_data[i]) {
            if (maximums[i] != 0.0) {
              normalized_input[i] = Double.parseDouble(inputarray[i]) / maximums[i];
            }
            else {
              normalized_input[i] = 0.0;
            }
          }
          // If the number is a discrete value (e.g. FTP, HTTP, etc.)
          else {
            if (i == 1) {
              normalized_input[i] = protocol_type.indexOf(inputarray[i]) / (double)protocol_type.size();
            }
            if (i == 2) {
              normalized_input[i] = service.indexOf(inputarray[i]) / (double)service.size();
            }
            if (i == 3) {
              normalized_input[i] = flag.indexOf(inputarray[i]) / (double)flag.size();
            }
            if (i == inputarray.length-1) {
              if (inputarray[i].equals("normal.")) {
                normalized_output[0] = 0.0;
              }
              else {
                normalized_output[0] = 1.0;
              }
            }
          }
        }

        // Load the resulting data arrays into a NeuralNetSample
        // and add it to the Set.
        NeuralNetSample asample = new NeuralNetSample();
        asample.setInput(normalized_input);
        asample.setOutput(normalized_output);
        set.add(asample);

        count++;

        // Check to see if the user cancelled the run
        if (count % 100 == 0) {
          if (applet.cancelled()) {
            applet.printCancelled();
            return;
          }
        }

        // Update the progress bar
        applet.setDataProgress(count + sampleSize);
      } // while

      // Close the input stream
      bufferedReader.close();
      applet.println("Complete");
    }
    catch (IOException e) {
      applet.println("IOException: ");
      e.printStackTrace();
      System.exit(1);
    }
  } // method loadCSVDataFromFile


  // Called by the worker thread to extract a Test set from this sample set
  public NeuralNetSampleSet extractTestSet(int percent) {
    // Determine how many test samples to extract
    double prcnt = percent * 0.01;
    int numTest = (int)Math.round(set.size() * prcnt);

    // Randomly extract test samples into a TestSet Linked List
    Random rand = new Random();
    NeuralNetSampleSet testSet = new NeuralNetSampleSet();
    for (int i=0; i < numTest; i++) {
      int index = rand.nextInt(set.size());
      // Move the sample to the Test Set
      testSet.add((Sample)set.remove(index));

      // Check to see if the user cancelled the run
      if (i % 100 == 0) {
        if (applet.cancelled()) {
          applet.printCancelled();
          return null;
        }
      }
    }
    // Return the new TestSet
    return testSet;
  } // method extractTestSet

  // Accessor method to associate this set to the applet.
  // Necessary so this object can update the progress bar and output window.
  public void setApplet(NeuralNetApplet a) {
    applet = a;
  }
  
} // class NeuralNetSampleSet