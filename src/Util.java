import java.io.*;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by pishilong on 15/6/10.
 */
public class Util {
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
                // 显示行号
                Integer docID = Integer.parseInt(tempString.split("\t")[0]);
                Integer labelID = Integer.parseInt(tempString.split("\t")[1]);
                labelInfo.put(docID, labelID);
                System.out.println("docID: " + docID +  "\t labelID:" + labelID);
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

    public static void refactorDataDirector(File rawDirectory) throws FileNotFoundException{
        String directoryName = rawDirectory.getName();
        String datasetPath = "/Users/pishilong/Workspace/tc/dataset/";
        Map<Integer, Integer> labelInfo;

        File resultDirectory = new File(datasetPath + directoryName + "_weka");
        if(resultDirectory.exists()){
            System.out.print("文件夹已转换");
            return ;
        }
        //读取label文件，获取每个文档的ID，及其对应的类型
        File labelFile = new File(datasetPath + directoryName + ".doc.label");
        if(!labelFile.exists()){
            System.out.print("标签文件不存在");
            return ;
        }

        labelInfo = loadLabelFile(labelFile);

    }

    public static void main(String[] args) throws Exception {
        File labelFile = new File("/Users/pishilong/Workspace/tc/dataset/train.doc.label");
        loadLabelFile(labelFile);
    }
}
