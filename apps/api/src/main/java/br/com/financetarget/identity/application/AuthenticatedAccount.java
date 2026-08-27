package br.com.financetarget.identity.application;

import java.util.UUID;

public record AuthenticatedAccount(UUID userId, UUID sessionFamilyId, String email, String displayName) {
}
