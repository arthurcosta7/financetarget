"use client";

import Link from "next/link";
import { useState } from "react";
import { useRouter } from "next/navigation";

import { ApiError, apiFetch } from "@/lib/api/client";
import { FormMessage } from "@/components/FormMessage";

type Mode = "register" | "login" | "verify" | "request-recovery" | "reset-password";

const copy: Record<Mode, { submit: string; pending: string }> = {
  register: { submit: "Criar minha conta", pending: "Criando conta…" },
  login: { submit: "Entrar", pending: "Entrando…" },
  verify: { submit: "Verificar e-mail", pending: "Verificando…" },
  "request-recovery": { submit: "Enviar instruções", pending: "Enviando…" },
  "reset-password": { submit: "Definir nova senha", pending: "Alterando…" },
};

export function IdentityForm({ mode, initialToken = "" }: { mode: Mode; initialToken?: string }) {
  const router = useRouter();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();
  const [token, setToken] = useState(initialToken);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setMessage(undefined);
    const data = new FormData(event.currentTarget);
    try {
      if (mode === "register") {
        const result = await apiFetch<{ message: string }>("/auth/registrations", {
          method: "POST",
          body: JSON.stringify({
            displayName: data.get("displayName"), email: data.get("email"), password: data.get("password"),
          }),
        });
        setMessage({ kind: "success", text: result.message });
      } else if (mode === "login") {
        await apiFetch("/auth/sessions", {
          method: "POST",
          body: JSON.stringify({ email: data.get("email"), password: data.get("password") }),
        });
        router.push("/app/onboarding");
      } else if (mode === "verify") {
        await apiFetch("/auth/verifications", { method: "POST", body: JSON.stringify({ token }) });
        setMessage({ kind: "success", text: "E-mail confirmado. Sua conta está pronta para receber o primeiro plano." });
      } else if (mode === "request-recovery") {
        const result = await apiFetch<{ message: string }>("/auth/password-recovery-requests", {
          method: "POST", body: JSON.stringify({ email: data.get("email") }),
        });
        setMessage({ kind: "success", text: result.message });
      } else {
        await apiFetch("/auth/password-recoveries", {
          method: "POST", body: JSON.stringify({ token, newPassword: data.get("password") }),
        });
        setMessage({ kind: "success", text: "Senha atualizada. Todas as sessões anteriores foram encerradas." });
      }
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível concluir a ação." });
    } finally {
      setPending(false);
    }
  }

  const asksEmail = ["register", "login", "request-recovery"].includes(mode);
  const asksPassword = ["register", "login", "reset-password"].includes(mode);
  const asksToken = ["verify", "reset-password"].includes(mode);

  return (
    <form className="form-stack" onSubmit={submit}>
      {mode === "register" && (
        <label className="field">
          <span>Como quer ser chamado</span>
          <input autoComplete="name" minLength={2} name="displayName" required />
        </label>
      )}
      {asksEmail && (
        <label className="field">
          <span>E-mail</span>
          <input autoComplete="email" inputMode="email" name="email" required type="email" />
        </label>
      )}
      {asksToken && (
        <label className="field">
          <span>Código seguro</span>
          <input autoComplete="one-time-code" onChange={(event) => setToken(event.target.value)} required value={token} />
          <small>O código pode ter sido preenchido pelo link recebido.</small>
        </label>
      )}
      {asksPassword && (
        <label className="field">
          <span>{mode === "reset-password" ? "Nova senha" : "Senha"}</span>
          <input autoComplete={mode === "login" ? "current-password" : "new-password"}
            minLength={mode === "login" ? undefined : 15} name="password" required type="password" />
          {mode !== "login" && <small>Use pelo menos 15 caracteres. Espaços e frases são permitidos.</small>}
        </label>
      )}
      {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}
      <button className="button button--primary button--wide" disabled={pending} type="submit">
        {pending ? copy[mode].pending : copy[mode].submit}
      </button>
      {mode === "login" && <Link className="text-link" href="/recuperar-acesso">Esqueci minha senha</Link>}
      {mode === "register" && <p className="form-footnote">Já tem uma conta? <Link href="/entrar">Entrar</Link></p>}
      {mode === "verify" && message?.kind === "success" && <Link className="button button--quiet" href="/entrar">Ir para o acesso</Link>}
    </form>
  );
}
