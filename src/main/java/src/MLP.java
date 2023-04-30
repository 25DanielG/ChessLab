package src;

import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import com.opencsv.CSVReader;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class MLP
{
    public static void main(String[] args)
    {
        // Load data
        List<DataSet> dataSets = loadData();
        System.out.println("Loaded data");
        int numInputs = (int) dataSets.get(0).getFeatures().size(1);
        int numOutputs = (int) dataSets.get(0).getLabels().size(1);
        int numHiddenNodes = 100;
        int batchSize = 64;
        // Split data
        DataSetIterator iterator = new ListDataSetIterator<DataSet>(dataSets, batchSize);
        // MLP architecture
        MultiLayerConfiguration configuration = new NeuralNetConfiguration.Builder()
            .seed(123)
            .optimizationAlgo(OptimizationAlgorithm.LINE_GRADIENT_DESCENT)
            .updater(new Sgd(0.1))
            .list()
            .layer(new DenseLayer.Builder()
                .nIn(numInputs)
                .nOut(numHiddenNodes)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(numHiddenNodes)
                .nOut(numHiddenNodes)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(numHiddenNodes)
                .nOut(numHiddenNodes)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(numHiddenNodes)
                .nOut(numHiddenNodes)
                .activation(Activation.RELU6)
                .build())
            .layer(new DenseLayer.Builder()
                .nIn(numHiddenNodes)
                .nOut(numHiddenNodes)
                .activation(Activation.RELU6)
                .build())
            .layer(new OutputLayer.Builder()
                .nIn(numHiddenNodes)
                .nOut(numOutputs)
                .activation(Activation.IDENTITY)
                .lossFunction(LossFunctions.LossFunction.MSE)
                .build())
            .build();
        MultiLayerNetwork network = new MultiLayerNetwork(configuration);
        network.init();
        System.out.println("Built network architecture");
        // Train network
        System.out.println("Training network...");
        network.setListeners(new ScoreIterationListener(100));
        int numEpochs = 10;
        System.out.print("|                    |\r");
        String spaceString = "                    ";
        String progressString = "";
        for (int i = 0; i < numEpochs; i++)
        {
            iterator.reset();
            network.fit(iterator);
            progressString += "==";
            spaceString = spaceString.substring(0, spaceString.length() - 2);
            System.out.print("|" + progressString + spaceString + "|\r");
        }
        System.out.println();
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
            while ((nextLine = reader.readNext()) != null)
            {
                double[] input = stringToFen(nextLine[0]);
                int index = nextLine[1].indexOf("#", 0);
                if (index != -1)
                {
                    nextLine[1] = nextLine[1].substring(0, index) + nextLine[1].substring(index + 1, nextLine[1].length());
                }
                index = nextLine[1].indexOf("+", 0);
                if (index != -1)
                {
                    nextLine[1] = nextLine[1].substring(0, index) + nextLine[1].substring(index + 1, nextLine[1].length());
                }
                nextLine[1] = nextLine[1].replaceAll("\uFEFF", "");
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
        return dataSets;
    }
    
    private static double[] stringToFen(String fen)
    {
        String[] parts = fen.split(" ");
        String position = parts[0];
        double[] nums = new double[64];
        int index = 0;
        for (int i = 0; i < position.length(); i++)
        {
            char c = position.charAt(i);
            if (c >= '1' && c <= '8')
            {
                index += c - '0';
            }
            else if (c == '/')
            {}
            else
            {
                nums[index] = pieceToNum(c);
                index++;
            }
        }
        double[] result = new double[index];
        for (int i = 0; i < index; i++)
        {
            result[i] = nums[i];
        }
        return result;
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