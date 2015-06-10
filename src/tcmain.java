import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;

import java.io.*;


public class TCMain {

    public static void main(String[] args) throws Exception {
        File trainDirector = new File("/Users/pishilong/Workspace/tc/dataset/train");
        File testDirector = new File("/Users/pishilong/Workspace/tc/dataset/test");

        //预处理数据
        Instances dataFiltered = Util.preProcess(trainDirector);

        // train J48 and output model
        J48 classifier = new J48();
        classifier.buildClassifier(dataFiltered);
        //System.out.println("\n\nClassifier model:\n\n" + classifier);
    }
}