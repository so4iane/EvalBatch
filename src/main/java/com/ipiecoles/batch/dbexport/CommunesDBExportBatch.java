package com.ipiecoles.batch.dbexport;

import com.ipiecoles.batch.model.Commune;
import com.ipiecoles.batch.repository.CommuneRepository;
import com.ipiecoles.batch.step.CommuneDbTasklet;
import com.ipiecoles.batch.step.CommuneSkipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.FormatterLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;

@Configuration
public class CommunesDBExportBatch {
    private static final Logger logger = LoggerFactory.getLogger(CommunesDBExportBatch.class);

    @Autowired
    public EntityManagerFactory entityManagerFactory;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Value("${communesImportBatch.chunksize}")
    private Integer maxLength;

    @Autowired
    public CommuneRepository communeRepository;

    @Bean
    @Qualifier("exportCommunes")
    public Job exportCommunes() {
        logger.info("Start extract communes");
        return jobBuilderFactory.get("exportCommunes")
                .incrementer(new RunIdIncrementer())
                .flow(stepExportFromDB())
                .next(stepExportToFile())
                .end()
                .build();
    }

    // Step 1
    @Bean
    public Step stepExportFromDB(){
        logger.info("Exporting communes from db");
        return stepBuilderFactory.get("stepExportFromDB")
                .tasklet(new CommuneDbTasklet())
                .listener(new CommuneDbTasklet())
                .build();
    }

    // Step 2
    @Bean
    public Step stepExportToFile(){
        logger.info("Exporting communes to file");
        return stepBuilderFactory.get("stepExportToFile")
                .<Commune, Commune> chunk(maxLength)
                .reader(communeReader())
                .writer(communeWriter())
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .listener(new CommuneSkipListener())
                .build();
    }


    @Bean
    public JpaPagingItemReader<Commune> communeReader() {
        logger.info("Reading db communes");
        String sqlQuery = "from Commune c order by code_postal, code_insee";
        return new JpaPagingItemReaderBuilder<Commune>()
                .name("communeReader")
                .entityManagerFactory(entityManagerFactory)
                .pageSize(maxLength)
                .queryString(sqlQuery)
                .build();

    }

    public ItemWriter<Commune> communeWriter(){
        logger.info("Writing communes");
        BeanWrapperFieldExtractor<Commune> communeBeanWrapperFieldExtractor = new BeanWrapperFieldExtractor<Commune>();
        communeBeanWrapperFieldExtractor.setNames(new String[] {"codePostal", "codeInsee", "nom", "latitude", "longitude"});

        FormatterLineAggregator<Commune> communeFormatterLineAggregator = new FormatterLineAggregator<Commune>();
        communeFormatterLineAggregator.setFormat("%5s - %5s - %s : %.5f %.5f");
        communeFormatterLineAggregator.setFieldExtractor(communeBeanWrapperFieldExtractor);

        FlatFileItemWriter<Commune> communeFlatFileItemWriter = new FlatFileItemWriter<Commune>();
        communeFlatFileItemWriter.setName("fileWriter");
        communeFlatFileItemWriter.setResource(new FileSystemResource("target/test.txt"));
        communeFlatFileItemWriter.setLineAggregator(communeFormatterLineAggregator);
        communeFlatFileItemWriter.setHeaderCallback(new CommuneHeader(communeRepository));
        communeFlatFileItemWriter.setFooterCallback(new CommuneFooter(communeRepository));

        return communeFlatFileItemWriter;

    }

}
