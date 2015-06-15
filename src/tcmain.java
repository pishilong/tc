import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.misc.SerializedClassifier;
import weka.core.*;
import weka.classifiers.meta.Vote;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.*;
import weka.classifiers.Evaluation;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Normalize;

import java.io.*;
import java.util.ArrayList;


public class TCMain {

    public static void main(String[] args) throws Exception {
        File trainDir = new File("/Users/pishilong/Workspace/tc/dataset/train");
        //重新组织数据集目录
        trainDir = Util.refactorDataDirector(trainDir);
        File testDir = new File("/Users/pishilong/Workspace/tc/dataset/test");
        //重新组织数据集目录
        testDir = Util.refactorDataDirector(testDir);

        BufferedReader reader = new BufferedReader(
                new FileReader("/Users/pishilong/Workspace/tc/dataset/extra.arff"));



        //预处理数据
        Instances trainData = Util.getWekaInstances(trainDir);
        Instances testData = Util.getWekaInstances(testDir);
        Instances extraData = new Instances(new BufferedReader(
                new FileReader("/Users/pishilong/Workspace/tc/dataset/extra.arff")));

        trainData.setClassIndex(trainData.numAttributes() - 1);
        testData.setClassIndex(testData.numAttributes() - 1);
        //extraData.setClassIndex(extraData.numAttributes() - 1);
        Filter idfFilter = Util.getIDFFilter();
        idfFilter.setInputFormat(trainData);
        trainData = Filter.useFilter(trainData, idfFilter);
        testData = Filter.useFilter(testData, idfFilter);
        idfFilter.setInputFormat(extraData);
        extraData = Filter.useFilter(extraData, idfFilter);

        AttributeSelection featureSelector = Util.getReduceDimFilter("CA");
        featureSelector.setInputFormat(trainData);
        trainData = Filter.useFilter(trainData, featureSelector);
        testData = Filter.useFilter(testData, featureSelector);
        featureSelector.setInputFormat(extraData);
        extraData = Filter.useFilter(extraData, featureSelector);
        Normalize normalize = new Normalize();
        normalize.setInputFormat(trainData);
        trainData = Filter.useFilter(trainData, normalize);
        testData = Filter.useFilter(testData, normalize);
        normalize.setInputFormat(extraData);
        extraData = Filter.useFilter(extraData, normalize);

        //最终需要把test数据集也加入训练数据之中
       /*
        for (Instance instance : testData){
            trainData.add(instance);
        }
        */

        //构造分类器
        ArrayList<Classifier> classifiers = new ArrayList<Classifier>();
        classifiers.add(Util.buildClassifier("single", "svm", trainData, extraData));
        //classifiers.add(Util.buildClassifier("single", "knn", trainData, extraData));
        //classifiers.add(Util.buildClassifier("single", "nb", trainData, extraData));
        //classifiers.add(Util.buildClassifier("single", "j48", trainData, extraData));
        //classifiers.add(Util.buildClassifier("ensemble", "knn,nb,svm", trainData, extraData));
        //classifiers.add(Util.buildClassifier("ensemble", "knn,nb,j48", trainData, extraData));
        //classifiers.add(Util.buildClassifier("AdaBoost", "svm", trainData, extraData));
        //classifiers.add(Util.buildClassifier("Bagging", "svm", trainData, extraData));


        //评估
        int index = 1;
        for(Classifier classifier : classifiers) {
            System.out.println("开始评估测试数据分类情况");
            //SerializationHelper.write("/Users/pishilong/Workspace/tc/models/model_" + index, classifier);
            Evaluation evaluation = new Evaluation(trainData);
            evaluation.evaluateModel(classifier, testData);
            Util.printResult(classifier, trainData.classAttribute(), testData, "test_weka.doc.list");
            System.out.println(evaluation.toClassDetailsString());
            System.out.println("Macro F1: " + evaluation.unweightedMacroFmeasure());
            System.out.println("Micro F1: " + evaluation.unweightedMicroFmeasure());
        }
    }
}