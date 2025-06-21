package com.pipemasters.server;

import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class TestDataRunner implements CommandLineRunner {
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final DelegationRepository delegationRepository;
    private final static Logger log = LoggerFactory.getLogger(TestDataRunner.class);

    public TestDataRunner(BranchRepository branchRepository,
                          UserRepository userRepository,
                          TrainRepository trainRepository,
                          UploadBatchRepository uploadBatchRepository,
                          DelegationRepository delegationRepository) {
        this.branchRepository = branchRepository;
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.uploadBatchRepository = uploadBatchRepository;
        this.delegationRepository = delegationRepository;
    }

    @Override
    @Transactional(readOnly = false)
    public void run(String... args) {
        // Branches
        log.info("Initializing test data...");
        if (branchRepository.existsByName("Main Branch")) return;
        log.debug("Creating test branches and users...");
        Branch mainBranch = new Branch("Main Branch", null);
        branchRepository.save(mainBranch);
        log.debug("Main branch created: {}", mainBranch.getName());
        Branch subBranch = new Branch("Sub Branch", mainBranch);
        branchRepository.save(subBranch);
        log.debug("Sub branch created: {} under {}", subBranch.getName(), mainBranch.getName());

        // Users
        User uploader = new User("Alexey", "Sidorov", "Petrovich",
                Set.of(Role.USER), mainBranch);
        User substitute = new User("Maria", "Ivanova", "Alexandrovna",
                Set.of(Role.USER, Role.BRANCH_ADMIN), subBranch);
        log.debug("Creating test users: {}, {}", uploader.getName(), substitute.getName());
        userRepository.saveAll(List.of(uploader, substitute));

        // Delegation
        Delegation delegation = new Delegation(uploader, substitute,
                LocalDate.now(), LocalDate.now().plusDays(30));
        delegationRepository.save(delegation);

        // Trains
        Train trainA = new Train(101L, "Moscow - Saint Petersburg", 10, "Ivanov I.I.");
        Train trainB = new Train(202L, "Saint Petersburg - Sochi", 12, "Petrov P.P.");
        trainRepository.saveAll(List.of(trainA, trainB));

        // Upload batch with files
        UploadBatch batchWithFiles = new UploadBatch();
        batchWithFiles.setDirectory(UUID.randomUUID());
        batchWithFiles.setUploadedBy(uploader);
        batchWithFiles.setTrainDeparted(LocalDate.now().minusDays(1));
        batchWithFiles.setTrain(trainA);
        batchWithFiles.setComment("Sample batch with media files");
        batchWithFiles.setBranch(mainBranch);
        batchWithFiles.getKeywords().addAll(Set.of("sample", "video"));

        MediaFile video = new MediaFile("batch1/video.mp4", FileType.VIDEO, batchWithFiles);
        MediaFile audio = new MediaFile("batch1/audio.mp3", FileType.AUDIO, Instant.now(), video, batchWithFiles);
        batchWithFiles.getFiles().addAll(List.of(video, audio));

        uploadBatchRepository.save(batchWithFiles);

        // Upload batch describing video absence
        UploadBatch absentBatch = new UploadBatch();
        absentBatch.setDirectory(UUID.randomUUID());
        absentBatch.setUploadedBy(substitute);
        absentBatch.setTrainDeparted(LocalDate.now().minusDays(2));
        absentBatch.setTrain(trainB);
        absentBatch.setComment("Batch without video");
        absentBatch.setBranch(subBranch);

        VideoAbsence absence = new VideoAbsence(absentBatch, AbsenceCause.DEVICE_FAILURE, "Camera broken");
        absentBatch.setAbsence(absence);

        uploadBatchRepository.save(absentBatch);
    }
}