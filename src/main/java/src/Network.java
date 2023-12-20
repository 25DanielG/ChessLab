package src;

import org.deeplearning4j.datasets.iterator.IteratorMultiDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration.GraphBuilder;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
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
import org.deeplearning4j.nn.conf.graph.PreprocessorVertex;
import org.deeplearning4j.nn.graph.ComputationGraph;
import com.opencsv.CSVReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Network {
    private static final Logger logger = LogManager.getLogger(Network.class);
    private static final String path = "network.zip";
    private static final int INPUT_SIZE = 19 * 64; // 19 bitboards, 64 squares each

    public static void main(String[] args) {
        MemoryIterator iterator = new MemoryIterator("./archive/chessData.csv", 64);

        int numInputs = INPUT_SIZE;
        int numOutputs = 1;
        double dropoutProb = 0.1;

        NeuralNetConfiguration.Builder layerBuilder = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Sgd(0.001));

        GraphBuilder graphBuilder = layerBuilder.graphBuilder()
            .addInputs("input")
            // convulutional layers
            .addLayer("conv1", new ConvolutionLayer.Builder(1, 3)
                .nIn(19)
                .nOut(32)
                .stride(1, 1) // Adjusted stride
                .activation(Activation.RELU)
                .build(), "input")
            .addLayer("conv2", new ConvolutionLayer.Builder(1, 3)
                .nIn(32)
                .nOut(64)
                .stride(1, 1) // Adjusted stride
                .activation(Activation.RELU)
                .build(), "conv1")
            // pooling
            .addLayer("pooling1", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{1, 2})
                .stride(1, 2)
                .build(), "conv2")
            .addVertex("flatten", new PreprocessorVertex(new CnnToFeedForwardPreProcessor()), "pooling1")
            // dense layers
            .addLayer("dense1", new DenseLayer.Builder()
                .nIn(1024)
                .nOut(2048)
                .activation(Activation.RELU)
                .build(), "flatten")
            .addLayer("dense2", new DenseLayer.Builder()
                .nIn(2048)
                .nOut(1024)
                .activation(Activation.RELU)
                .build(), "dense1")
            // output
            .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                .nIn(1024)
                .nOut(numOutputs)
                .activation(Activation.TANH)
                .build(), "dense2")
            .setOutputs("output");

        ComputationGraphConfiguration config = graphBuilder.build();
        ComputationGraph graph = new ComputationGraph(config);
        graph.init();
        System.out.println("Built network architecture");

        System.out.println("Training network...");
        graph.setListeners(new ScoreIterationListener(100));
        int numEpochs = 25;
        for (int epoch = 0; epoch < numEpochs; epoch++) {
            System.out.println("Epoch " + epoch + " of " + numEpochs + "\r");
            while (iterator.hasNext()) {
                MultiDataSet dataSet = iterator.next();
                graph.fit(dataSet);
            }
            iterator.reset();
        }
        System.out.println("\nTraining completed.");
        System.out.println("Saving network...");
        saveNetwork(graph, new File(path), true);
    }

    private static void saveNetwork(ComputationGraph graph, File locationToSave, boolean saveUpdater) {
        try {
            ModelSerializer.writeModel(graph, locationToSave, saveUpdater);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<MultiDataSet> loadData() {
        List<MultiDataSet> dataSetList = new ArrayList<>();
        try {
            CSVReader reader = new CSVReader(new FileReader("./archive/chessData.csv"));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null && dataSetList.size() < 500) {
                double[][][] bitboards = fenToBitboards(nextLine[0]);
                double value = parseValue(nextLine[1]);
                double output = 2.0 * (value / 5000.0) - 1.0;
    
                INDArray inputArray = Nd4j.create(bitboards).reshape(1, 19, 8, 8);
                INDArray outputArray = Nd4j.create(new double[] {output}, new int[]{1, 1});
                MultiDataSet multiDataSet = new org.nd4j.linalg.dataset.MultiDataSet(new INDArray[] {inputArray}, new INDArray[]{outputArray});
               dataSetList.add(multiDataSet);
            }
            reader.close(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("Data sets size: " + dataSetList.size());
        return dataSetList;
    }

    public static double[][][] fenToBitboards(String fen) {
        double[][][] bitboards = new double[19][8][8]; // 19 layers, each 8x8
    
        Map<Character, Integer> pieceToBitboardIndex = new HashMap<>();
        String pieces = "PNBRQKpnbrqk";
        for (int i = 0; i < pieces.length(); i++) {
            pieceToBitboardIndex.put(pieces.charAt(i), i);
        }
    
        String[] fenParts = fen.split(" ");
        String board = fenParts[0];
        int row = 0, col = 0;
        for (char c : board.toCharArray()) {
            if (c == '/') {
                row++;
                col = 0;
            } else if (Character.isDigit(c)) {
                col += Character.getNumericValue(c);
            } else {
                int boardIndex = pieceToBitboardIndex.get(c);
                bitboards[boardIndex][row][col] = 1;
                col++;
            }
        }
    
        // castling
        String castlingRights = "KQkq";
        for (int i = 0; i < castlingRights.length(); i++) {
            if (fenParts[2].indexOf(castlingRights.charAt(i)) != -1) {
                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {
                        bitboards[12 + i][r][c] = 1;
                    }
                }
            }
        }
    
        // en passant
        if (!fenParts[3].equals("-")) {
            int pos = algebraicToSquareIndex(fenParts[3]);
            bitboards[16][pos / 8][pos % 8] = 1;
        }
    
        // color to move
        int activeColorLayer = fenParts[1].equals("w") ? 0 : 1;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                bitboards[17][r][c] = activeColorLayer;
            }
        }
    
        // full move
        int fullMoveNumber = Integer.parseInt(fenParts[5]);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                bitboards[18][r][c] = fullMoveNumber;
            }
        }
    
        return bitboards;
    }
    
    private static int algebraicToSquareIndex(String algebraic) {
        int file = algebraic.charAt(0) - 'a';
        int rank = 7 - (algebraic.charAt(1) - '1');
        return 8 * rank + file;
    }

    public static double parseValue(String valueStr) {
        if (valueStr.contains("#")) {
            valueStr = "5000";
        } else if (valueStr.contains("+")) {
            valueStr = valueStr.replaceAll("\\+", "");
        }
        return Double.parseDouble(valueStr.trim());
    }

    public static ComputationGraph loadNetwork() throws IOException {
        return ModelSerializer.restoreComputationGraph(path);
    }

    public static double score(double[][][] bitboards, ComputationGraph network) {
        INDArray inputArray = Nd4j.create(bitboards).reshape(1, 19, 8, 8);

        INDArray output = network.output(inputArray)[0];
        return output.getDouble(0);
    }
}