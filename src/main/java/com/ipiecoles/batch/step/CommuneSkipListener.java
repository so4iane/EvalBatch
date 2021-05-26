package com.ipiecoles.batch.step;

import com.ipiecoles.batch.dto.CommuneDto;
import com.ipiecoles.batch.model.Commune;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

public class CommuneSkipListener implements SkipListener<CommuneDto, Commune> {
    private static final Logger logger = LoggerFactory.getLogger(CommuneSkipListener.class);

    @Override
    public void onSkipInRead(Throwable throwable) {
        logger.warn("Skip while reading => " + throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(Commune commune, Throwable throwable) {
        logger.warn("Skip while writing => " + commune.toString() + ", " + throwable.getMessage());
    }

    @Override
    public void onSkipInProcess(CommuneDto communeDto, Throwable throwable) {
        logger.warn("Skip while processing => " + communeDto.toString() + ", " + throwable.getMessage());
    }
}
