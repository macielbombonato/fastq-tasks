package bombonato.fastq.data.model;

import bombonato.fastq.data.enums.Status;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "fastq_sequence")
public class FastqSequence implements Serializable {

//    @Id
//    @Column(name = "ID", nullable = false, unique = true)
//    private String id;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "seq_id_1", length = 4000, nullable = true)
    private String seqId1;

    @Column(name = "sequence_1", length = 4000, nullable = true)
    private String sequence1;

    @Column(name = "sequence_1_marked", length = 4000, nullable = true)
    private String sequence1Marked;

    @Column(name = "feature_1", length = 4000, nullable = true)
    private String feature1;

    @Column(name = "quality_1", length = 4000, nullable = true)
    private String quality1;

    @Column(name = "score_1", nullable = true)
    private Double score1;

    @Column(name = "size_1", nullable = true)
    private Long size1;

    @Column(name = "seq_id_2", length = 4000, nullable = true)
    private String seqId2;

    @Column(name = "sequence_2", length = 4000, nullable = true)
    private String sequence2;

    @Column(name = "sequence_2_marked", length = 4000, nullable = true)
    private String sequence2Marked;

    @Column(name = "feature_2", length = 4000, nullable = true)
    private String feature2;

    @Column(name = "quality_2", length = 4000, nullable = true)
    private String quality2;

    @Column(name = "score_2", nullable = true)
    private Double score2;

    @Column(name = "size_2", nullable = true)
    private Long size2;

    @Column(name = "status", nullable = false)
    private Status status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSeqId1() {
        return seqId1;
    }

    public void setSeqId1(String seqId1) {
        this.seqId1 = seqId1;
    }

    public String getSequence1() {
        return sequence1;
    }

    public void setSequence1(String sequence1) {
        this.sequence1 = sequence1;
    }

    public String getFeature1() {
        return feature1;
    }

    public void setFeature1(String feature1) {
        this.feature1 = feature1;
    }

    public String getQuality1() {
        return quality1;
    }

    public void setQuality1(String quality1) {
        this.quality1 = quality1;
    }

    public Double getScore1() {
        return score1;
    }

    public void setScore1(Double score1) {
        this.score1 = score1;
    }

    public Long getSize1() {
        return size1;
    }

    public void setSize1(Long size1) {
        this.size1 = size1;
    }

    public String getSeqId2() {
        return seqId2;
    }

    public void setSeqId2(String seqId2) {
        this.seqId2 = seqId2;
    }

    public String getSequence2() {
        return sequence2;
    }

    public void setSequence2(String sequence2) {
        this.sequence2 = sequence2;
    }

    public String getFeature2() {
        return feature2;
    }

    public void setFeature2(String feature2) {
        this.feature2 = feature2;
    }

    public String getQuality2() {
        return quality2;
    }

    public void setQuality2(String quality2) {
        this.quality2 = quality2;
    }

    public Double getScore2() {
        return score2;
    }

    public void setScore2(Double score2) {
        this.score2 = score2;
    }

    public Long getSize2() {
        return size2;
    }

    public void setSize2(Long size2) {
        this.size2 = size2;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getSequence1Marked() {
        return sequence1Marked;
    }

    public void setSequence1Marked(String sequence1Marked) {
        this.sequence1Marked = sequence1Marked;
    }

    public String getSequence2Marked() {
        return sequence2Marked;
    }

    public void setSequence2Marked(String sequence2Marked) {
        this.sequence2Marked = sequence2Marked;
    }
}
