package bombonato.fastq.batch.config;

import bombonato.fastq.business.service.FastqSequenceService;
import bombonato.fastq.business.service.impl.FastqSequeceServiceImpl;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing(modular=true)
public class AppConfig {

    @Bean
    public FastqSequenceService fastqSequenceService() {
        return new FastqSequeceServiceImpl();
    }

}
