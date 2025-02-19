import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DocumentManager {
    private final Map<String, Document> storage = new HashMap<>();

    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document = Document.builder()
                    .id(UUID.randomUUID().toString())
                    .title(document.getTitle())
                    .content(document.getContent())
                    .author(document.getAuthor())
                    .created(Instant.now())
                    .build();
        }
        storage.put(document.getId(), document);
        return document;
    }
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> request.getTitlePrefixes() == null || request.getTitlePrefixes().stream().anyMatch(doc.getTitle()::startsWith))
                .filter(doc -> request.getContainsContents() == null || request.getContainsContents().stream().anyMatch(doc.getContent()::contains))
                .filter(doc -> request.getAuthorIds() == null || request.getAuthorIds().contains(doc.getAuthor().getId()))
                .filter(doc -> request.getCreatedFrom() == null || !doc.getCreated().isBefore(request.getCreatedFrom()))
                .filter(doc -> request.getCreatedTo() == null || !doc.getCreated().isAfter(request.getCreatedTo()))
                .collect(Collectors.toList());
    }

    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }

    public static void main(String[] args) {
        DocumentManager manager = new DocumentManager();
        Author author = Author.builder().id("1").name("Thomas Tom").build();
        Document document = Document.builder().title("Document").content("Document content").author(author).build();

        Document savedDocument = manager.save(document);
        System.out.println("Saved Document: " + savedDocument);

        Optional<Document> foundDocument = manager.findById(savedDocument.getId());
        System.out.println("Found Document: " + foundDocument.orElse(null));

        SearchRequest searchRequest = SearchRequest.builder().titlePrefixes(Collections.singletonList("Document")).build();
        List<Document> searchResults = manager.search(searchRequest);
        System.out.println("Search Results: " + searchResults);
    }
}
