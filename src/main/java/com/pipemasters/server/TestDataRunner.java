package com.pipemasters.server;

import com.pipemasters.server.entity.*;
import com.pipemasters.server.entity.enums.AbsenceCause;
import com.pipemasters.server.entity.enums.FileType;
import com.pipemasters.server.entity.enums.MediaFileStatus;
import com.pipemasters.server.entity.enums.Role;
import com.pipemasters.server.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class TestDataRunner implements CommandLineRunner {

    private final BranchRepository      branchRepository;
    private final UserRepository        userRepository;
    private final TrainRepository       trainRepository;
    private final UploadBatchRepository uploadBatchRepository;
    private final DelegationRepository  delegationRepository;

    public TestDataRunner(BranchRepository branchRepository,
                          UserRepository userRepository,
                          TrainRepository trainRepository,
                          UploadBatchRepository uploadBatchRepository,
                          DelegationRepository delegationRepository) {
        this.branchRepository      = branchRepository;
        this.userRepository        = userRepository;
        this.trainRepository       = trainRepository;
        this.uploadBatchRepository = uploadBatchRepository;
        this.delegationRepository  = delegationRepository;
    }

    @Override
    @Transactional(readOnly = false)
    public void run(String... args) {
        if (branchRepository.count() > 0) return;

        /* branches */
        Branch central = new Branch("Central", null);
        Branch north   = new Branch("Northern", central);
        Branch south   = new Branch("Southern", central);
        Branch east    = new Branch("Eastern",  central);
        branchRepository.saveAll(List.of(central, north, south, east));

        /* users */
        User centralAdmin = new User("Ivan",   "Petrov",   "Ivanovich", Set.of(Role.ADMIN),        central);
        User centralUser  = new User("Oleg",   "Sidorov",  "Nikolaevich", Set.of(Role.USER),       central);
        User northAdmin   = new User("Anna",   "Morozova", "Sergeevna",  Set.of(Role.BRANCH_ADMIN), north);
        User northUser    = new User("Sergey", "Kuzmin",   "Alexeevich", Set.of(Role.USER),        north);
        User southUser    = new User("Yuri",   "Smirnov",  "Andreevich", Set.of(Role.USER),        south);
        User eastUser     = new User("Maria",  "Ivanova",  "Pavlovna",   Set.of(Role.USER),        east);
        userRepository.saveAll(List.of(centralAdmin, centralUser, northAdmin, northUser, southUser, eastUser));

        /* delegations */
        Delegation d1 = new Delegation(centralUser, centralAdmin, LocalDate.now(), LocalDate.now().plusDays(7));
        Delegation d2 = new Delegation(northUser,  northAdmin,   LocalDate.now().minusDays(3), LocalDate.now().plusDays(14));
        delegationRepository.saveAll(List.of(d1, d2));

        /* trains */
        Train t101 = new Train(101L, "Moscow – Saint-Petersburg", 10, centralAdmin, central);
        Train t202 = new Train(202L, "Saint-Petersburg – Sochi",  12, northAdmin, north);
        Train t303 = new Train(303L, "Sochi – Kazan",             15, southUser, south);
        Train t404 = new Train(404L, "Kazan – Vladivostok",       18, eastUser,  east);
        trainRepository.saveAll(List.of(t101, t202, t303, t404));

        /* upload batches */
        LocalDate today = LocalDate.now();
        List<UploadBatch> batches = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            boolean withFiles = i % 3 != 0;
            User   uploader   = switch (i % 4) {
                case 0 -> centralUser;
                case 1 -> northUser;
                case 2 -> southUser;
                default -> eastUser;
            };
            Branch branch = uploader.getBranch();
            Train  train  = switch (i % 4) {
                case 0 -> t101;
                case 1 -> t202;
                case 2 -> t303;
                default -> t404;
            };

            UploadBatch batch = new UploadBatch();
            batch.setDirectory(UUID.randomUUID());
            batch.setUploadedBy(uploader);

            LocalDate departed = today.minusDays(i + 1);
            batch.setTrainDeparted(departed);
            batch.setTrainArrived(departed.plusDays(1));
            batch.setTrain(train);
            batch.setComment("Test batch #" + (i + 1));
            batch.setBranch(branch);

            if (withFiles) {
                MediaFile video = new MediaFile("batch" + (i + 1) + "/video.mp4", FileType.VIDEO, batch);
                video.setStatus(MediaFileStatus.PROCESSED);
                MediaFile audio = new MediaFile(
                        "batch" + (i + 1) + "/audio.mp3",
                        FileType.AUDIO,
                        Instant.now(),
                        video,
                        batch);
                audio.setStatus(MediaFileStatus.PROCESSED);
                batch.getFiles().addAll(List.of(video, audio));
            } else {
                VideoAbsence absence = new VideoAbsence(batch, AbsenceCause.HUMAN_FACTOR, "No video recorder");
                batch.setAbsence(absence);
            }

            batches.add(batch);
        }

        uploadBatchRepository.saveAll(batches);
    }
}