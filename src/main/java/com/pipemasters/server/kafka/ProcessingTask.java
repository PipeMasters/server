package com.pipemasters.server.kafka;

public class ProcessingTask {
    private Long mediaFileId;

    public ProcessingTask() {
    }

    public ProcessingTask(Long mediaFileId) {
        this.mediaFileId = mediaFileId;
    }

    public Long getMediaFileId() {
        return mediaFileId;
    }

    public void setMediaFileId(Long mediaFileId) {
        this.mediaFileId = mediaFileId;
    }
}