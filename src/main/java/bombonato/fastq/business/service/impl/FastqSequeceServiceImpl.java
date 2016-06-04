package bombonato.fastq.business.service.impl;

import bombonato.fastq.business.service.FastqSequenceService;
import bombonato.fastq.data.enums.Status;
import bombonato.fastq.data.model.FastqSequence;
import bombonato.fastq.data.repository.FastqSequenceRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

@Service("fastqSequenceService")
public class FastqSequeceServiceImpl implements FastqSequenceService {

    private static final Log log = LogFactory.getLog(FastqSequeceServiceImpl.class);

    private int bufferSize = 100;

    private boolean skipProgressBar = false;

    private ExecutorService progressbarExecutor;

    @Autowired
    private FastqSequenceRepository fastqSequenceRepository;


    @Override
    public FastqSequence save(FastqSequence entity) {
        return fastqSequenceRepository.save(entity);
    }

    @Override
    public Long calculateSize(String sequence) {

        Long result = 0L;

        if (sequence != null) {
            result = new Long(sequence.length());
        }

        return result;
    }

    @Override
    public FastqSequence calculateScore(
            FastqSequence entity,
            Double minQual,
            Double minQualPerc) {

        Double result = 0D;

        if (entity.getQuality1() != null
                && entity.getQuality1().length() > 0) {
            if (minQual == null) {
                minQual = 20D;
            }

            if (minQualPerc == null) {
                minQualPerc = 80D;
            }

            double above = 0;
            double below = 0;

            StringBuffer sequenceMarked = new StringBuffer();
            int index = 0;

            for (char c : entity.getQuality1().toCharArray()) {
                if ((c - 33) >= minQual) {
                    above++;

                    sequenceMarked.append(entity.getSequence1().charAt(index));
                } else {
                    below++;

                    sequenceMarked.append('_');
                }

                index++;
            }

            result = (above / entity.getQuality1().length()) * 100;

            entity.setScore1(result);

            if (below > 0) {
                entity.setSequence1Marked(sequenceMarked.toString());
            }
        }

        if (entity.getQuality2() != null
                && entity.getQuality2().length() > 0) {
            if (minQual == null) {
                minQual = 20D;
            }

            if (minQualPerc == null) {
                minQualPerc = 80D;
            }

            double above = 0;
            double below = 0;

            StringBuffer sequenceMarked = new StringBuffer();
            int index = 0;

            for (char c : entity.getQuality2().toCharArray()) {
                if ((c - 33) >= minQual) {
                    above++;

                    sequenceMarked.append(entity.getSequence2().charAt(index));
                } else {
                    below++;

                    sequenceMarked.append('_');
                }

                index++;
            }

            result = (above / entity.getQuality2().length()) * 100;

            entity.setScore2(result);

            if (below > 0) {
                entity.setSequence2Marked(sequenceMarked.toString());
            }
        }

        return entity;
    }

