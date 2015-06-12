import weka.classifiers.Classifier;
import weka.classifiers.UpdateableClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.core.*;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.core.stopwords.StopwordsHandler;
import weka.core.stopwords.WordsFromFile;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.channels.*;
/**
 * Created by pishilong on 15/6/10.
 */
public class Util {
    public static void fileCopy(File s, File t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(s);
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fi.close();
                in.close();
                fo.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /*
    * 读取label文件
    * Map<DocumentID, LabelID>
    * */
    public static Map<Integer, Integer> loadLabelFile(File labelFile) throws FileNotFoundException{
        System.out.print("加载标签文件" + labelFile.getName());
        Map<Integer, Integer> labelInfo = new HashMap<Integer, Integer>();
        BufferedReader reader = new BufferedReader(new FileReader(labelFile));
        String tempString = null;
        try {
            while ((tempString = reader.readLine()) != null) {
                Integer docID = Integer.parseInt(tempString.split("\t")[0]);
                Integer labelID = Integer.parseInt(tempString.split("\t")[1]);
                labelInfo.put(docID, labelID);
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return labelInfo;
    }

    /*
    Weka需要的组织形式为
            Directory
    Class 1(Subdirectory)
    File 1
    File 2
      */
    public static File refactorDataDirector(File rawDirectory) throws Exception{
        String directoryName = rawDirectory.getName();
        String datasetPath = "/Users/pishilong/Workspace/tc/dataset/";
        Map<Integer, Integer> labelInfo;

        File resultDirectory = new File(datasetPath + directoryName + "_weka");

        if(resultDirectory.exists()){
            System.out.println("数据集" + directoryName + "已转换为weka格式，跳过");
            return resultDirectory;
        }
        resultDirectory.mkdir();

        //读取label文件，获取每个文档的ID，及其对应的类型
        File labelFile = new File(datasetPath + directoryName + ".doc.label");
        if(!labelFile.exists()){
            System.out.println("标签文件不存在");
            return null;
        }

        System.out.print("开始转换" + directoryName + "为weka格式");

        labelInfo = loadLabelFile(labelFile);

        File[] dataFiles = rawDirectory.listFiles();
        for (File file : dataFiles) {
            String fileName = file.getName();
            Integer docID = Integer.parseInt(fileName);
            Integer labelID = labelInfo.get(docID);
            File subDir = new File(resultDirectory.getAbsolutePath() + "/" + labelID);
            if(!subDir.exists()){
                subDir.mkdir();
            }
            File newFile = new File(subDir.getAbsolutePath() + "/" + fileName);
            newFile.createNewFile();
            fileCopy(file, newFile);
        }

        System.out.print(directoryName + "数据转换结束，一共" + dataFiles.length + "个文件");
        return resultDirectory;

    }

    public static Instances getWekaInstances(File dataDir) throws Exception{
        System.out.println("把数据集" + dataDir.getName() + "转换为weka instances");
        //weka装载数据
        Instances result;
        TextDirectoryLoader loader = new TextDirectoryLoader();
        loader.setDirectory(dataDir);
        result = loader.getDataSet();

        return result;
    }


    public static StringToWordVector getIDFFilter() {
        // 把文本映射到向量空间
        StringToWordVector filter = new StringToWordVector();
        filter.setIDFTransform(true);
        filter.setTFTransform(false);
        filter.setWordsToKeep(1000);
        filter.setDoNotOperateOnPerClassBasis(true);
        filter.setOutputWordCounts(true);
        filter.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL, StringToWordVector.TAGS_FILTER));
        File stopWordFile = new File("/Users/pishilong/Workspace/tc/dataset/stopword.txt");
        WordsFromFile stopwordsHandler = new WordsFromFile();
        stopwordsHandler.setStopwords(stopWordFile);
        filter.setStopwordsHandler(stopwordsHandler);
        return filter;
    }

    public static FilteredClassifier getClassifier(String modelName) throws Exception{
        FilteredClassifier filteredClassifier = new FilteredClassifier();
        filteredClassifier.setFilter(Util.getIDFFilter());


        Classifier classifier;
        switch(modelName){
            case "knn":
                classifier = new IBk(8);
                break;
            case "nb":
                classifier = new NaiveBayes();
                break;
            case "svm":
                classifier = new SMO();
                break;
            default:
                classifier = new SMO();
                break;
        }

        filteredClassifier.setClassifier(classifier);

        return filteredClassifier;
    }

    // typeName : single, ensemble
    // modelName : svm, "svm, knn..."
    public static Classifier buildClassifier(String typeName, String modelName, Instances trainData, Instances extraData) throws Exception{
        System.out.println("开始构建分类器:" + typeName + "\t" + modelName);
        if(typeName == "single"){
            Classifier classifier = getClassifier(modelName);
            classifier = trainWithExtraData(classifier, trainData, extraData);
            return classifier;
        }else{
            String[] modelNames = modelName.split(",");
            Classifier[] cfsArray = new Classifier[modelNames.length];
            int index = 0;
            for(String name : modelNames){
                cfsArray[index] = Util.getClassifier(name);
                index ++ ;
            }
            Vote vote = new Vote();
           /*
            * 订制ensemble分类器的决策方式主要有：
            * AVERAGE_RULE
            * PRODUCT_RULE
            * MAJORITY_VOTING_RULE
            * MIN_RULE
            * MAX_RULE
            * MEDIAN_RULE
            * 它们具体的工作方式，大家可以参考weka的说明文档。
            * 在这里我们选择的是多数投票的决策规则
            */
            vote.setCombinationRule(new SelectedTag(Vote.MAJORITY_VOTING_RULE, Vote.TAGS_RULES));
            vote.setClassifiers(cfsArray);
            //设置随机数种子
            vote.setSeed(2);
            vote = (Vote)trainWithExtraData(vote, trainData, extraData);
            //训练ensemble分类器
            return vote;
        }
    }

    public static Classifier trainWithExtraData(Classifier classifier, Instances trainData, Instances extraData) throws Exception {
        System.out.println("开始训练初始分类器");
        classifier.buildClassifier(trainData);
        /* 每次都重新训练分类器
        int index = 1;
        for(Instance data : extraData){
            double label = classifier.classifyInstance(data);
            data.setClassValue(label);
            trainData.add(data);
            System.out.println("利用第" + index + "个无标数据的分类结果重新训练分类器");
            classifier.buildClassifier(trainData);
            index ++;
        }
        System.out.println("分类器训练完毕");
        */
        //只最后重新训练一次
        int index = 1;
        for(Instance data : extraData){
            double label = classifier.classifyInstance(data);
            data.setClassValue(label);
            trainData.add(data);
            //System.out.println("分类第" + index + "个无标数据，并将其加入训练数据中");
            index ++;
        }
        System.out.println("无标数据分类完毕，并添加入训练数据中");
        classifier.buildClassifier(trainData);
        System.out.println("分类器重新训练完毕");
        return classifier;
    }

    public static void main(String[] args) throws Exception {
        // 测试loadLableFile
        //File labelFile = new File("/Users/pishilong/Workspace/tc/dataset/train.doc.label");
        //loadLabelFile(labelFile);
        // 测试refactorDataDirector
        File trainDirector = new File("/Users/pishilong/Workspace/tc/dataset/train");
        refactorDataDirector(trainDirector);
    }
}
