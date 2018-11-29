
public class main {
    public static void main(String[] args) {
        String template = "data/template.utf8";
        String trainData = "data/train.utf8";
        CRF crf = new CRF(trainData,template);
        crf.train(1);
        crf.calculateAccuracy();
        crf.test();
        //readfile(template);

    }
}