    @Override
    public boolean sanitize(
            int bufferSize,
            String r1,
            String r2,
            long minSize,
            int parallel,
            double minQual,
            double minQualPerc,
            boolean skipDuplicates,
            boolean skipSimilar,
            boolean skipSize,
            boolean skipLowQuality,
            boolean skipProgressBar) {

        this.progressbarExecutor = Executors.newWorkStealingPool(1);

        this.skipProgressBar = skipProgressBar;

        boolean result = false;

        File f1 = null;
        Scanner s1 = null;
        File f2 = null;
        Scanner s2 = null;

        boolean loadR1 = false;
        boolean loadR2 = false;

        try {
            if (r1 != null
                    && !"".equals(r1)) {
                f1 = new File(r1);
                s1 = new Scanner(f1);
                loadR1 = true;

                this.removeDirectory(r1);
            }

            if (r2 != null
                    && !"".equals(r2)) {
                f2 = new File(r2);
                s2 = new Scanner(f2);
                loadR2 = true;

                this.removeDirectory(r2);
            }

            if (loadR1) {
                List<FastqSequence> sequences = new ArrayList<FastqSequence>();
                int buffer = 0;
                int lineCount = 0;
                String line1 = null;
                String line2 = null;
                FastqSequence seq = null;

                long readedLength = 0;

                while (s1.hasNext()) {
                    line1 = s1.nextLine();

                    readedLength += line1.length();

                    if (loadR2) {
                        line2 = s2.nextLine();
                    }

                    if (lineCount == 0) {
                        seq = new FastqSequence();
                        seq.setSeqId1(line1);

                        if (loadR2) {
                            try {
                                seq.setSeqId2(line2);
                            } catch (Throwable ex) {
                                log.error("Error when try to read line from second file");
                                log.error(ex);
                            }

                        }

                        lineCount++;

                    } else if (lineCount == 1) {
                        seq.setSequence1(line1);

                        if (loadR2) {
                            seq.setSequence2(line2);
                        }

                        lineCount++;
                    } else if (lineCount == 2) {
                        seq.setFeature1(line1);

                        if (loadR2) {
                            seq.setFeature2(line2);
                        }

                        lineCount++;
                    } else if (lineCount == 3) {
                        seq.setQuality1(line1);

                        if (loadR2) {
                            seq.setQuality2(line2);
                        }

                        lineCount = 0;
                    } else {
                        seq = null;
                        lineCount = 0;
                    }

                    if (lineCount == 0
                            && seq != null) {
                        if (buffer < bufferSize) {
                            sequences.add(seq);
                            buffer++;
                        } else {
                            System.out.print("\n");

                            storeLoadedSequence(
                                    r1,
                                    r2,
                                    minSize,
                                    parallel,
                                    minQual,
                                    minQualPerc,
                                    loadR2,
                                    skipDuplicates,
                                    skipSize,
                                    skipLowQuality,
                                    sequences
                            );

                            buffer = 0;
                            sequences = new ArrayList<FastqSequence>();
                        }
                    }

                    updateProgress("Reading Source", new Double(readedLength), new Double(f1.length()));
                } // end while

                // Process remaning sequences
                if (sequences != null
                        && sequences.size() > 0) {
                    System.out.print("\n");

                    storeLoadedSequence(
                            r1,
                            r2,
                            minSize,
                            parallel,
                            minQual,
                            minQualPerc,
                            loadR2,
                            skipDuplicates,
                            skipSize,
                            skipLowQuality,
                            sequences
                    );

                    buffer = 0;
                    sequences = new ArrayList<FastqSequence>();
                }

                if (!skipDuplicates) {
                    this.bufferSize = bufferSize;

                    this.findDuplicatedOrSimilarLines(skipSimilar, parallel);

                    ExecutorService executor = Executors.newWorkStealingPool(parallel);

                    executor.submit(() -> {
                        this.writeFile("Writing file with similar sequences", Status.SIMILAR, 1, r1, r2);
                    });

                    executor.submit(() -> {
                        this.writeFile("Writing file with duplicated sequences", Status.DUPLICATE, 1, r1, r2);
                    });

                    executor.submit(() -> {
                        this.writeFile("Writing recoded file", Status.USE, 1, r1, r2);
                    });

                    waitForTheOthers(executor);

                    System.out.println("Finished");

                }

            } // end loadR1

        } catch (IOException e) {
            log.error(e);
        }

        System.out.println("\n");

        return result;
    }

