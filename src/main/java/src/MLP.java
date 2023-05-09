package src;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.deeplearning4j.earlystopping.EarlyStoppingConfiguration;
import org.deeplearning4j.earlystopping.EarlyStoppingResult;
import org.deeplearning4j.earlystopping.saver.LocalFileModelSaver;
import org.deeplearning4j.earlystopping.scorecalc.DataSetLossCalculator;
import org.deeplearning4j.earlystopping.termination.MaxEpochsTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxScoreIterationTerminationCondition;
import org.deeplearning4j.earlystopping.termination.MaxTimeIterationTerminationCondition;
import org.deeplearning4j.earlystopping.trainer.EarlyStoppingTrainer;

import com.opencsv.CSVReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MLP
{
    public static void main(String[] args)
    {
        // Load data
        List<DataSet> dataSets = loadData();
        DataSet mainSet = DataSet.merge(dataSets);
        System.out.println("Loaded data");
        int numInputs = (int) dataSets.get(0).getFeatures().size(1);
        int numOutputs = (int) dataSets.get(0).getLabels().size(1);
        int batchSize = 64;
        double dropoutProb = 0.1;
        SplitTestAndTrain trainAndValid = mainSet.splitTestAndTrain(0.8);
        DataSetIterator trainIterator = new ListDataSetIterator<DataSet>(trainAndValid.getTrain().asList(), batchSize);
        DataSetIterator validIterator = new ListDataSetIterator<DataSet>(trainAndValid.getTest().asList());
        // MLP architecture
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .updater(new Sgd(0.001))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(4096)
                .activation(Activation.IDENTITY)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(4096)
                .nOut(2048)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(2048)
                .nOut(1024)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(1024)
                .nOut(512)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(512)
                .nOut(256)
                .dropOut(dropoutProb)
                .weightInit(WeightInit.XAVIER)
                .activation(Activation.RELU6)
                .build())
            .layer(new OutputLayer.Builder()
                .nIn(256)
                .nOut(numOutputs)
                .activation(Activation.TANH)
                .lossFunction(LossFunctions.LossFunction.MEAN_ABSOLUTE_ERROR)
                .build())
            .build();
        MultiLayerNetwork network = new MultiLayerNetwork(configuration);
        network.init();
        System.out.println("Built network architecture");
        // Train network
        System.out.println("Training network...");
        network.setListeners(new ScoreIterationListener(100));
        int numEpochs = 25;
        EarlyStoppingConfiguration stopConfig = new EarlyStoppingConfiguration.Builder()
            .epochTerminationConditions(new MaxEpochsTerminationCondition(numEpochs))
            .scoreCalculator(new DataSetLossCalculator(validIterator, true))
            .evaluateEveryNEpochs(1)
            .build();
        EarlyStoppingTrainer earlyStopper = new EarlyStoppingTrainer(stopConfig, network, trainIterator);
        EarlyStoppingResult<MultiLayerNetwork> result = earlyStopper.fit();
        network = result.getBestModel();
        double score = result.getBestModelScore();
        System.out.println("Best model score: " + score);
        System.out.println("Training completed.");
        // Save model
        System.out.println("Saving network...");
        File locationToSave = new File("trainedMLP.zip");
        boolean saveUpdater = true;
        try
        {
            ModelSerializer.writeModel(network, locationToSave, saveUpdater);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static List<DataSet> loadData()
    {
        List<DataSet> dataSets = new ArrayList<DataSet>();
        try
        {
            System.out.println("Loading data...");
            CSVReader reader = new CSVReader(new FileReader("./archive/chessData.csv"));
            String[] nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null && dataSets.size() < 1000000)
            {
                double[] input = stringToFen(nextLine[0]);
                int index = nextLine[1].indexOf("#", 0);
                if (index != -1)
                {
                    nextLine[1] = "100000"; // Assumed max value
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
                nextLine[1] = "" + ((2.0 * ((double) value + 100000) / 200000) - 1.0); // Scale the value between -1 and 1 for TANH
                double[] output = {Double.parseDouble(nextLine[1].trim())};
                INDArray inputArray = Nd4j.create(input, new int[]{1, input.length});
                INDArray outputArray = Nd4j.create(output, new int[]{1, 1});
                DataSet dataSet = new DataSet(inputArray, outputArray);
                dataSets.add(dataSet);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        System.out.println("Data sets size: " + dataSets.size());
        return dataSets;
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