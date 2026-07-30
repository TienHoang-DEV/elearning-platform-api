package vn.edu.fpt.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Service lọc từ thô tục / độc hại dựa trên Cấu trúc dữ liệu Cây Prefix Trie.
 * Tốc độ tìm kiếm O(N) với N là chiều dài đoạn văn bản.
 */
@Slf4j
@Service
public class ProfanityFilterService {

    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
        String word = null;
    }

    private final TrieNode root = new TrieNode();
    private int loadedWordCount = 0;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("bad-words.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase(Locale.ROOT);
                    if (!word.isEmpty() && !word.startsWith("#")) {
                        insert(word);
                        loadedWordCount++;
                    }
                }
            }
            log.info("Đã khởi tạo ProfanityFilterService (Cây Trie) thành công với {} từ cấm.", loadedWordCount);
        } catch (Exception e) {
            log.error("Không thể đọc danh sách từ cấm bad-words.txt: {}", e.getMessage(), e);
        }
    }

    private void insert(String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
        }
        current.isEndOfWord = true;
        current.word = word;
    }

    /**
     * Kiểm tra xem văn bản có chứa bất kỳ từ cấm nào hay không
     * @param text Bình luận / Feedback cần kiểm tra
     * @return true nếu phát hiện từ cấm
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.toLowerCase(Locale.ROOT);
        int length = normalized.length();

        for (int i = 0; i < length; i++) {
            TrieNode current = root;
            for (int j = i; j < length; j++) {
                char c = normalized.charAt(j);
                current = current.children.get(c);
                if (current == null) {
                    break;
                }
                if (current.isEndOfWord) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Che các từ thô tục bằng ký tự ***
     * @param text Nội dung văn bản
     * @return Văn bản đã được thay thế từ cấm
     */
    public String maskProfanity(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        String normalized = text.toLowerCase(Locale.ROOT);
        StringBuilder result = new StringBuilder(text);
        int length = normalized.length();

        for (int i = 0; i < length; i++) {
            TrieNode current = root;
            int matchEnd = -1;

            for (int j = i; j < length; j++) {
                char c = normalized.charAt(j);
                current = current.children.get(c);
                if (current == null) {
                    break;
                }
                if (current.isEndOfWord) {
                    matchEnd = j;
                }
            }

            if (matchEnd != -1) {
                for (int k = i; k <= matchEnd; k++) {
                    result.setCharAt(k, '*');
                }
                i = matchEnd; // Bỏ qua đoạn vừa che
            }
        }

        return result.toString();
    }
}
