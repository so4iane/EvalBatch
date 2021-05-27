package com.ipiecoles.batch.step;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class CommuneDbTasklet implements Tasklet {
    private static final Logger logger = LoggerFactory.getLogger(CommuneDbTasklet.class);

    @BeforeStep
    public void before(StepExecution stepExecution){
        logger.info("Doing something before tasklet");
    }


    @AfterStep
    public ExitStatus after(StepExecution stepExecution){
        logger.info("Doing something after tasklet");
        logger.info(stepExecution.getSummary());
        return ExitStatus.COMPLETED;
    }



    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        logger.info("Starting tasklet");
        return RepeatStatus.FINISHED;
    }
}
