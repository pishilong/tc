import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;

import java.io.*;


public class TCMain {

    public static void main(String[] args) throws Exception {
        File trainDirector = new File("dataset/train");
        File testDirector = new File("dataset/test");

        /*
        重新组织数据集目录
        Weka需要的组织形式为
        Directory
            Class 1(Subdirectory)
                File 1
                File 2
            .....
         */
        Util.refactorDataDirector(trainDirector);


        // convert the directory into a dataset
        TextDirectoryLoader loader = new TextDirectoryLoader();
        loader.setDirectory(new File(args[0]));
        Instances dataRaw = loader.getDataSet();
        //System.out.println("\n\nImported data:\n\n" + dataRaw);

        // apply the StringToWordVector
        // (see the source code of setOptions(String[]) method of the filter
        // if you want to know which command-line option corresponds to which
        // bean property)
        StringToWordVector filter = new StringToWordVector();
        filter.setInputFormat(dataRaw);
        Instances dataFiltered = Filter.useFilter(dataRaw, filter);
        //System.out.println("\n\nFiltered data:\n\n" + dataFiltered);

        // train J48 and output model
        J48 classifier = new J48();
        classifier.buildClassifier(dataFiltered);
        System.out.println("\n\nClassifier model:\n\n" + classifier);
    }
}