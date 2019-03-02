package NeuralNet;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import oracle.jdeveloper.layout.VerticalFlowLayout;

public class NeuralNetApplet extends JApplet  {
  // Network configuration variables
  private int[] hiddenLayers = null;
  private int numHiddenLayers = 1;
  private int inputNodes = 41;
  private int outputNodes = 1;
  private boolean cancel = false;
  private File datafile = null;
  private long starttime = 0;

  // The GUI component definitions were moved to the bottom of this class
  // for readability.

  // Constructor
  public NeuralNetApplet() {
  }


  // Main method for running the applet stand alone
  public static void main(String[] args) {
    // Build the frame and initialized the GUI
    NeuralNetApplet applet = new NeuralNetApplet();
    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getContentPane().add(applet, BorderLayout.CENTER);
    frame.setTitle("Applet Frame");
    applet.init();
    applet.start();
    frame.setSize(650, 650);
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    frame.setLocation((d.width - frameSize.width) / 2, (d.height - frameSize.height) / 2);
    frame.setVisible(true);

    // Determine if the applet is being run stand alone...and thus has the
    // data file name specified as the first command-line argument
    if (args.length > 0) {
      applet.setDataFile(new File(args[0]));
    }
  } // method main


  public void init() {
    try {
      // Build the GUI
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // method init


  // This method starts the entire network run. It is called when the user
  // clicks the "Train and Test Neural Network" button.  It gathers the
  // settings from the GUI and launches the worker thread.
  private void trainAndTestButton_mouseClicked(MouseEvent e) {

    // Record the start time in milliseconds.  Used for an Elapsed Time metric.
    startTimer();
  
    // Clear old GUI indications, and enable the "Cancel Run" button.
    trainingErrorTextField.setText("");
    percentCorrectTextField.setText("");
    setProgress(0);
    setEpochProgress(0);
    setDataProgress(0);
    trainAndTestButton.setEnabled(false);
    cancelButton.setEnabled(true);
    cancel = false;

    // Retrieve the following user specified settings from GUI:
    //    - number of samples
    //    - % of samples to use as the training set
    //    - number of epochs
    //    - number of hidden layers
    //    - number of neurons in each hidden layer
    int numSamples = Integer.parseInt(sampleSizeTextField.getText());
    int test_pcnt = 100 - Integer.parseInt(percentTrainingTextField.getText());
    if (test_pcnt < 0) {
      test_pcnt = 1;
      percentTrainingTextField.setText(String.valueOf(100 - test_pcnt));
    }
    int numEpochs = Integer.parseInt(numEpochsTextField.getText());
    hiddenLayers = new int[numHiddenLayers];
    if (numHiddenLayers >= 1) {
      hiddenLayers[0] = Integer.parseInt(layer1TextField.getText());
    }
    if (numHiddenLayers >= 2) {
      hiddenLayers[1] = Integer.parseInt(layer2TextField.getText());
    }
    if (numHiddenLayers >= 3) {
      hiddenLayers[2] = Integer.parseInt(layer3TextField.getText());
    }

    // Prepare to pass the data source (either a local file if in stand alone
    // or the HTTP URL) to the worker thread as an Object type.  The worker
    // thread will use the instanceof operator to determine if this is a File
    // or URL instance.  This allows the use of a single worker thread
    // constructor...really just a coin flip as a design decision.
    Object datasource = null;
    if (datafile == null) {
      try {
        datasource = new URL ("http://scis.nova.edu/~sheaffer/corrected.100.zip");
      } catch (MalformedURLException ex) {
        println("Malformed URL");
        System.exit(1);
      }    
    }
    else {
      datasource = datafile;
    }

    // Print the run header in the output window
    println("************************************");
    println("Number of samples: " + numSamples);
    println("Number of training epochs: " + numEpochs);

    // Instantiate the worker thread and start
    NeuralNetWorkerThread worker = new NeuralNetWorkerThread(this, inputNodes, outputNodes, hiddenLayers, numSamples, numEpochs, test_pcnt, datasource);
    worker.start();
    
  } // method trainAndTestButton_mouseClicked


  //////////////////////////////////////////////////////////////////////////
  // The remaining methods below are all accessor methods, GUI component  //
  // handlers, and helper methods.                                        //
  //////////////////////////////////////////////////////////////////////////

  // Accessors to the "Data Load Progress" progress bar
  public void setDataProgress(int percent) {
    dataLoadProgressBar.setValue(percent);
  }
  public void setDataProgressMax(int max) {
    dataLoadProgressBar.setMaximum(max);
  }

  // Accessors to the "Training Progress" progress bar
  public void setProgress(int percent) {
    progressBar.setValue(percent);
  }
  public void setProgressMax(int max) {
    progressBar.setMaximum(max);
  }

  // Accessors to the "Current Epoch Progress" progress bar
  public void setEpochProgress(int percent) {
    epochProgressBar.setValue(percent);
  }
  public void setEpochProgressMax(int max) {
    epochProgressBar.setMaximum(max);
  }

  // Button click handlers for the + - buttons to adjust the number
  // of neurons in each hidden layer.
  // The first 6 methods call the last two.
  private void layer1UpButton_mouseClicked(MouseEvent e) {
    layerValueUp(layer1TextField);
  }
  private void layer1DownButton_mouseClicked(MouseEvent e) {
    layerValueDown(layer1TextField);
  }
  private void layer2UpButton_mouseClicked(MouseEvent e) {
    layerValueUp(layer2TextField);
  }
  private void layer2DownButton_mouseClicked(MouseEvent e) {
    layerValueDown(layer2TextField);
  }
  private void layer3UpButton_mouseClicked(MouseEvent e) {
    layerValueUp(layer3TextField);
  }
  private void layer3DownButton_mouseClicked(MouseEvent e) {
    layerValueDown(layer3TextField);
  }
  private void layerValueUp(JTextField layer) {
    int current = Integer.parseInt(layer.getText());
    if (current < 50) {
      layer.setText(String.valueOf(current + 1));
    }
  }
  private void layerValueDown (JTextField layer) {
    int current = Integer.parseInt(layer.getText());
    if (current > 1) {
      layer.setText(String.valueOf(current - 1));
    }
  }

  // ComboBox event handler to display/hide the hidden layer setting
  // based on the number of hidden layers selected in the ComboBox.
  private void numHiddenLayersComboBox_actionPerformed(ActionEvent e) {
    numHiddenLayers = numHiddenLayersComboBox.getSelectedIndex() + 1;

    // hidden layer 1
    if (numHiddenLayers >= 1) {
      layer1TextField.setVisible(true);
      layer1UpButton.setVisible(true);
      layer1DownButton.setVisible(true);
      layer1Label.setVisible(true);
    }
    else {
      layer1TextField.setVisible(false);
      layer1UpButton.setVisible(false);
      layer1DownButton.setVisible(false);      
      layer1Label.setVisible(false);
    }

    // hidden layer 2
    if (numHiddenLayers >= 2) {
      layer2TextField.setVisible(true);
      layer2UpButton.setVisible(true);
      layer2DownButton.setVisible(true);
      layer2Label.setVisible(true);
    }
    else {
      layer2TextField.setVisible(false);
      layer2UpButton.setVisible(false);
      layer2DownButton.setVisible(false);      
      layer2Label.setVisible(false);
    }

    // hidden layer 3
    if (numHiddenLayers >= 3) {
      layer3TextField.setVisible(true);
      layer3UpButton.setVisible(true);
      layer3DownButton.setVisible(true);
      layer3Label.setVisible(true);
    }
    else {
      layer3TextField.setVisible(false);
      layer3UpButton.setVisible(false);
      layer3DownButton.setVisible(false);      
      layer3Label.setVisible(false);
    }
  } // method numHiddenLayersComboBox_actionPerformed

  // Accessors to set the result values in the GUI
  public void setPercentCorrect(double prcnt) {
    prcnt = Math.round(prcnt * 10000.0) / 100.0;
    percentCorrectTextField.setText(String.valueOf(prcnt) + "%");
  }
  public void setTrainingError(double rms) {
    rms = Math.round(rms * 100000.0) / 100000.0;
    trainingErrorTextField.setText(String.valueOf(rms));
  }

  // Periodically called by the other two thread to check if the user
  // has canceled the run.
  public boolean cancelled() {
    return cancel;
  }

  // Called by other threads after they notice the cancel button has been
  // pressed.  Lets the user know the cancel request was received by the
  // current thread.
  public void printCancelled() {
    println("Cancelling (Please wait)...");
  }

  // Handler method for the "Cancel Run" button.
  // This method simply sets the cancel flag and resets the GUI buttons.
  // Threads are expected to periodically check this flag.
  private void cancelButton_mouseClicked(MouseEvent e) {
    cancel = true;
    cancelButton.setEnabled(false);
    trainAndTestButton.setEnabled(true);
  }

  // Called by any object that wishes to print text to the output window.
  // This method does NOT end the text with a newline.
  public void print(String output) {
    outputTextArea.append(output);
  }

  // Called by any object that wishes to print text to the output window.
  // This method DOES end the text with a newline.
  public void println(String output) {
    outputTextArea.append(output + "\n");
  }

  // Called by the last thread to reset the GUI buttons.
  public void endRun() {
    cancel = false;
    cancelButton.setEnabled(false);
    trainAndTestButton.setEnabled(true);
  }

  // Button handler for the "Clear" button to clear the output window
  private void clearOutputButton_mouseClicked(MouseEvent e) {
    outputTextArea.setText("");
  }

  // Accessor method for the NeuralNetApplet called by the static main()
  // method to set the data file (if specified).
  public void setDataFile(File f) {
    datafile = f;
  }

  // Records the system time in milliseconds as an elapsed time metric.
  // Note:  The applet can only track one timer since there is only one
  // starttime variable.
  public void startTimer() {
    starttime = System.currentTimeMillis();
  }

  // Returns the number of milliseconds elapsed since the startTimer()
  // method was called.
  public long stopTimer() {
    return System.currentTimeMillis() - starttime;
  }

  // Private method called by init() to build the GUI
  private void jbInit() throws Exception {
    this.getContentPane().setLayout(verticalFlowLayout7);
    this.setSize(new Dimension(650, 659));
    leftTopPanel.setLayout(verticalFlowLayout6);
    sampleDataPanel.setLayout(verticalFlowLayout1);
    sampleDataPanel.setBorder(BorderFactory.createTitledBorder("Sample Data Settings"));
    jLabel1.setText("Number of samples");
    sampleSizeTextField.setText("10000");
    sampleSizeTextField.setColumns(10);
    jLabel2.setText("Percent to use as training data");
    percentTrainingTextField.setText("75");
    percentTrainingTextField.setColumns(4);
    jLabel3.setText("Number of training epochs (iterations)");
    numEpochsTextField.setText("10");
    numEpochsTextField.setColumns(5);
    neuralNetworkPanel.setLayout(verticalFlowLayout3);
    neuralNetworkPanel.setBorder(BorderFactory.createTitledBorder("Neural Network Configuration"));
    jLabel9.setText("Number of hidden layers");
    numHiddenLayersComboBox.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          numHiddenLayersComboBox_actionPerformed(e);
        }
      });
    layer1Label.setText("Number of neurons in Layer 1");
    layer1TextField.setText("10");
    layer1TextField.setEditable(false);
    layer1TextField.setColumns(3);
    layer1TextField.setMargin(new Insets(0, 5, 0, 2));
    layer1UpButton.setText("+");
    layer1UpButton.setFocusPainted(false);
    layer1UpButton.setToolTipText("Increase the number of neurons");
    layer1UpButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer1UpButton_mouseClicked(e);
        }
      });
    layer1DownButton.setText("-");
    layer1DownButton.setFocusPainted(false);
    layer1DownButton.setToolTipText("Decrease the number of neurons");
    layer1DownButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer1DownButton_mouseClicked(e);
        }
      });
    layer2Label.setText("Number of neurons in Layer 2");
    layer2TextField.setText("5");
    layer2TextField.setEditable(false);
    layer2TextField.setMargin(new Insets(0, 5, 0, 2));
    layer2TextField.setColumns(3);
    layer2UpButton.setText("+");
    layer2UpButton.setToolTipText("Increase the number of neurons");
    layer2UpButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer2UpButton_mouseClicked(e);
        }
      });
    layer2DownButton.setText("-");
    layer2DownButton.setToolTipText("Decrease the number of neurons");
    layer2DownButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer2DownButton_mouseClicked(e);
        }
      });
    layer3Label.setText("Number of neurons in Layer 3");
    layer3TextField.setText("2");
    layer3TextField.setEditable(false);
    layer3TextField.setMargin(new Insets(0, 5, 0, 2));
    layer3TextField.setColumns(3);
    layer3UpButton.setText("+");
    layer3UpButton.setToolTipText("Increase the number of neurons");
    layer3UpButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer3UpButton_mouseClicked(e);
        }
      });
    layer3DownButton.setText("-");
    layer3DownButton.setToolTipText("Decrease the number of neurons");
    layer3DownButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          layer3DownButton_mouseClicked(e);
        }
      });
    rightTopPanel.setLayout(verticalFlowLayout2);
    jPanel16.setLayout(verticalFlowLayout5);
    trainingErrorLabel.setText("Final training RMS error:");
    trainingErrorTextField.setColumns(7);
    trainingErrorTextField.setEditable(false);
    percentCorrectLabel.setText("Percent correct on test set:");
    percentCorrectTextField.setColumns(5);
    percentCorrectTextField.setEditable(false);
    progressPanel.setLayout(verticalFlowLayout4);
    progressPanel.setBorder(BorderFactory.createTitledBorder("Progress"));
    jPanel13.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    jPanel13.setLayout(flowLayout1);
    jLabel10.setText("Training Progress");
    jPanel15.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
    jLabel13.setText("Current Epoch Progress");
    trainAndTestButton.setText("Train and Test Neural Net");
    trainAndTestButton.setToolTipText("Press this button to start the network");
    trainAndTestButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          trainAndTestButton_mouseClicked(e);
        }
      });
    cancelButton.setText("Cancel Run");
    cancelButton.setToolTipText("Press this button to cancel the current network run");
    cancelButton.setEnabled(false);
    cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          cancelButton_mouseClicked(e);
        }
      });
    outputScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    outputTextArea.setColumns(70);
    outputTextArea.setRows(13);
    outputTextArea.setFont(new Font("Courier New", 0, 11));
    outputTextArea.setEditable(false);
    clearOutputButton.setText("Clear");
    clearOutputButton.setToolTipText("Clear the output window");
    clearOutputButton.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          clearOutputButton_mouseClicked(e);
        }
      });
    jLabel4.setText("Data Load Progress");
    rightTopPanel.add(progressPanel, null);
    rightTopPanel.add(trainAndTestButton, null);
    rightTopPanel.add(cancelButton, null);
    resultsPanel.add(jPanel16, null);
    rightTopPanel.add(resultsPanel, null);
    jPanel16.add(jPanel1, null);
    jPanel16.add(jPanel2, null);
    jPanel1.add(trainingErrorLabel, null);
    jPanel1.add(trainingErrorTextField, null);
    jPanel2.add(percentCorrectLabel, null);
    jPanel2.add(percentCorrectTextField, null);
    jPanel9.add(dataLoadProgressBar, null);
    jPanel8.add(jLabel4, null);
    progressPanel.add(jPanel8, null);
    progressPanel.add(jPanel9, null);
    progressPanel.add(jPanel13, null);
    jPanel17.add(progressBar, null);
    progressPanel.add(jPanel17, null);
    progressPanel.add(jPanel15, null);
    progressPanel.add(jPanel14, null);
    jPanel13.add(jLabel10, null);
    jPanel15.add(jLabel13, null);
    jPanel14.add(epochProgressBar, null);
    leftTopPanel.add(sampleDataPanel, null);
    leftTopPanel.add(neuralNetworkPanel, null);
    sampleDataPanel.add(jPanel4, null);
    sampleDataPanel.add(jPanel5, null);
    sampleDataPanel.add(jPanel6, null);
    jPanel4.add(jLabel1, null);
    jPanel4.add(sampleSizeTextField, null);
    jPanel5.add(jLabel2, null);
    jPanel5.add(percentTrainingTextField, null);
    jPanel6.add(jLabel3, null);
    jPanel6.add(numEpochsTextField, null);
    neuralNetworkPanel.add(jPanel12, null);
    neuralNetworkPanel.add(jPanel7, null);
    neuralNetworkPanel.add(jPanel11, null);
    neuralNetworkPanel.add(jPanel10, null);
    jPanel12.add(jLabel9, null);
    jPanel12.add(numHiddenLayersComboBox, null);
    numHiddenLayersComboBox.addItem("1");
    numHiddenLayersComboBox.addItem("2");
    numHiddenLayersComboBox.addItem("3");
    jPanel7.add(layer1Label, null);
    jPanel7.add(layer1TextField, null);
    jPanel7.add(layer1UpButton, null);
    jPanel7.add(layer1DownButton, null);
    jPanel11.add(layer2Label, null);
    jPanel11.add(layer2TextField, null);
    jPanel11.add(layer2UpButton, null);
    jPanel11.add(layer2DownButton, null);
    jPanel10.add(layer3Label, null);
    jPanel10.add(layer3TextField, null);
    jPanel10.add(layer3UpButton, null);
    jPanel10.add(layer3DownButton, null);
    jPanel3.add(leftTopPanel, null);
    jPanel3.add(rightTopPanel, null);
    this.getContentPane().add(jPanel3, null);
    outputScrollPane.getViewport().add(outputTextArea, null);
    outputPanel.add(outputScrollPane, null);
    outputPanel.add(clearOutputButton, null);
    this.getContentPane().add(outputPanel, null);
  } // method jbInit

  // GUI component declarations
  private JPanel jPanel3 = new JPanel();
  private JPanel leftTopPanel = new JPanel();
  private VerticalFlowLayout verticalFlowLayout6 = new VerticalFlowLayout();
  private JPanel sampleDataPanel = new JPanel();
  private VerticalFlowLayout verticalFlowLayout1 = new VerticalFlowLayout();
  private JPanel jPanel4 = new JPanel();
  private JLabel jLabel1 = new JLabel();
  private JTextField sampleSizeTextField = new JTextField();
  private JPanel jPanel5 = new JPanel();
  private JLabel jLabel2 = new JLabel();
  private JTextField percentTrainingTextField = new JTextField();
  private JPanel jPanel6 = new JPanel();
  private JLabel jLabel3 = new JLabel();
  private JTextField numEpochsTextField = new JTextField();
  private JPanel neuralNetworkPanel = new JPanel();
  private VerticalFlowLayout verticalFlowLayout3 = new VerticalFlowLayout();
  private JPanel jPanel12 = new JPanel();
  private JLabel jLabel9 = new JLabel();
  private JComboBox numHiddenLayersComboBox = new JComboBox();
  private JPanel jPanel7 = new JPanel();
  private JLabel layer1Label = new JLabel();
  private JTextField layer1TextField = new JTextField();
  private JButton layer1UpButton = new JButton();
  private JButton layer1DownButton = new JButton();
  private JPanel jPanel11 = new JPanel();
  private JLabel layer2Label = new JLabel();
  private JTextField layer2TextField = new JTextField();
  private JButton layer2UpButton = new JButton();
  private JButton layer2DownButton = new JButton();
  private JPanel jPanel10 = new JPanel();
  private JLabel layer3Label = new JLabel();
  private JTextField layer3TextField = new JTextField();
  private JButton layer3UpButton = new JButton();
  private JButton layer3DownButton = new JButton();
  private JPanel rightTopPanel = new JPanel();
  private VerticalFlowLayout verticalFlowLayout2 = new VerticalFlowLayout();
  private JPanel resultsPanel = new JPanel();
  private JPanel jPanel16 = new JPanel();
  private VerticalFlowLayout verticalFlowLayout5 = new VerticalFlowLayout();
  private JPanel jPanel1 = new JPanel();
  private JLabel trainingErrorLabel = new JLabel();
  private JTextField trainingErrorTextField = new JTextField();
  private JPanel jPanel2 = new JPanel();
  private JLabel percentCorrectLabel = new JLabel();
  private JTextField percentCorrectTextField = new JTextField();
  private JPanel progressPanel = new JPanel();
  private VerticalFlowLayout verticalFlowLayout4 = new VerticalFlowLayout();
  private JPanel jPanel13 = new JPanel();
  private JLabel jLabel10 = new JLabel();
  private JPanel jPanel17 = new JPanel();
  private JPanel jPanel15 = new JPanel();
  private JLabel jLabel13 = new JLabel();
  private JPanel jPanel14 = new JPanel();
  private JProgressBar epochProgressBar = new JProgressBar();
  private JButton trainAndTestButton = new JButton();
  private JButton cancelButton = new JButton();
  private VerticalFlowLayout verticalFlowLayout7 = new VerticalFlowLayout();
  private JPanel outputPanel = new JPanel();
  private JScrollPane outputScrollPane = new JScrollPane();
  private JTextArea outputTextArea = new JTextArea();
  private JButton clearOutputButton = new JButton();
  private JProgressBar progressBar = new JProgressBar();
  private FlowLayout flowLayout1 = new FlowLayout();
  private JPanel jPanel8 = new JPanel();
  private JPanel jPanel9 = new JPanel();
  private JLabel jLabel4 = new JLabel();
  private JProgressBar dataLoadProgressBar = new JProgressBar();  
} // class NeuralNetApplet