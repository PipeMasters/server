package com.pipemasters.server.kafka.handler.impl;

import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.kafka.KafkaProducerService;
import com.pipemasters.server.kafka.event.MinioEvent;
import com.pipemasters.server.kafka.handler.MinioEventHandler;
import com.pipemasters.server.repository.MediaFileRepository;
import com.pipemasters.server.service.ImotioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ObjectCreatedHandler implements MinioEventHandler {
    private final static Logger log = LoggerFactory.getLogger(ObjectCreatedHandler.class);

    private final MediaFileRepository repository;
    private final KafkaProducerService producer;
    private final ImotioService imotioService;

    public ObjectCreatedHandler(MediaFileRepository repository, KafkaProducerService producer, ImotioService imotioService) {
        this.repository = repository;
        this.producer = producer;
        this.imotioService = imotioService;
    }

    @Override
    public boolean supports(String eventName) {
        return eventName.startsWith("s3:ObjectCreated:");
    }


    // TODO: надо решить что делать с проверкой на mp3 тут. + надо подумать что делать со статусом Медиафайла,
    //  так как тут мы его ставим как UPLOADED значит и дальше надо с ним что-то делать
    //  надо написать доп тесты на этот обработчик
    @Override
    @Transactional
    public void handle(MinioEvent event) {
        String filename = event.filename();
        String fileExtension;
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            fileExtension = filename.substring(lastDotIndex + 1).toLowerCase();
        } else {
            fileExtension = "";
        }
        repository.findByFilenameAndUploadBatchDirectory(filename, event.batchId())
                .ifPresentOrElse(file -> {
                    file.setStatus(MediaFileStatus.UPLOADED);
                    repository.save(file);
                    log.debug("Status of file {} set to {}", file.getId(), file.getStatus());

                    if (file.getFileType() == FileType.AUDIO && (fileExtension.equals("mp3") || fileExtension.equals("wav"))) {
                        log.info("Audio file detected {}", file.getFilename());
                        imotioService.processImotioFileUpload(file.getId());
                    }
                    else if (file.getFileType() == FileType.VIDEO) {
                        producer.send("processing-queue", file.getId().toString());
                    }
                }, () -> log.warn("MediaFile not found for key {}", event.decodedKey()));
    }
}
