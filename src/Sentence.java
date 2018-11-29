import java.util.ArrayList;

public class Sentence {
    private ArrayList<Word> wordList;
    private int length;

    public Sentence() {
        this.length = 0;
        this.wordList = new ArrayList<Word>();
    }

    public Sentence(String sentence) {
        this.wordList = new ArrayList<>();
        for (int i = 0; i < sentence.length(); i++) {
            String value = sentence.charAt(i) + "";
            Word temp = new Word(value, "");
            wordList.add(temp);
            length++;
        }
    }

    public void addWord(Word word) {
        this.wordList.add(word);
        this.length++;
    }

    public int getLength() {
        return length;
    }

    public String getGoalState() {
        String goalState = "";
        for (int i = 0; i < wordList.size(); i++) {
            goalState += wordList.get(i).getTag();
        }
        return goalState;
    }

    public ArrayList<String> getWordTemplate(int index, ArrayList gram) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < gram.size(); i++) {
            ArrayList oneTemplate = (ArrayList) gram.get(i);
            String oneKey = "";
            String value = "";
            if (oneTemplate.size() == 1) {
                int num = (int) oneTemplate.get(0);
                if (index + num < 0 || index + num >= length) {
                    value = " ";
                } else {
                    value= this.wordList.get(index + num).getValue();
                }

            } else if (oneTemplate.size() == 2) {
                int num1 = (int) oneTemplate.get(0);
                int num2 = (int) oneTemplate.get(1);
                if (index + num1 < 0 || index + num2 < 0 || index + num1 >= length || index + num2 >= length) {
                    value = "  ";
                } else {
                    value = this.wordList.get(index + num1).getValue() + this.wordList.get(index + num2).getValue();
                }
            }
            String si = i + "";
            oneKey = si + oneKey + value;
            result.add(oneKey);
        }
        return result;
    }
}

