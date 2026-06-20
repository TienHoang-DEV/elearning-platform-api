package vn.edu.fpt.service.quiz;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.quizdto.QuizDTO;
import vn.edu.fpt.entity.Quiz;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.QuizRepository;

import java.util.*;

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

    public int totalQuestion(Set<QuizDTO> quizzes) {
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
