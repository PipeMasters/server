package com.pipemasters.server.service;

import org.springframework.transaction.annotation.Transactional;

public interface ImotioService {
    String processImotioFileUpload(Long mediaFileId);
}
