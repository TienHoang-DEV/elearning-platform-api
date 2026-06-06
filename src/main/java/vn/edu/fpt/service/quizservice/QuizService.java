package vn.edu.fpt.service.quizservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.dto.quizdto.QuizQuestionDTO;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.exception.ResourceNotFoundException;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.QuizRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {
    private final QuizRepository repository;
    private final DtoMapper dtoMapper;

    public QuizService(QuizRepository quizRepository, DtoMapper dtoMapper) {
        this.repository = quizRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<Quiz> findAll() {
        return repository.findAll();
    }

    public Optional<Quiz> findById(Integer id) {
        return repository.findById(id);
    }

    public Quiz save(Quiz entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public QuizDTO findQuizDTOByLessonId(Integer lessonId) {
        Quiz quiz = repository.findByLessonId(lessonId);
        if (quiz == null) {
            throw new ResourceNotFoundException("Quiz for lesson id " + lessonId + " not found");
        }
        return toQuizDto(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuizDTO> toQuizDtos(Collection<Quiz> quizzes) {
        if (quizzes == null || quizzes.isEmpty()) {
            return List.of();
        }

        return quizzes.stream()
                .map(this::toQuizDto)
                .sorted(Comparator.comparing(QuizDTO::getId, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizDTO toQuizDto(Quiz quiz) {
        QuizDTO quizDTO = dtoMapper.toQuizDto(quiz);
        if (quizDTO.getQuestions() == null || quizDTO.getQuestions().isEmpty()) {
            quizDTO.setQuestions(List.of());
            return quizDTO;
        }

        quizDTO.setQuestions(
                quizDTO.getQuestions().stream()
                        .sorted(Comparator
                                .comparing(QuizQuestionDTO::getPosition, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(QuizQuestionDTO::getId, Comparator.nullsLast(Integer::compareTo)))
                        .peek(questionDTO -> {
                            if (questionDTO.getAnswers() == null || questionDTO.getAnswers().isEmpty()) {
                                questionDTO.setAnswers(List.of());
                                return;
                            }

                            questionDTO.setAnswers(questionDTO.getAnswers().stream()
                                    .sorted(Comparator.comparing(answer -> answer.getId(), Comparator.nullsLast(Integer::compareTo)))
                                    .collect(Collectors.toList()));
                        })
                        .collect(Collectors.toList())
        );
        return quizDTO;
    }

    public int totalQuestion(List<QuizDTO> quizzes) {
        int total = 0;
        for (QuizDTO quizDTO : quizzes) {
            if (quizDTO.getQuestions() == null || quizDTO.getQuestions().isEmpty()) {
                continue;
            }
            total += quizDTO.getQuestions().size();
        }
        return total;
    }

}
