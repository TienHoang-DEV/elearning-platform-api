package vn.edu.fpt.service.quizservice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.entity.QuizQuestion;
import vn.edu.fpt.repository.QuizQuestionRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizQuestionService {
    private final QuizQuestionRepository repository;

    public QuizQuestionService(QuizQuestionRepository quizQuestionRepository) {
        this.repository = quizQuestionRepository;
    }

    public List<QuizQuestion> findAll() { return repository.findAll(); }
    public Optional<QuizQuestion> findById(Integer id) { return repository.findById(id); }
    public QuizQuestion save(QuizQuestion entity) { return repository.save(entity); }
    public void deleteById(Integer id) { repository.deleteById(id); }
    public boolean existsById(Integer id) { return repository.existsById(id); }
}
