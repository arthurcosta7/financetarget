"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch } from "@/lib/api/client";
import type { FinancialProfile, Goal } from "@/lib/goals";

export function GoalCreateForm() {
  const router = useRouter();
  const [profile, setProfile] = useState<FinancialProfile>();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<string>();

  useEffect(() => {
    apiFetch<FinancialProfile>("/onboarding/financial-profile")
      .then(setProfile)
      .catch((error) => {
        if (error instanceof ApiError && error.status === 401) router.push("/entrar");
        else if (error instanceof ApiError && error.status === 404) router.push("/app/onboarding");
        else setMessage(error instanceof Error ? error.message : "Não foi possível carregar o ponto de partida.");
      });
  }, [router]);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!profile) return;
    setPending(true);
    setMessage(undefined);
    const data = new FormData(event.currentTarget);
    const decimal = (name: string) => String(data.get(name) ?? "").replace(",", ".");
    try {
      const goal = await apiFetch<Goal>(`/planning-spaces/${profile.spaceId}/goals`, {
        method: "POST",
        body: JSON.stringify({
          goalType: "HOME_DOWN_PAYMENT",
          title: data.get("title"),
          targetAmount: decimal("targetAmount"),
          targetValueBasis: data.get("targetValueBasis"),
          targetDate: data.get("targetDate"),
          initialBalance: decimal("initialBalance"),
          annualInflationRate: decimal("annualInflationRate"),
          annualReturnRate: decimal("annualReturnRate"),
          contributionTiming: data.get("contributionTiming"),
        }),
      });
      router.push(`/app/metas/${goal.id}`);
    } catch (error) {
      setMessage(error instanceof ApiError ? error.message : "Não foi possível criar a meta.");
    } finally {
      setPending(false);
    }
  }

  return (
    <AppShell section="Metas · nova">
      <header className="app-heading goal-heading">
        <p className="eyebrow">Primeira direção</p>
        <h1>Qual entrada você quer alcançar?</h1>
        <p>Informe a meta e as premissas que você escolheu. O cálculo será feito e versionado no servidor.</p>
      </header>
      <div className="goal-create-layout">
        <form className="form-stack form-stack--large goal-form" onSubmit={submit}>
          <label className="field"><span>Nome da meta</span>
            <input defaultValue="Entrada do imóvel" maxLength={120} minLength={2} name="title" required />
          </label>
          <div className="field-row">
            <label className="field"><span>Valor que pretende alcançar</span>
              <span className="money-input"><b>{profile?.currency ?? "—"}</b><input inputMode="decimal" name="targetAmount" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
            <label className="field"><span>Data da meta</span><input name="targetDate" required type="date" /></label>
          </div>
          <div className="field-row">
            <label className="field"><span>Valor já separado</span>
              <span className="money-input"><b>{profile?.currency ?? "—"}</b><input defaultValue={profile?.initialGoalBalance ?? ""} inputMode="decimal" name="initialBalance" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span>
            </label>
            <label className="field"><span>Como interpretar o valor</span>
              <select defaultValue="CURRENT_VALUE" name="targetValueBasis">
                <option value="CURRENT_VALUE">Valor de hoje, atualizado pela inflação</option>
                <option value="FIXED_NOMINAL">Valor nominal fixo na data</option>
              </select>
            </label>
          </div>
          <fieldset className="assumption-fields">
            <legend>Premissas anuais</legend>
            <div className="field-row">
              <label className="field"><span>Inflação em decimal</span><input inputMode="decimal" name="annualInflationRate" pattern="-?[0-9]+([.,][0-9]{1,8})?" placeholder="Informe como decimal, não percentual" required /></label>
              <label className="field"><span>Retorno hipotético em decimal</span><input inputMode="decimal" name="annualReturnRate" pattern="-?[0-9]+([.,][0-9]{1,8})?" placeholder="Informe como decimal, não percentual" required /></label>
            </div>
            <label className="field"><span>Momento do aporte mensal</span>
              <select defaultValue="END_OF_MONTH" name="contributionTiming">
                <option value="END_OF_MONTH">No fim do mês</option>
                <option value="BEGINNING_OF_MONTH">No início do mês</option>
              </select>
            </label>
          </fieldset>
          {message && <FormMessage kind="error">{message}</FormMessage>}
          <button className="button button--primary" disabled={pending || !profile} type="submit">
            {pending ? "Calculando…" : "Calcular e criar meta"}
          </button>
        </form>
        <aside className="goal-create-note">
          <p className="eyebrow">O que será salvo</p>
          <p>Entradas originais, valores normalizados, resultado, avisos, fórmula e versão do motor formam um snapshot imutável.</p>
          <dl>
            <div><dt>Capacidade declarada</dt><dd>{profile ? `${profile.currency} ${profile.confirmedMonthlyCapacity.replace(".", ",")}` : "—"}</dd></div>
            <div><dt>Periodicidade</dt><dd>Mensal</dd></div>
            <div><dt>Arredondamento</dt><dd>Meio par, 2 casas</dd></div>
          </dl>
        </aside>
      </div>
    </AppShell>
  );
}
