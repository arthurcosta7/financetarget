package br.com.financetarget.planningspace.application;

import java.time.Instant;
import java.util.UUID;

public interface PersonalSpaceProvisioner {
    UUID createForVerifiedUser(UUID userId, Instant now);
}
