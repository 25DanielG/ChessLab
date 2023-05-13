package src;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

public class CustomDataSetIterator implements DataSetIterator
{

    private DataSetIterator iterator1;
    private DataSetIterator iterator2;

    public CustomDataSetIterator(DataSetIterator iterator1, DataSetIterator iterator2)
    {
        this.iterator1 = iterator1;
        this.iterator2 = iterator2;
    }

    @Override
    public boolean hasNext()
    {
        return iterator1.hasNext() && iterator2.hasNext();
    }

    @Override
    public DataSet next()
    {
        DataSet dataSet1 = iterator1.next();
        DataSet dataSet2 = iterator2.next();
        INDArray features1 = dataSet1.getFeatures();
        INDArray features2 = dataSet2.getFeatures();
        INDArray labels1 = dataSet1.getLabels();
        INDArray labels2 = dataSet2.getLabels();
        INDArray mergedFeatures = Nd4j.concat(1, features1, features2);
        INDArray mergedLabels = Nd4j.concat(1, labels1, labels2);
        return new DataSet(mergedFeatures, mergedLabels);
    }

    @Override
    public void reset()
    {
        iterator1.reset();
        iterator2.reset();
    }

    @Override
    public boolean resetSupported()
    {
        return true;
    }

    @Override
    public boolean asyncSupported()
    {
        return false;
    }

    @Override
    public int batch()
    {
        return iterator1.batch() + iterator2.batch();
    }

    @Override
    public void setPreProcessor(DataSetPreProcessor preProcessor)
    {
        iterator1.setPreProcessor(preProcessor);
        iterator2.setPreProcessor(preProcessor);
    }

    @Override
    public DataSetPreProcessor getPreProcessor()
    {
        return iterator1.getPreProcessor();
    }

    @Override
    public int inputColumns()
    {
        return iterator1.inputColumns() + iterator2.inputColumns();
    }

    @Override
    public int totalOutcomes()
    {
        return iterator1.totalOutcomes() + iterator2.totalOutcomes();
    }

    @Override
    public DataSet next(int num)
    {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public List<String> getLabels()
    {
        throw new UnsupportedOperationException("Not implemented");
    }
}
