package com.pipemasters.server.entity;

import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UploadBatchTest {
    @Test
    void getChainedFiles_returnsSortedVideoFilesWithNumericPostfix() {
        UploadBatch batch = new UploadBatch();
        MediaFile file1 = new MediaFile("video_2.mp4", FileType.VIDEO, batch);
        MediaFile file2 = new MediaFile("video_1.mp4", FileType.VIDEO, batch);
        MediaFile file3 = new MediaFile("video.mp4", FileType.VIDEO, batch); // no postfix
        MediaFile file4 = new MediaFile("audio_1.mp3", FileType.AUDIO, batch); // not a video
        MediaFile file5 = new MediaFile("video_abc.mp4", FileType.VIDEO, batch); // non-numeric postfix

        batch.setFiles(List.of(file1, file2, file3, file4, file5));

        List<MediaFile> result = batch.getChainedFiles();

        assertEquals(2, result.size());
        assertEquals("video_1.mp4", result.get(0).getFilename());
        assertEquals("video_2.mp4", result.get(1).getFilename());
    }

    @Test
    void getChainedFiles_returnsEmptyListWhenNoVideoFiles() {
        UploadBatch batch = new UploadBatch();
        MediaFile file1 = new MediaFile("audio_1.mp3", FileType.AUDIO, batch);
        MediaFile file2 = new MediaFile("image_1.jpg", FileType.IMAGE, batch);

        batch.setFiles(List.of(file1, file2));

        List<MediaFile> result = batch.getChainedFiles();

        assertTrue(result.isEmpty());
    }

    @Test
    void getChainedFiles_handlesEmptyFilesList() {
        UploadBatch batch = new UploadBatch();
        batch.setFiles(List.of());

        List<MediaFile> result = batch.getChainedFiles();

        assertTrue(result.isEmpty());
    }
}
