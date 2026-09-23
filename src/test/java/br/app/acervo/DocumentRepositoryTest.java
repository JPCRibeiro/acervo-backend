package br.app.acervo;

import br.app.acervo.document.domain.Document;
import br.app.acervo.document.domain.DocumentStatus;
import br.app.acervo.document.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
public class DocumentRepositoryTest {
    @Autowired
    private DocumentRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void shouldSaveGenerateVersion7IdAndRetrieveDocument() {
        Document doc = Document.create(UUID.randomUUID(), "relatorio.pdf", "tenant/relatorio.pdf");

        Document saved = repository.save(doc);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId().version()).isEqualTo(7);

        em.flush();
        em.clear();

        Document retrieved = repository.findById(saved.getId()).orElseThrow();
        assertThat(retrieved.getFileName()).isEqualTo("relatorio.pdf");
        assertThat(retrieved.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(retrieved.getUploadedAt()).isNotNull();
    }
}
