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

    public static void refactorDataDirector(File rawDirectory) throws Exception{
        String directoryName = rawDirectory.getName();
        String datasetPath = "/Users/pishilong/Workspace/tc/dataset/";
        Map<Integer, Integer> labelInfo;

        File resultDirectory = new File(datasetPath + directoryName + "_weka");

        if(resultDirectory.exists()){
            resultDirectory.delete();
        }
        resultDirectory.mkdir();

        //读取label文件，获取每个文档的ID，及其对应的类型
        File labelFile = new File(datasetPath + directoryName + ".doc.label");
        if(!labelFile.exists()){
            System.out.print("标签文件不存在");
            return ;
        }

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
