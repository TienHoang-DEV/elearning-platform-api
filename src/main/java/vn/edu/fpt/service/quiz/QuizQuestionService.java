package vn.edu.fpt.service.quiz;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.quizdto.QuizAnswerDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.entity.QuizAnswer;
import vn.edu.fpt.entity.QuizQuestion;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.QuizQuestionRepository;
import vn.edu.fpt.repository.QuizRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizQuestionService {
    private final QuizQuestionRepository repository;
    private final QuizRepository quizRepository;
    private final DtoMapper dtoMapper;
    public QuizQuestionService(QuizQuestionRepository quizQuestionRepository,
                               QuizRepository quizRepository,
                               DtoMapper dtoMapper) {
        this.repository = quizQuestionRepository;
        this.quizRepository = quizRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<QuizQuestion> findAll() {
        return repository.findAll();
    }

    public Optional<QuizQuestion> findById(Integer id) {
        return repository.findById(id);
    }

    public QuizQuestion save(QuizQuestion entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public void createQuestion(QuizQuestionDTO dto, Integer quizId) {

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow();

        Integer maxPosition =
                repository.findMaxPositionByQuizId(quizId);

        QuizQuestion question = new QuizQuestion();

        question.setQuiz(quiz);
        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setPoints(dto.getPoints());
        question.setExplanation(dto.getExplanation());

        question.setPosition(
                maxPosition == null ? 1 : maxPosition + 1
        );

        for (int i = 0; i < dto.getAnswers().size(); i++) {

            QuizAnswerDTO answerDTO = dto.getAnswers().get(i);

            QuizAnswer answer = new QuizAnswer();

            answer.setAnswerText(answerDTO.getAnswerText());
            answer.setCorrect(Boolean.TRUE.equals(answerDTO.getCorrect()));

            answer.setPosition(i + 1);

            question.addAnswer(answer);
        }

        repository.save(question);
    }

    public void updateQuestion(QuizQuestionDTO dto) {

        QuizQuestion question = repository
                .findById(dto.getId())
                .orElseThrow();

        question.setQuestionText(dto.getQuestionText());
        question.setQuestionType(dto.getQuestionType());
        question.setPoints(dto.getPoints());
        question.setExplanation(dto.getExplanation());

        question.getAnswers().clear();

        for (int i = 0; i < dto.getAnswers().size(); i++) {

            QuizAnswerDTO answerDTO = dto.getAnswers().get(i);

            QuizAnswer answer = new QuizAnswer();

            answer.setAnswerText(answerDTO.getAnswerText());
            answer.setCorrect(Boolean.TRUE.equals(answerDTO.getCorrect()));

            answer.setPosition(i + 1);

            question.addAnswer(answer);
        }

        repository.save(question);
    }

    public void saveQuestion(QuizQuestionDTO quizQuestionDto, Integer quizId){
        if(quizQuestionDto.getId() == null){
            createQuestion(quizQuestionDto, quizId);
        }
        else{
            updateQuestion(quizQuestionDto);
        }
    }

    public Page<QuizQuestionDTO> getQuestionsByQuizId(Integer quizId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<QuizQuestion> quizQuestionPage =
                repository.findAllByQuizIdOrderByPositionAsc(quizId, pageable);

        List<QuizQuestionDTO> dtos = new ArrayList<>();
        for (QuizQuestion quizQ : quizQuestionPage.getContent()){
            dtos.add(dtoMapper.toQuizQuestionDto(quizQ));
        }

        return new PageImpl<>(
                dtos,
                quizQuestionPage.getPageable(),
                quizQuestionPage.getTotalElements()
        );
    }

    public void deleteQuestion(Integer questionId){

        QuizQuestion question =
                repository.findQuizQuestionById(questionId);

        if(question == null){
            return;
        }

        Integer quizId = question.getQuiz().getId();
        Integer position = question.getPosition();

        repository.delete(question);

        repository.decreasePositionsAfter(
                quizId,
                position
        );
    }

    public Integer copyQuestion(Integer questionId){

        QuizQuestion original =
                repository.findQuizQuestionById(questionId);

        if(original == null){
            return null;
        }

        Integer newPosition =
                original.getPosition() + 1;

        repository.increasePositionsAfter(
                original.getQuiz().getId(),
                original.getPosition()
        );

        QuizQuestion copy = new QuizQuestion();

        copy.setQuiz(original.getQuiz());
        copy.setQuestionText(original.getQuestionText());
        copy.setQuestionType(original.getQuestionType());
        copy.setPoints(original.getPoints());
        copy.setExplanation(original.getExplanation());

        copy.setPosition(newPosition);

        for(QuizAnswer answer : original.getAnswers()){

            QuizAnswer answerCopy = new QuizAnswer();

            answerCopy.setAnswerText(answer.getAnswerText());
            answerCopy.setCorrect(answer.getCorrect());
            answerCopy.setPosition(answer.getPosition());

            copy.addAnswer(answerCopy);
        }

        repository.save(copy);

        return newPosition;
    }

    public QuizQuestionDTO findQuizQuestionById(Integer quizQuestionId){
        return dtoMapper.toQuizQuestionDto(repository.findQuizQuestionById(quizQuestionId));
    }

    public Integer getTotalQuestionsByQuizId(Integer quizId){
        return repository.countByQuizId(quizId);
    }


}
