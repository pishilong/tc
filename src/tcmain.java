import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.*;
import weka.filters.unsupervised.attribute.*;
import weka.classifiers.Evaluation;

import java.io.*;


public class TCMain {

    public static void main(String[] args) throws Exception {
        File trainDir = new File("/Users/pishilong/Workspace/tc/dataset/train");
        File testDir = new File("/Users/pishilong/Workspace/tc/dataset/test");

        //预处理数据
        Instances trainData = Util.getWekaInstances(trainDir);
        Instances testData = Util.getWekaInstances(testDir);

        FilteredClassifier classifier = new FilteredClassifier();
        classifier.setFilter(Util.getIDFFilter());
        Classifier knn = new SMO();
        classifier.setClassifier(knn);

        classifier.buildClassifier(trainData);

        Evaluation evaluation = new Evaluation(trainData);
        evaluation.evaluateModel(classifier, testData);
        System.out.println(evaluation.toClassDetailsString());
        System.out.println("Macro F1: " + evaluation.unweightedMacroFmeasure());
        System.out.println("Micro F1: " + evaluation.unweightedMicroFmeasure());
    }
}