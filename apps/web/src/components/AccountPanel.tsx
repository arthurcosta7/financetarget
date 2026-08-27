"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch, idempotencyKey } from "@/lib/api/client";

type Account = { id: string; email: string; displayName: string };
type DeletionRequest = { id: string; status: string; createdAt: string };

export function AccountPanel() {
  const router = useRouter();
  const [account, setAccount] = useState<Account>();
  const [deletion, setDeletion] = useState<DeletionRequest>();
  const [message, setMessage] = useState<{ kind: "error" | "success" | "neutral"; text: string }>();

  useEffect(() => {
    apiFetch<Account>("/auth/me").then(setAccount).catch((error) => {
      if (error instanceof ApiError && error.status === 401) router.push("/entrar");
      else setMessage({ kind: "error", text: "Não foi possível carregar sua conta." });
    });
    apiFetch<DeletionRequest>("/privacy/deletion-requests/current").then(setDeletion).catch(() => undefined);
  }, [router]);

  async function updateName(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    try {
      setAccount(await apiFetch<Account>("/auth/me", {
        method: "PATCH", body: JSON.stringify({ displayName: data.get("displayName") }),
      }));
      setMessage({ kind: "success", text: "Nome atualizado." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível atualizar." });
    }
  }

  async function exportData(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      const exported = await apiFetch<unknown>("/privacy/exports", {
        method: "POST",
        headers: { "Idempotency-Key": idempotencyKey("export") },
        body: JSON.stringify({ password: data.get("password") }),
      });
      const url = URL.createObjectURL(new Blob([JSON.stringify(exported, null, 2)], { type: "application/json" }));
      const link = document.createElement("a");
      link.href = url;
      link.download = "financetarget-meus-dados.json";
      link.click();
      URL.revokeObjectURL(url);
      setMessage({ kind: "success", text: "Exportação preparada neste dispositivo." });
      form.reset();
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível exportar." });
    }
  }

  async function requestDeletion(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      const request = await apiFetch<DeletionRequest>("/privacy/deletion-requests", {
        method: "POST",
        headers: { "Idempotency-Key": idempotencyKey("deletion") },
        body: JSON.stringify({ password: data.get("password") }),
      });
      setDeletion(request);
      setMessage({ kind: "neutral", text: "Solicitação registrada. Nenhum dado foi apagado nesta fase." });
      form.reset();
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível registrar." });
    }
  }

  return (
    <AppShell section="Conta · privacidade">
      <header className="app-heading">
        <p className="eyebrow">Controle da conta</p>
        <h1>Seus dados, suas decisões.</h1>
        <p>Revise a identidade da conta e exerça direitos sem misturar dados de outro participante.</p>
      </header>
      {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}
      <div className="settings-grid">
        <section className="setting-card">
          <span className="hero__number">01 · Perfil</span>
          <h2>Como chamamos você</h2>
          <form className="form-stack" onSubmit={updateName}>
            <label className="field"><span>Nome de exibição</span>
              <input defaultValue={account?.displayName} key={account?.displayName} name="displayName" required />
            </label>
            <p className="form-footnote">Conta: {account?.email ?? "carregando"}</p>
            <button className="button button--quiet" type="submit">Salvar nome</button>
          </form>
        </section>
        <section className="setting-card">
          <span className="hero__number">02 · Portabilidade</span>
          <h2>Exportar meus dados</h2>
          <p>A exportação inclui apenas sua conta, seu perfil pessoal e seus registros de consentimento.</p>
          <form className="form-stack" onSubmit={exportData}>
            <label className="field"><span>Confirme sua senha</span><input autoComplete="current-password" name="password" required type="password" /></label>
            <button className="button button--quiet" type="submit">Baixar arquivo JSON</button>
          </form>
        </section>
        <section className="setting-card setting-card--critical">
          <span className="hero__number">03 · Exclusão</span>
          <h2>Solicitar exclusão</h2>
          <p>A Fase 3 registra e acompanha o pedido, mas ainda não executa a remoção física. As regras para espaços compartilhados precisam ser aprovadas antes.</p>
          {deletion && <p className="request-status">Pedido {deletion.status.toLowerCase()} em {new Date(deletion.createdAt).toLocaleDateString("pt-BR")}.</p>}
          <form className="form-stack" onSubmit={requestDeletion}>
            <label className="field"><span>Confirme sua senha</span><input autoComplete="current-password" name="password" required type="password" /></label>
            <button className="button button--danger" type="submit">Registrar solicitação</button>
          </form>
        </section>
      </div>
    </AppShell>
  );
}
