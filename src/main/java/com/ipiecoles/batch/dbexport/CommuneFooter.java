package com.ipiecoles.batch.dbexport;

import com.ipiecoles.batch.repository.CommuneRepository;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.Writer;

public class CommuneFooter implements FlatFileFooterCallback {
    @Autowired
    public CommuneRepository communeRepository;

    @Override
    public void writeFooter(Writer writer) throws IOException {
        writer.write("Total communes : "+communeRepository.countDistinctCommune());
    }

    public CommuneFooter (CommuneRepository communeRepository){
        this.communeRepository = communeRepository;
    }
}
