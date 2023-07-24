package src;

import org.deeplearning4j.datasets.iterator.IteratorMultiDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration.GraphBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.graph.ComputationGraph;
import com.opencsv.CSVReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Network
{
    private static final Logger logger = LogManager.getLogger(Network.class);

    private static final String PIECE_SYMBOLS = "PNBRQKpnbrqk";
    private static final String CASTLING_SYMBOLS = "KQkq";
    private static final int BOARD_SIZE = 64;
    private static final int NUM_PIECE_TYPES = 6;
    private static final int NUM_CASTLING_RIGHTS = 4;
    private static final int NUM_FILES = 8;
    private static final int NUM_RANKS = 8;
    private static final int INPUT_SIZE = 64 * 320;
    public static void main(String[] args)
    {
        // Load data
        List<MultiDataSet> data = loadData();
        org.nd4j.linalg.dataset.MultiDataSet mainSet = org.nd4j.linalg.dataset.MultiDataSet.merge(data);
        logger.debug("Loaded data");
        int numInputs = (int) mainSet.getFeatures(0).size(1);
        int numOutputs = (int) mainSet.getLabels(0).size(1);
        int batchSize = 64;
        double dropoutProb = 0.1;
        // Architecture
        NeuralNetConfiguration.Builder layerBuilder = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Sgd(0.001));
        GraphBuilder mergedLayer = layerBuilder.graphBuilder()
            .addInputs("input1", "input2")
            .addLayer("dense1", new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(2048)
                .activation(Activation.RELU)
                .build(), "input1")
            .addLayer("dense2", new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(2048)
                .activation(Activation.RELU)
                .build(), "input2")
            .addVertex("merge", new MergeVertex(), "dense1", "dense2")
            .addLayer("dense3", new DenseLayer.Builder()
                .nIn(4096)
                .nOut(2048)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .build(), "merge")
            .addLayer("dense4", new DenseLayer.Builder()
                .nIn(2048)
                .nOut(1024)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU)
                .build(), "dense3")
            .addLayer("output", new OutputLayer.Builder()
                .nIn(1024)
                .nOut(numOutputs)
                .activation(Activation.TANH)
                .lossFunction(LossFunctions.LossFunction.MSE)
                .build(), "dense4")
            .setOutputs("output");
        ComputationGraphConfiguration config = mergedLayer.build();
        ComputationGraph graph = new ComputationGraph(config);
        graph.init();
        logger.debug("Built network architecture");
        // Train network
        logger.debug("Training network...");
        graph.setListeners(new ScoreIterationListener(100));
        int numEpochs = 25;
        for (int epoch = 0; epoch < numEpochs; epoch++)
        {
            logger.debug("Epoch " + epoch + " of " + numEpochs + "\r");
            Iterator<MultiDataSet> iterator = data.iterator();
            IteratorMultiDataSetIterator trainIterator = new IteratorMultiDataSetIterator(iterator, batchSize);
            while (trainIterator.hasNext())
            {
                MultiDataSet dataSet = trainIterator.next();
                graph.fit(dataSet);
            }
        }
        logger.debug("\nTraining completed.");
        // Save model
        logger.debug("Saving network...");
        File locationToSave = new File("trainedMLP.zip");
        boolean saveUpdater = true;
        try
        {
            ModelSerializer.writeModel(graph, locationToSave, saveUpdater);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static List<MultiDataSet> loadData()
    {
        List<MultiDataSet> dataSetList = new ArrayList<>();
        try
        {
            logger.debug("Loading data...");
            CSVReader reader = new CSVReader(new FileReader("./archive/chessData.csv"));
            String[] nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null && dataSetList.size() < 1000000)
            {
                double[][] fen = halfKP(nextLine[0]);

                // Handling special cases
                int index = nextLine[1].indexOf("#", 0);
                if (index != -1)
                {
                    nextLine[1] = "5000";
                }
                else
                {
                    index = nextLine[1].indexOf("+", 0);
                    if (index != -1)
                    {
                        nextLine[1] = nextLine[1].substring(0, index) + nextLine[1].substring(index + 1, nextLine[1].length());
                    }
                }
                nextLine[1] = nextLine[1].replaceAll("\uFEFF", "");

                double value = Double.parseDouble(nextLine[1]);
                
                // Scale the value between -1 and 1 for TANH
                double output = 2.0 * (value / 5000.0) - 1.0;

                INDArray inputWhite = Nd4j.create(fen[0], new int[] {1, INPUT_SIZE});
                INDArray inputBlack = Nd4j.create(fen[1], new int[] {1, INPUT_SIZE});
                INDArray outputArray = Nd4j.create(new double[] {output}, new int[]{1, 1});
                org.nd4j.linalg.dataset.MultiDataSet multiDataSet = new org.nd4j.linalg.dataset.MultiDataSet(new INDArray[] {inputWhite, inputBlack}, new INDArray[]{outputArray});
                dataSetList.add(multiDataSet);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        logger.debug("Data sets size: " + dataSetList.size());
        return dataSetList;
    }
    
    private static double[][] halfKP(String fen) {
        double[] whiteInput = new double[INPUT_SIZE];
        double[] blackInput = new double[INPUT_SIZE];
        
        String[] parts = fen.split(" ");
        String board = parts[0];
        int squareIndex = 0;
        int whiteKingPosition = -1;
        int blackKingPosition = -1;
    
        // First pass to find the king positions
        for (int i = 0; i < board.length(); i++) {
            char c = board.charAt(i);
            if (c == 'K') {
                whiteKingPosition = squareIndex;
            } else if (c == 'k') {
                blackKingPosition = squareIndex;
            } else if (Character.isDigit(c)) {
                squareIndex += c - '0';
            } else if (c != '/') {
                squareIndex++;
            }
        }
        squareIndex = 0;

        // Second pass to set the input values
        for (int i = 0; i < board.length(); i++) {
            char c = board.charAt(i);
            if (Character.isDigit(c)) {
                squareIndex += c - '0';
            } else if (c != '/' && c != 'K' && c != 'k') {
                int pieceIndex = pieceToNum(c);
                if (Character.isUpperCase(c)) {
                    blackInput[blackKingPosition * 320 + pieceIndex * 64 + squareIndex] = 1;
                } else {
                    whiteInput[whiteKingPosition * 320 + pieceIndex * 64 + squareIndex] = 1;
                }
                squareIndex++;
            } else if (c != '/') {
                squareIndex++;
            }
        }
        
        return new double[][] {whiteInput, blackInput};
    }
    
    private static int pieceToNum(char piece)
    {
        String pieces = "PpNnBbRrQqKk";
        return pieces.indexOf(piece) / 2;
    }

    public static ComputationGraph loadNetwork() throws IOException
    {
        ComputationGraph network = ModelSerializer.restoreComputationGraph("trainedMLP.zip");
        return network;
    }

    public static double score(String fen, ComputationGraph network)
    {
        double[][] fenData = halfKP(fen);
        double[] whiteFeatures = fenData[0];
        double[] blackFeatures = fenData[1];
        
        int halfLength = blackFeatures.length;
        INDArray inputWhite = Nd4j.create(whiteFeatures, new int[] {1, halfLength});
        INDArray inputBlack = Nd4j.create(blackFeatures, new int[] {1, halfLength});
        
        org.nd4j.linalg.dataset.MultiDataSet dataSet = new org.nd4j.linalg.dataset.MultiDataSet(new INDArray[] {inputWhite, inputBlack}, null);
        INDArray[] output = network.output(dataSet.getFeatures(0), dataSet.getFeatures(1));
        double score = output[0].getDouble(0);
        return score;
    }
}