# Backup e restauração

## Política provisória

Os scripts atuais servem apenas a `dev` e `staging`. Produção exige armazenamento imutável, cofre, agenda, retenção e acesso mínimo escolhidos na Fase 9.

- formato: dump custom do PostgreSQL;
- proteção: AES-256-GCM, nonce aleatório e chave de 32 bytes fora do arquivo;
- integridade: tag GCM e SHA-256 reportado;
- restauração: sempre em banco isolado de nome gerado e validado;
- hipótese operacional: RPO de 24 h e RTO de 4 h, sujeitos a validação de negócio.

## Criar backup

```powershell
$env:BACKUP_ENCRYPTION_KEY_BASE64='<valor-injetado-pelo-cofre>'
./scripts/backup-db.ps1 `
  -EnvironmentFile .env.staging `
  -OutputPath D:\backups\financetarget-AAAA-MM-DD.ftb
```

O script recusa ambientes diferentes de `dev`/`staging`, chave fora de 32 bytes e sobrescrita de arquivo.

## Verificar restauração

```powershell
$env:BACKUP_ENCRYPTION_KEY_BASE64='<mesma-chave>'
./scripts/verify-db-restore.ps1 `
  -EnvironmentFile .env.staging `
  -BackupPath D:\backups\financetarget-AAAA-MM-DD.ftb `
  -ExpectedSchemaVersion 7
```

Para containers sintéticos fora do Compose, os dois scripts aceitam `ContainerName`, `DatabaseName` e/ou `DatabaseUser`. Esses identificadores passam por allowlist antes de qualquer comando.

## Critérios de sucesso

- descriptografia autenticada;
- `pg_restore --exit-on-error` concluído;
- `app_metadata.schema_version` presente e igual à versão esperada;
- banco isolado removido ao final, inclusive após falha;
- nenhuma restauração sobre a origem.

## Falha

Não execute `flyway repair` automaticamente. Preserve arquivo, hash, versão da aplicação e erro sem chave. Teste outra cópia em ambiente isolado e abra incidente se o RPO estiver ameaçado.

O alerta `FinanceTargetBackupStale` depende de um agendador/collector futuro publicar `financetarget_backup_last_success_timestamp_seconds`; essa infraestrutura ainda não foi implantada.
