# NeuralNetworkApplet

###Demonstrating Supervised Machine Learning of Network Intrusion Signatures

*Developed by John Sheaffer for CISC 670 (Fall 2002), [Nova Southeastern University](https://www.nova.edu/), M.S. of Computer Science program*

---

**Note:** This code is currently not working since it refers to a dataset location that no longer exists. I intend to update the code so it works again.

---


###Neural Network Applet Overview:
This applet demonstrates the technique of Supervised Machine Learning using a Neural Network in the context of Intrusion Detection Systems (IDS).

The applet analyzes network connection records using a Multi-layer Feed-forward Neural Network. First the network is trained using samples and their expected output through the Back-Propagation learning algorithm. Then the trained network is tested on a different set of samples and the expected vs. actual output is compared to determine the network's accuracy. The network's output is either 0.0 for a normal connection, or 1.0 for a attack related connection.

The sample data was obtained from the University of California, Irvine, [Knowledge Discovery Database](http://kdd.ics.uci.edu/). Each sample is a connection record stored in a text file as a comma-separated string of connection characteristics. This list of [Derived Features](http://kdd.ics.uci.edu/databases/kddcup99/task.html) describes the connection characteristics each record includes. The data used by this applet is the first 100,000 records of the data in the [corrected.gz](http://kdd.ics.uci.edu/databases/kddcup99/corrected.gz) file in the UCI-KDD archive.

###How to use the applet:
All you have to do is press the "Train and Test Neural Net" button. You may wish to change any of the settings in the "Sample Data Settings" or "Neural Network Configuration" panels.

When you click the "Train and Test Neural Net" button, the following happens:

1. The applet connects to the scis.nova.edu server and opens a stream to the data file.
2. The applet reads the first n records from the data file(where n is the value in the "Number of samples" setting) and determines the maximum value of each attribute of the record.
3. The applet reads the data file a second time and loads the data into memory, normalizing each records attribute value between 0.0 and 1.0 inclusive.
4. The applet randomly extracts a percentage of the sample data as test data (to be use to compare the expected vs. actual output). This leaves two data sets, Training and Test, who's sum is the value specified in the "Number of samples" setting. The test set is removed from the training set so the network can't simply "memorize" the output to the test samples.
5. The applet builds the Neural Network with 42 input neurons, 1 output neuron, and as many hidden layers and neurons as specified in the "Neural Network Configuration" panel.
6. The applet trains the Neural Network by executing the network and adjusting the connection weights using the Back-Propagation learning algorithm.
7. The applet repeats the previous step as many times as listed in the "Number of training epochs(iterations)".
8. The applet executes the Neural Network on the Test set and computes the percentage of correct outputs. The tolerance for accuracy is 0.1, and the output can range from 0.0 (normal connection) to 1.0 (attack connection). Therefore an attack must output between 0.9 and 1.0, and an normal connection must output between 0.0 and 0.1.

Training a Neural Network is computationally intensive. I received tolerable run completion times using samples sets of up to 30,000 and 10 epochs, on a 1 GHz computer. On slower computers, the runs may take considerably longer.

To cancel a run, simply press the "Cancel Run" button. All of the loading and network activities are launched in separate threads. These threads periodically check to see if the user has pressed the "Cancel Run" button.

###Running Stand Alone:
You can also run the applet stand alone. To do this you must have JRE 1.3.1 or higher installed on your computer.

1. First, download the applet JAR file and the data file:
 * NeuralNetApplet.jar
 * corrected.100.zip
2. Place both files in the same directory and run the applet with:
```
java -jar NeuralNetApplet.jar corrected.100.zip
```

Note: If you don't specify the data file as the first argument, the applet will attempt to access the data file at "http://scis.nova.edu/~sheaffer/corrected.100.zip"

 

 

 