    private void waitForTheOthers(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error(e);
        }
    }

    private void waitParallels(int parallel, List<Future<?>> futures, Future<?> future) {
        if (!future.isDone()) {
            futures.add(future);
        }

        if (futures.size() == parallel) {
            boolean hasDoneSomeThread = false;

            while(!hasDoneSomeThread) {
                for (Future<?> fut : futures) {
                    if (fut.isDone()) {
                        hasDoneSomeThread = true;
                    } else {
                        try {
                            fut.get(10, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                        } catch (ExecutionException e) {
                        } catch (TimeoutException e) {
                        }
                    }
                }
            }
        }
    }

    private void storeLoadedSequence(
            String r1,
            String r2,
            long minSize,
            int parallel,
            double minQual,
            double minQualPerc,
            boolean loadR2,
            boolean skipDuplicates,
            boolean skipSize,
            boolean skipLowQuality,
            List<FastqSequence> sequences) {

        ExecutorService executor = Executors.newWorkStealingPool(parallel);

        List<Future<?>> futures = new ArrayList<Future<?>>();

        long index = 0;

        for(final FastqSequence bseq : sequences) {

            final long finalIndex = ++index;

            Future<?> future = executor.submit(() -> {

                bseq.setSize1(
                        this.calculateSize(bseq.getSequence1())
                );

                this.calculateScore(
                        bseq,
                        minQual,
                        minQualPerc
                );

                if (bseq.getSize1() < minSize
                        && !skipSize) {

                    bseq.setStatus(Status.SMALL);

                } else if (bseq.getScore1() < minQualPerc
                        && !skipLowQuality) {

                    bseq.setStatus(Status.LOW_QUALITY);

                } else {
                    bseq.setStatus(Status.USE);
                }

                if (loadR2) {
                    bseq.setSize2(
                            this.calculateSize(bseq.getSequence2())
                    );

                    if (Status.USE.equals(bseq.getStatus())) {
                        if (bseq.getSize1() < minSize
                                && !skipSize) {

                            bseq.setStatus(Status.SMALL);

                        } else if (bseq.getScore1() < minQualPerc
                                && !skipLowQuality) {

                            bseq.setStatus(Status.LOW_QUALITY);

                        }
                    }
                }

                if (Status.SMALL.equals(bseq.getStatus())
                        || Status.LOW_QUALITY.equals(bseq.getStatus())) {
                    writeSeqInFiles(bseq, r1, r2);
                } else if (Status.USE.equals(bseq.getStatus())) {
                    if (skipDuplicates) {
                        writeSeqInFiles(bseq, r1, r2);
                    } else {
                        this.save(bseq);
                    }
                }

                updateProgress("Writing buffer data", new Double(finalIndex), new Double(sequences.size()));
            });

            waitParallels(parallel, futures, future);

        } // end for

        waitForTheOthers(executor);
    }

    private void writeFile(String message, Status status, int pageNumber, String r1, String r2) {
        Page<FastqSequence> fqList = this.getByStatus(status, pageNumber);

        if (fqList != null
                && fqList.hasContent()) {

            long index = 0;

            for(FastqSequence seq: fqList.getContent()) {

                this.writeSeqInFiles(seq, r1, r2);

                updateProgress(message, new Double(++index * pageNumber), new Double(fqList.getTotalElements()));
            }

            if (fqList.getTotalPages() > pageNumber) {
                // Clean last list in memory
                fqList = null;

                this.writeFile(message, status, ++pageNumber, r1, r2);
            }
        }
    }

    private void updateProgress(String message, double current, double total) {
        if (!this.skipProgressBar) {
            if ((current % 10) == 0) {

                this.progressbarExecutor.submit(() -> {
                    final int width = 25; // progress bar width in chars
                    final int fraction = 4;

                    double progressPercentage = ((current / total) * 100D);

                    StringBuffer bar = new StringBuffer();

                    bar.append("\r[");

                    for (int i = 0; i < width; i++) {
                        if (i <= (progressPercentage / fraction)) {
                            bar.append("#");
                        } else {
                            bar.append(" ");
                        }
                    }
                    bar.append("] " + new BigDecimal(progressPercentage).setScale(3, BigDecimal.ROUND_CEILING).toString() + "% -> " + message);

                    System.out.print(bar.toString());

                    if (progressPercentage == 100D) {
                        System.out.print("\n");
                    }
                });

            }
        }
    }

    private void findDuplicatedOrSimilarLines(boolean skipSimilar, int parallel) {
        long totalSeqs = fastqSequenceRepository.count();

        ExecutorService executor = Executors.newWorkStealingPool(parallel);

        List<Future<?>> futures = new ArrayList<Future<?>>();

        for (long index = 1; index <= totalSeqs; index++) {
            final FastqSequence seq = fastqSequenceRepository.findByIdAndStatus(index, Status.USE);

            if (seq != null) {

                final long finalIndex = index;

                Future<?> future = executor.submit(() -> {
                    List<FastqSequence> duplicatedSeqList = null;
                    if (seq.getSeqId2() != null) {
                        String sequence1 = null;
                        String sequence2 = null;

                        if (seq.getSequence1Marked() != null && !skipSimilar) {
                            sequence1 = seq.getSequence1Marked();
                        } else {
                            sequence1 = seq.getSequence1();
                        }

                        if (seq.getSequence2Marked() != null && !skipSimilar) {
                            sequence2 = seq.getSequence2Marked();
                        } else {
                            sequence2 = seq.getSequence2();
                        }

                        duplicatedSeqList = fastqSequenceRepository.findByTwoSequences(
                                seq.getId(),
                                sequence1,
                                sequence2,
                                Status.USE
                        );
                    } else {
                        String sequence1 = null;

                        if (seq.getSequence1Marked() != null) {
                            sequence1 = seq.getSequence1Marked();
                        } else {
                            sequence1 = seq.getSequence1();
                        }

                        duplicatedSeqList = fastqSequenceRepository.findBySequence(
                                seq.getId(),
                                sequence1,
                                Status.USE
                        );
                    }

                    if (duplicatedSeqList != null
                            && !duplicatedSeqList.isEmpty()) {

                        FastqSequence tempSeq = seq;

                        boolean isSimilar = false;
                        for(FastqSequence duplicatedSeq : duplicatedSeqList) {
                            if (seq.getSeqId2() != null) {

                                if ((duplicatedSeq.getSequence1().equals(tempSeq.getSequence1()) && duplicatedSeq.getSequence2().equals(tempSeq.getSequence2()))
                                        || (duplicatedSeq.getSequence2().equals(tempSeq.getSequence1()) && duplicatedSeq.getSequence1().equals(tempSeq.getSequence2()))) {
                                    isSimilar = false;
                                } else {
                                    isSimilar = true;
                                }

                                if ( (duplicatedSeq.getScore1() + duplicatedSeq.getScore2()) > (tempSeq.getScore1() + tempSeq.getScore2()) ){
                                    if (isSimilar) {
                                        tempSeq.setStatus(Status.SIMILAR);
                                    } else {
                                        tempSeq.setStatus(Status.DUPLICATE);
                                    }

                                    this.save(tempSeq);
                                } else {
                                    if (isSimilar) {
                                        duplicatedSeq.setStatus(Status.SIMILAR);
                                    } else {
                                        duplicatedSeq.setStatus(Status.DUPLICATE);
                                    }

                                    this.save(duplicatedSeq);
                                }
                            } else {
                                if ( tempSeq.getSequence1().equals(seq.getSequence1()) ) {
                                    if ( tempSeq.getScore1() > seq.getScore1() ){
                                        seq.setStatus(Status.DUPLICATE);

                                        this.save(seq);
                                    } else {
                                        tempSeq.setStatus(Status.DUPLICATE);

                                        this.save(tempSeq);
                                    }
                                }
                            }

                            if (Status.USE.equals(duplicatedSeq)) {
                                tempSeq = duplicatedSeq;
                            }

                            isSimilar = false;
                        }
                    }

                    updateProgress("Looking for duplicated or similar sequences", new Double(finalIndex), new Double(totalSeqs));

                });

                waitParallels(parallel, futures, future);

            }

        }

        waitForTheOthers(executor);
    }

    private String generateDirectory(String filename, Status status) {
        if (filename.contains(".fastq")) {
            filename = filename.replace(".fastq", "");

            File dir = new File(filename);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String[] dirs = filename.split(File.separator);

            filename = filename + File.separator + dirs[dirs.length - 1] + status.getExtension();
        }
        return filename;
    }

    @Override
    public void removeDirectory(String filename) {
        if (filename.contains(".fastq")) {
            filename = filename.replace(".fastq", "");

            File dir = new File(filename);
            if (dir.exists()) {

                File[] files = dir.listFiles();
                for (File file : files) {
                    file.delete();
                }

                dir.delete();
            }
        }
    }

    private void writeSeqInFiles(
            FastqSequence seq,
            String r1,
            String r2
        ) {

        if (r1 != null && seq != null) {
            // Create file
            FileWriter fstream1 = null;
            FileWriter fstream2 = null;

            try {
                writeR1Seq(seq, r1);
            } catch (IOException e) {
                log.error(e);
            }

            if (seq.getSeqId2() != null && r2 != null) {
                try {
                    writeR2Seq(seq, r2);
                } catch (IOException e) {
                    log.error(e);
                }
            }

        }
    }

    private void writeR1Seq(FastqSequence seq, String r1) throws IOException {
        FileWriter fstream1;
        r1 = generateDirectory(r1, seq.getStatus());

        fstream1 = new FileWriter(r1, true);

        BufferedWriter out1 = new BufferedWriter(fstream1);

        out1.write(seq.getSeqId1());
        out1.newLine();

        out1.write(seq.getSequence1());
        out1.newLine();

        out1.write(seq.getFeature1());
        out1.newLine();

        out1.write(seq.getQuality1());
        out1.newLine();

        //Close the output stream
        out1.close();
    }

    private void writeR2Seq(FastqSequence seq, String r2) throws IOException {
        FileWriter fstream2;
        r2 = generateDirectory(r2, seq.getStatus());

        fstream2 = new FileWriter(r2, true);

        BufferedWriter out2 = new BufferedWriter(fstream2);

        out2.write(seq.getSeqId2());
        out2.newLine();

        out2.write(seq.getSequence2());
        out2.newLine();

        out2.write(seq.getFeature2());
        out2.newLine();

        out2.write(seq.getQuality2());
        out2.newLine();

        //Close the output stream
        out2.close();
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public Page<FastqSequence> getByStatus(Status status, Integer pageNumber) {
        if (pageNumber < 1) {
            pageNumber = 1;
        }

        PageRequest request = new PageRequest(pageNumber - 1, this.bufferSize);

        Page<FastqSequence> result = fastqSequenceRepository.findByStatus(
                status,
                request
        );

        return result;
    }
}
