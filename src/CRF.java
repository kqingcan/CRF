
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CRF {
    private ArrayList templateList = new ArrayList();
    private HashMap<String, Integer> funcs = new HashMap();
    private ArrayList<Sentence> sentens = new ArrayList<Sentence>();
    private ArrayList unigram = new ArrayList();
    private ArrayList bigram = new ArrayList();
    private String[] lables = {"B","E","I","S"} ;

    public CRF(String trainFile, String tempFile) {
        initTemplate(tempFile);
        initTrainData(trainFile);
    }

    public void train(int trainNum) {

        int sentensNum = sentens.size();
        sentensNum = 18753;
        for (int j = 0; j<trainNum ;j++){
            for (int i =0; i<sentensNum; i++){
                System.out.println(i);
                long startTime=System.nanoTime();
                Sentence currentSentence = sentens.get(i);
                String predict = viterbi(currentSentence);
                long endTime=System.nanoTime();
                update(currentSentence,predict);
                long end = System.nanoTime();
                System.out.println(currentSentence.getGoalState());
                System.out.println(predict);
                System.out.println("viterbi: "+ (endTime-startTime)+"  update: "+(end- endTime));
            }
        }

        System.out.println("Train Finished!");
    }

    public void calculateAccuracy(){
        int sentensNum = sentens.size();
        int sentensStart = 18753;
        int error = 0;
        int trueNum = 0;
        int totalNum = 0;
        for (int i = sentensStart; i<sentensNum;i++){
            System.out.println(i);
            Sentence currentSentence = sentens.get(i);
            String predict = viterbi(currentSentence);
            String goalState = currentSentence.getGoalState();
            totalNum += goalState.length();
            for (int j=0;j<goalState.length();j++){
                if ((predict.charAt(j)!=goalState.charAt(j))){
                    error++;
                }else {
                    trueNum++;
                }
            }
        }
        float accuracy = (float)trueNum/totalNum;
        System.out.println("Accuracy: " + accuracy);
        float errorRate = (float)error/totalNum;
        System.out.println("Error:  "+ errorRate);
    }

    public void test(){
        System.out.println("输入句子来测试： ");
        Scanner input = new Scanner(System.in);
        while (input.hasNext()){
            String line = input.nextLine();
            Sentence lineSen = new Sentence(line);
            String result = viterbi(lineSen);
            System.out.println("结果是：");
            System.out.println(result);
        }
    }


    private String viterbi(Sentence sentence) {
        int length = sentence.getLength();
        int[][] scoreBoard = new int[length][4];
        int[][] lableBoard = new int[length][4];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < 4; j++) {
                String currentState = lables[j];
                ArrayList<String> uniWordTemplate = sentence.getWordTemplate(i,this.unigram);
                for (int k = 0; k < uniWordTemplate.size(); k++) {
                    String currentKey = uniWordTemplate.get(k)+ currentState;
                    Integer val = funcs.get(currentKey);
                    if (val !=null){
                        scoreBoard[i][j] += val;
                    }
//                    if (funcs.containsKey(currentKey)) {
//                        //对于单个状态的可以直接加
//                        scoreBoard[i][j] += funcs.get(currentKey);
//                    }
                }
                //对于两个状态的要取前一个状态
                ArrayList<String> biWordTemplate = sentence.getWordTemplate(i, this.bigram);
                if (i == 0) {
                    for (int m = 0; m < biWordTemplate.size(); m++) {
                        String currentKey = biWordTemplate.get(m)+"N"+currentState;
                        Integer val = funcs.get(currentKey);
                        if (val !=null){
                            scoreBoard[i][j] += val;
                        }
//                        if (funcs.containsKey(currentKey)) {
//                            scoreBoard[i][j] += funcs.get(currentKey);
//                        }
                    }
                    continue;
                }
                int[] transScore = new int[4];
                for (int n = 0; n < 4; n++) {
                    transScore[n] = scoreBoard[i - 1][n];
                }
                int maxScore = -9999;
                int maxIndex = 0;
                for (int lastState = 0; lastState < 4; lastState++) {
                    String slastState = lables[lastState];
                    for (int m = 0; m < biWordTemplate.size(); m++) {
                        String currentKey = biWordTemplate.get(m) + slastState + currentState;
                         Integer val =  funcs.get(currentKey);
                        if (val !=null) {
                            transScore[lastState] += val;
                        }
                    }
                    if (transScore[lastState]>maxScore){
                        maxScore = transScore[lastState];
                        maxIndex =lastState;
                    }
                }
                scoreBoard[i][j] += maxScore;
                lableBoard[i][j] = maxIndex;
            }
        }
        int lastMaxIndex = 0;
        int lastMaxScore = -9999;
        for (int i=0;i<4;i++){
            if (scoreBoard[length-1][i] > lastMaxScore){
                lastMaxScore = scoreBoard[length-1][i];
                lastMaxIndex = i;
            }
        }
        //decode阶段
        int[] states = new int[length];
        states[length-1] = lastMaxIndex;
        for (int i = length-1; i>0;i--){
            states[i-1] = lableBoard[i][states[i]];
        }
        return parseStatesToString(states);
    }

    private void update(Sentence sentence, String resultState)  {
        String goalState = sentence.getGoalState();
        for (int i=0; i<goalState.length();i++){
            if (goalState.charAt(i) ==resultState.charAt(i)){
                continue;
            }
            String trueState = goalState.charAt(i) +"";
            String falseState = resultState.charAt(i)+"";
            ArrayList<String> uniWordTemplate = sentence.getWordTemplate(i,unigram);
            for (int j = 0; j<uniWordTemplate.size();j++){
                String currentKey = uniWordTemplate.get(j) + trueState;
                Integer val = funcs.get(currentKey);
                if (val == null){
                    funcs.put(currentKey,1);
                }else {
                    funcs.put(currentKey,val+1);
                }
                currentKey = uniWordTemplate + falseState;
                val = funcs.get(currentKey);
                if (val == null){
                    funcs.put(currentKey,-1);
                }else {
                    funcs.put(currentKey,val-1);
                }
            }
            ArrayList<String> biWordTemplate = sentence.getWordTemplate(i, bigram);
            String lastState = "";
            String flastState = "";
            if (i==0){
                lastState = "N";
                flastState = "N";
            }else {
                lastState = goalState.charAt(i-1)+"";
                flastState = resultState.charAt(i-1)+"";
            }
            for (int j=0;j<biWordTemplate.size();j++){
                String currentKey = biWordTemplate.get(j) + lastState + trueState;
                Integer val = funcs.get(currentKey);
                if (val == null){
                    funcs.put(currentKey,1);
                }else {
                    funcs.put(currentKey,val+1);
                }
                currentKey = biWordTemplate.get(j) + flastState + falseState;
                val = funcs.get(currentKey);
                if (val == null){
                    funcs.put(currentKey,-1);
                }else {
                    funcs.put(currentKey,val-1);
                }
            }
        }
    }

    private String parseStatesToString(int[] states){
        String result = "";
        for (int i=0; i<states.length;i++){
            result = result + lables[states[i]];
        }
        return result;
    }

    private void initTemplate(String fileName) {
        try {
            Scanner fileTemp = new Scanner(new File(fileName));
            //处理第一行没用的line
            String firstLine = fileTemp.nextLine();
            while (fileTemp.hasNext()) {
                String temp = fileTemp.nextLine();
                if (temp.equals("")) {
                    break;
                }
                String pattern = "\\[(\\-)?\\d,\\d\\]";
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(temp);
                ArrayList tempList = new ArrayList();
                while (matcher.find()) {
                    //    System.out.println(matcher.group(0));
                    String sTemp = matcher.group(0);
                    sTemp = sTemp.substring(1);
                    String[] xy = sTemp.split(",");
                    String x = xy[0];
                    int index = Integer.parseInt(x);
                    tempList.add(index);
                }
                this.unigram.add(tempList);
            }
            String unusedLine = fileTemp.nextLine();
            while (fileTemp.hasNext()) {
                String temp = fileTemp.nextLine();
                String pattern = "\\[(\\-)?\\d,\\d\\]";
                Pattern r = Pattern.compile(pattern);
                Matcher matcher = r.matcher(temp);
                ArrayList tempList = new ArrayList();
                while (matcher.find()) {
                    System.out.println(matcher.group(0));
                    String sTemp = matcher.group(0);
                    sTemp = sTemp.substring(1);
                    String[] xy = sTemp.split(",");
                    String x = xy[0];
                    int index = Integer.parseInt(x);
                    tempList.add(index);
                }
                this.bigram.add(tempList);
                if (temp.equals("")) {
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initTrainData(String fileName) {
        try {
            Scanner trainFile = new Scanner(new File(fileName));
            Sentence sentenceTemp = new Sentence();
            while (trainFile.hasNext()) {
                String temp = trainFile.nextLine();
                if (temp.equals("")) {
                    sentens.add(sentenceTemp);
                    sentenceTemp = new Sentence();
                } else {
                    String[] strings = temp.split(" ");
                    String value = strings[0];
                    String tag = strings[1];
                    // System.out.println(value);
                    sentenceTemp.addWord(new Word(value, tag));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
