# Registro de release

Copie este modelo para o repositório operacional aprovado. Não registre segredos, dados pessoais ou valores financeiros.

## Identificação

- release:
- SHA Git completo:
- digest da imagem API:
- digest da imagem web:
- SBOM/atestação:
- schema esperado:
- ambiente:
- janela UTC:

## Responsáveis

- decisão de release:
- execução técnica:
- banco/backup:
- incidente e substituto:
- privacidade/jurídico:
- suporte/comunicação:

## Gates

- [ ] CI, auditoria e scan sem achado bloqueante
- [ ] documentos jurídicos e RIPD aprovados
- [ ] validação matemática independente registrada
- [ ] backup recente e restauração isolada aprovados
- [ ] alertas entregues e plantão confirmado
- [ ] SPF, DKIM, DMARC, bounce e complaint aprovados
- [ ] migrations revisadas como expand/contract
- [ ] digest anterior compatível disponível
- [ ] smoke de staging aprovado com os mesmos digests
- [ ] autorização manual de rollout registrada

## Rollout e evidências

Para cada etapa, registrar percentual/escopo, início, fim, disponibilidade, 5xx, p95, saturação, decisão e responsável.

## Resultado

- decisão: prosseguir / pausar / rollback
- motivo factual:
- versão ativa:
- schema ativo:
- smoke final:
- risco residual aceito por:
- ações e prazos:
