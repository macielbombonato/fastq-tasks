package bombonato.fastq.business.service;

import bombonato.fastq.data.model.FastqSequence;

public interface FastqSequenceService {

    FastqSequence save(FastqSequence entity);

    Long calculateSize(String sequence);

    FastqSequence calculateScore(
            FastqSequence entity,
            Double minQual,
            Double minQualPerc
    );

    boolean sanitize(
            int bufferSize,
            String r1,
            String r2,
            long minSize,
            double minQual,
            double minQualPerc,
            boolean skipDuplicates,
            boolean skipSimilar,
            boolean skipSize,
            boolean skipLowQuality,
            boolean skipProgressBar
    );

    void removeDirectory(String filename);

}
