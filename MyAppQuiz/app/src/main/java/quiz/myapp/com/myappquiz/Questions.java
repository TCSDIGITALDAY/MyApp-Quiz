package quiz.myapp.com.myappquiz;

import java.util.Map;
import quiz.myapp.com.myappquiz.QuizContainer;

/**
 * Created by venkatesh on 1/7/2018.
 */

public class Questions implements QuestionsAdapter {

    private String question;
    private Map<String, Boolean> answers;

    public Questions()
    {

    }
    public Questions(String question,Map<String,Boolean> answerlist)
    {
        setQuestion(question);
        setAnswers(answerlist);
    }
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Map<String, Boolean> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<String, Boolean> answers) {
        this.answers = answers;
    }

    @Override
    public QuizContainer QuestionWrapper(int qNo) {
        QuizContainer quizData=new QuizContainer();
        quizData.setQuestionNo(String.valueOf(qNo));
        quizData.setQuestion(question);
        int i = 0;
        for (Map.Entry<String, Boolean> pair : answers.entrySet()) {
            //i += pair.getKey() + pair.getValue();
            i++;
            boolean result=pair.getValue();
            if(result)
            {
                quizData.setAnswer(String.valueOf(i));
            }

            switch (i) {
                case 1:
                quizData.setOption1(pair.getKey());
                break;
                case 2:
                    quizData.setOption2(pair.getKey());
                    break;
                case 3:
                    quizData.setOption3(pair.getKey());
                    break;
                case 4:
                    quizData.setOption4(pair.getKey());
                    break;
            }

        }
    return quizData;
    }
}
