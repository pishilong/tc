import weka.core.*;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stopwords.StopwordsHandler;
import weka.core.stopwords.WordsFromFile;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

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
            System.out.println("数据集已转换为weka格式，跳过");
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

    public static Instances getWekaInstances(File rawDir) throws Exception{
        //重新组织数据集目录
        File dataDir = refactorDataDirector(rawDir);

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

    public static void main(String[] args) throws Exception {
        // 测试loadLableFile
        //File labelFile = new File("/Users/pishilong/Workspace/tc/dataset/train.doc.label");
        //loadLabelFile(labelFile);
        // 测试refactorDataDirector
        File trainDirector = new File("/Users/pishilong/Workspace/tc/dataset/train");
        refactorDataDirector(trainDirector);
    }
}
