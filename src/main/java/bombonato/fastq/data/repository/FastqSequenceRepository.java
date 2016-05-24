package bombonato.fastq.data.repository;

import bombonato.fastq.data.enums.Status;
import bombonato.fastq.data.model.FastqSequence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FastqSequenceRepository extends JpaRepository<FastqSequence, String> {

    long count();

    FastqSequence findByIdAndStatus(
            Long id,
            Status status
    );

    @Query("select f " +
            "from FastqSequence f ")
    Page<FastqSequence> findAll(
            Pageable page
    );

    @Query("select f " +
            "from FastqSequence f " +
            "where f.status = :status " +
            "order by f.id ")
    Page<FastqSequence> findByStatus(
            @Param("status") Status status,
            Pageable page
    );

    @Query("select f " +
            "from FastqSequence f " +
            "where f.sequence1 like :seq1 " +
            "  and f.id != :id " +
            "  and f.status = :status " +
            "order by f.id ")
    List<FastqSequence> findBySequence(
            @Param("id") Long id,
            @Param("seq1") String seq1,
            @Param("status") Status status
    );

    @Query("select f " +
            "from FastqSequence f " +
            "where ( " +
            " ( " +
            "       f.sequence1 like :seq1 " +
            "  and  f.sequence2 like :seq2 " +
            " ) " +
            "   or " +
            " (" +
            "       f.sequence1 like :seq2 " +
            "  and  f.sequence2 like :seq1 " +
            " ) " +
            " ) " +
            "  and f.id != :id " +
            "  and f.status = :status " +
            "order by f.id ")
    List<FastqSequence> findByTwoSequences(
            @Param("id") Long id,
            @Param("seq1") String seq1,
            @Param("seq2") String seq2,
            @Param("status") Status status
    );

}
