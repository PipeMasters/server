package com.pipemasters.server.service;

import com.pipemasters.server.entity.MediaFile;

public interface TagService {
    void fetchAndProcessImotioTags(MediaFile mediaFile, String callId);
}
