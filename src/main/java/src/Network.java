package src;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration.GraphBuilder;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.SplitTestAndTrain;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingGraphTrainer;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.graph.ComputationGraph;
import com.opencsv.CSVReader;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
@SuppressWarnings("unchecked")
public class Network
{
    public static void main(String[] args)
    {
        // Load data
        List<org.nd4j.linalg.dataset.MultiDataSet> data = loadData();
        org.nd4j.linalg.dataset.MultiDataSet mainSet = org.nd4j.linalg.dataset.MultiDataSet.merge(data);
        System.out.println("Loaded data");
        int numInputs = (int) mainSet.getFeatures(0).size(1);
        int numOutputs = (int) mainSet.getLabels(0).size(1);
        int batchSize = 64;
        double dropoutProb = 0.1;
        SplitTestAndTrain trainAndValid = ((org.nd4j.linalg.dataset.api.DataSet) mainSet).splitTestAndTrain(0.8);
        DataSetIterator trainIterator = new ListDataSetIterator<DataSet>(trainAndValid.getTrain().asList(), batchSize);
        DataSetIterator validIterator = new ListDataSetIterator<DataSet>(trainAndValid.getTest().asList());

        // Architecture
        NeuralNetConfiguration.Builder layerBuilder = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Sgd(0.001));
        GraphBuilder mergedLayer = layerBuilder.graphBuilder()
            .addInputs("input1", "input2")
            .addLayer("dense1", new DenseLayer.Builder()
                .nIn(numInputs / 2)
                .nOut(4096)
                .activation(Activation.IDENTITY)
                .build(), "input1")
            .addLayer("dense2", new DenseLayer.Builder()
                .nIn(numInputs / 2)
                .nOut(4096)
                .activation(Activation.IDENTITY)
                .build(), "input2")
            .addVertex("merge", new MergeVertex(), "dense1", "dense2")
            .addLayer("dense3", new DenseLayer.Builder()
                .nIn(8192)
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
                .lossFunction(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                .build(), "dense4")
            .setOutputs("output");
        ComputationGraphConfiguration config = mergedLayer.build();
        ComputationGraph graph = new ComputationGraph(config);
        graph.init();
        System.out.println("Built network architecture");
        // Train network
        System.out.println("Training network...");
        graph.setListeners(new ScoreIterationListener(100));
        int numEpochs = 25;
        EarlyStoppingConfiguration<ComputationGraph> stopConfig = new EarlyStoppingConfiguration.Builder<ComputationGraph>()
            .epochTerminationConditions(new MaxEpochsTerminationCondition(numEpochs))
            .scoreCalculator(new DataSetLossCalculator(validIterator, true))
            .evaluateEveryNEpochs(1)
            .build();
        EarlyStoppingGraphTrainer trainer = new EarlyStoppingGraphTrainer(stopConfig, graph, trainIterator);
        EarlyStoppingResult<ComputationGraph> result = trainer.fit();
        graph = result.getBestModel();
        double score = result.getBestModelScore();
        System.out.println("Best model score: " + score);
        System.out.println("Training completed.");
        // Save model
        System.out.println("Saving network...");
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

    private static List<org.nd4j.linalg.dataset.MultiDataSet> loadData()
    {
        List<org.nd4j.linalg.dataset.MultiDataSet> dataSetList = new ArrayList<>();
        try
        {
            System.out.println("Loading data...");
            CSVReader reader = new CSVReader(new FileReader("./archive/chessData.csv"));
            String[] nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null && dataSetList.size() < 10000)
            {
                double[] fen = stringToFen(nextLine[0]);
                int index = nextLine[1].indexOf("#", 0);
                if (index != -1)
                {
                    nextLine[1] = "5000"; // Assumed max value
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
                int value = Integer.parseInt(nextLine[1].trim());
                nextLine[1] = "" + ((2.0 * ((double) value + 5000) / 10000) - 1.0); // Scale the value between -1 and 1 for TANH
                double output = Double.parseDouble(nextLine[1].trim());
                int halfLength = fen.length / 2; // Split FEN string
                INDArray inputWhite = Nd4j.create(fen, new int[] {1, halfLength});
                INDArray inputBlack = Nd4j.create(fen, new int[] {1, halfLength}, 'c');
                INDArray outputArray = Nd4j.create(new double[] {output}, new int[]{1, 1});
                org.nd4j.linalg.dataset.MultiDataSet multiDataSet = new org.nd4j.linalg.dataset.MultiDataSet(new INDArray[]{inputWhite, inputBlack}, new INDArray[]{outputArray});
                dataSetList.add(multiDataSet);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Data sets size: " + dataSetList.size());
        return dataSetList;
    }
    
    private static double[] stringToFen(String fen)
    {
        String[] parts = fen.split(" ");
        double[] input = new double[69];
        int index = 0;
        for (int i = 0; i < parts[0].length(); i++)
        {
            char c = parts[0].charAt(i);
            if (c >= '1' && c <= '8')
            {
                index += c - '0';
            }
            else if (c == '/') {}
            else
            {
                input[index] = pieceToNum(c);
                index++;
            }
        }
        return input;
    }

    private static int pieceToNum(char piece)
    {
        switch(piece)
        {
            case 'P':
                return 1;
            case 'N':
                return 2;
            case 'B':
                return 3;
            case 'R':
                return 4;
            case 'Q':
                return 5;
            case 'K':
                return 6;
            case 'p':
                return -1;
            case 'n':
                return -2;
            case 'b':
                return -3;
            case 'r':
                return -4;
            case 'q':
                return -5;
            case 'k':
                return -6;
            default:
                return 0;
        }
    }

    public static MultiLayerNetwork loadNetwork() throws IOException
    {
        MultiLayerNetwork network = ModelSerializer.restoreMultiLayerNetwork("trainedMLP.zip");
        return network;
    }

    public static double score(String fen, MultiLayerNetwork network)
    {
        double[] input = stringToFen(fen);
        int nIn = input.length;
        INDArray inputMatrix = Nd4j.create(input).reshape(1, nIn);
        INDArray output = network.output(inputMatrix);
        double score = output.getDouble(0);
        return score;
    }
}