package vn.edu.fpt.service.quiz;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.dto.quizdto.QuizAttemptDTO;
import vn.edu.fpt.entity.QuizAttempt;
import vn.edu.fpt.mapper.DtoMapper;
import vn.edu.fpt.repository.QuizAttemptRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class QuizAttemptService {
    private final QuizAttemptRepository repository;
    private final DtoMapper dtoMapper;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository, DtoMapper dtoMapper)
    {
        this.repository = quizAttemptRepository;
        this.dtoMapper = dtoMapper;
    }

    public List<QuizAttempt> findAll() {
        return repository.findAll();
    }

    public Optional<QuizAttempt> findById(Integer id) {
        return repository.findById(id);
    }

    public QuizAttempt save(QuizAttempt entity) {
        return repository.save(entity);
    }

    public void deleteById(Integer id) {
        repository.deleteById(id);
    }

    public boolean existsById(Integer id) {
        return repository.existsById(id);
    }

    public List<QuizAttempt> findAttemptsByUserAndQuiz(Integer userId, Integer quizId) {
        return repository.findByUserIdAndQuizIdOrderBySubmittedAtDesc(userId, quizId);
    }

    public Page<QuizAttemptDTO> getAllAttemptByQuizId(
            Integer quizId,
            int page,
            int size,
            String sortBy,
            String searchKeyword,
            String status // <-- Nhận thêm parameter status từ Controller
    ) {
        Sort sort;
        switch (sortBy != null ? sortBy : "") {
            case "scoreDesc":
                sort = Sort.by("score").descending();
                break;
            case "scoreAsc":
                sort = Sort.by("score").ascending();
                break;
            case "startedAtAsc":
                sort = Sort.by("startedAt").ascending();
                break;
            default:
                sort = Sort.by("submittedAt").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<QuizAttempt> attempts;

        // Xử lý chuyển đổi String status -> Boolean passed
        Boolean passedStatus = null;
        if ("passed".equalsIgnoreCase(status)) {
            passedStatus = true;
        } else if ("failed".equalsIgnoreCase(status)) {
            passedStatus = false;
        }

        boolean hasKeyword = (searchKeyword != null && !searchKeyword.trim().isEmpty());
        boolean hasStatus = (passedStatus != null);

        // Rẽ 4 nhánh điều kiện để gọi Repo tương ứng
        if (hasKeyword && hasStatus) {
            attempts = repository.searchAttemptsWithStatus(quizId, searchKeyword.trim(), passedStatus, pageable);
        } else if (hasKeyword) {
            attempts = repository.searchAttempts(quizId, searchKeyword.trim(), pageable);
        } else if (hasStatus) {
            attempts = repository.findAllByQuizIdAndPassed(quizId, passedStatus, pageable);
        } else {
            attempts = repository.findAllByQuizId(quizId, pageable);
        }

        List<QuizAttemptDTO> dtos = new ArrayList<>();
        for (QuizAttempt attempt : attempts.getContent()) {
            dtos.add(dtoMapper.toQuizAttemptDto(attempt));
        }

        return new PageImpl<>(dtos, attempts.getPageable(), attempts.getTotalElements());
    }
}
