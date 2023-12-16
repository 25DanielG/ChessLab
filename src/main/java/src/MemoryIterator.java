package src;

import org.nd4j.linalg.dataset.api.iterator.MultiDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.dataset.api.MultiDataSetPreProcessor;
import org.nd4j.linalg.factory.Nd4j;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MemoryIterator implements MultiDataSetIterator {
    private CSVReader reader;
    private int batchSize;
    private boolean hasNext = true;
    private String filename;
    private int lineno = 0;
    private int batchLimit;

    public MemoryIterator(String filename, int batchSize) {
        this.batchSize = batchSize;
        this.filename = filename;
        this.batchLimit = 0;
        try {
            this.reader = new CSVReader(new FileReader(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MemoryIterator(String filename, int batchSize, int batchLimit) {
        this.batchSize = batchSize;
        this.filename = filename;
        this.batchLimit = batchLimit;
        try {
            this.reader = new CSVReader(new FileReader(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasNext() {
        if (batchLimit != 0) {
            return hasNext && lineno < (batchLimit * batchSize);
        }
        return hasNext;
    }

    @Override
    public MultiDataSet next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }
        ++lineno;

        List<MultiDataSet> dataSetList = new ArrayList<>();
        try {
            String[] nextLine = null;
            while (dataSetList.size() < batchSize && (nextLine = reader.readNext()) != null) {
                double[][][] bitboards = Network.fenToBitboards(nextLine[0]);
                double value = Network.parseValue(nextLine[1]);
                double output = 2.0 * (value / 5000.0) - 1.0;

                INDArray inputArray = Nd4j.createFromArray(bitboards).reshape(1, 19, 8, 8);
                INDArray outputArray = Nd4j.create(new double[] {output}, new int[]{1, 1});
                dataSetList.add(new org.nd4j.linalg.dataset.MultiDataSet(inputArray, outputArray));
            }

            if (nextLine == null) {
                hasNext = false;
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.out.println("CSV Validation Exception");
            e.printStackTrace();
        }

        return org.nd4j.linalg.dataset.MultiDataSet.merge(dataSetList);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiDataSet next(int num) {
        return next();
    }

    @Override
    public void reset() {
        try {
            reader.close();
            reader = new CSVReader(new FileReader(filename));
            hasNext = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void setPreProcessor(MultiDataSetPreProcessor preProcessor) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiDataSetPreProcessor getPreProcessor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean resetSupported() {
        return true;
    }

    @Override
    public boolean asyncSupported() {
        return false;
    }
}