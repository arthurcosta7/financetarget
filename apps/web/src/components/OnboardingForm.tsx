"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch } from "@/lib/api/client";

type Profile = {
  recurringIncome: string;
  essentialExpenses: string;
  initialGoalBalance: string;
  suggestedMonthlyCapacity: string;
  confirmedMonthlyCapacity: string;
  currency: string;
};
type Requirements = { termsVersion: string; privacyNoticeVersion: string };

export function OnboardingForm() {
  const router = useRouter();
  const [requirements, setRequirements] = useState<Requirements>();
  const [profile, setProfile] = useState<Profile>();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();

  useEffect(() => {
    Promise.all([
      apiFetch<Requirements>("/onboarding/requirements"),
      apiFetch<Profile>("/onboarding/financial-profile").catch((error) => {
        if (error instanceof ApiError && error.status === 404) return undefined;
        throw error;
      }),
    ]).then(([nextRequirements, nextProfile]) => {
      setRequirements(nextRequirements);
      setProfile(nextProfile);
    }).catch((error) => {
      if (error instanceof ApiError && error.status === 401) router.push("/entrar");
      else setMessage({ kind: "error", text: error instanceof Error ? error.message : "Não foi possível carregar o perfil." });
    });
  }, [router]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    setMessage(undefined);
    const data = new FormData(event.currentTarget);
    const money = (name: string) => String(data.get(name) ?? "").replace(",", ".");
    try {
      const saved = await apiFetch<Profile>("/onboarding/financial-profile", {
        method: "PUT",
        body: JSON.stringify({
          recurringIncome: money("income"), essentialExpenses: money("expenses"),
          initialGoalBalance: money("balance"), confirmedMonthlyCapacity: money("capacity"),
          termsAccepted: data.get("terms") === "on", privacyNoticeAcknowledged: data.get("privacy") === "on",
        }),
      });
      setProfile(saved);
      setMessage({ kind: "success", text: "Perfil salvo. A estimativa foi calculada no servidor e continua editável." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível salvar o perfil." });
    } finally {
      setPending(false);
    }
  }

  return (
    <AppShell section="Onboarding · 01">
      <header className="app-heading">
        <p className="eyebrow">Ponto de partida</p>
        <h1>O que cabe no plano de hoje?</h1>
        <p>Use valores mensais aproximados. Você poderá revisar tudo antes de criar uma meta.</p>
      </header>
      <div className="app-grid">
        <form className="form-stack form-stack--large" onSubmit={submit}>
          <div className="field-row">
            <label className="field"><span>Renda recorrente mensal</span>
              <span className="money-input"><b>R$</b><input defaultValue={profile?.recurringIncome ?? ""} inputMode="decimal" name="income" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
            <label className="field"><span>Despesas essenciais mensais</span>
              <span className="money-input"><b>R$</b><input defaultValue={profile?.essentialExpenses ?? ""} inputMode="decimal" name="expenses" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
          </div>
          <div className="field-row">
            <label className="field"><span>Valor já separado para metas</span>
              <span className="money-input"><b>R$</b><input defaultValue={profile?.initialGoalBalance ?? ""} inputMode="decimal" name="balance" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
            <label className="field"><span>Quanto quer confirmar por mês</span>
              <span className="money-input"><b>R$</b><input defaultValue={profile?.confirmedMonthlyCapacity ?? ""} inputMode="decimal" name="capacity" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
          </div>
          <label className="check-field"><input name="terms" required type="checkbox" />
            <span>Confirmo os termos configurados <small>versão {requirements?.termsVersion ?? "carregando"}</small></span>
          </label>
          <label className="check-field"><input name="privacy" required type="checkbox" />
            <span>Declaro ciência do aviso de privacidade <small>versão {requirements?.privacyNoticeVersion ?? "carregando"}</small></span>
          </label>
          {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}
          <button className="button button--primary" disabled={pending || !requirements} type="submit">
            {pending ? "Salvando…" : "Salvar ponto de partida"}
          </button>
          {profile && <Link className="text-link" href="/app/metas/nova">Continuar para a primeira meta</Link>}
        </form>
        <aside className="calculation-note" aria-live="polite">
          <span className="hero__number">Estimativa do sistema</span>
          <strong>{profile ? `R$ ${profile.suggestedMonthlyCapacity.replace(".", ",")}` : "—"}</strong>
          <p>Diferença entre renda recorrente e despesas essenciais, limitada a zero. É uma referência editável, não uma garantia.</p>
        </aside>
      </div>
    </AppShell>
  );
}
