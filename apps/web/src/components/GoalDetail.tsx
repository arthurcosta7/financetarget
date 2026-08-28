"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch, idempotencyKey } from "@/lib/api/client";
import { formatDate, formatMoney, goalTypeCopy, warningCopy, type FinancialProfile, type Goal } from "@/lib/goals";

export function GoalDetail({ goalId }: { goalId: string }) {
  const router = useRouter();
  const [goal, setGoal] = useState<Goal>();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();

  useEffect(() => {
    apiFetch<FinancialProfile>("/onboarding/financial-profile")
      .then((profile) => apiFetch<Goal>(`/planning-spaces/${profile.spaceId}/goals/${goalId}`))
      .then(setGoal)
      .catch((error) => {
        if (error instanceof ApiError && error.status === 401) router.push("/entrar");
        else setMessage({ kind: "error", text: error instanceof Error ? error.message : "Não foi possível carregar a meta." });
      });
  }, [goalId, router]);

  async function contribute(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!goal) return;
    setPending(true);
    setMessage(undefined);
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      const result = await apiFetch<{ goal: Goal }>(`/planning-spaces/${goal.spaceId}/goals/${goal.id}/contributions`, {
        method: "POST",
        headers: { "Idempotency-Key": idempotencyKey("contribution") },
        body: JSON.stringify({
          amount: String(data.get("amount") ?? "").replace(",", "."),
          contributionDate: data.get("contributionDate"),
          note: data.get("note") || undefined,
        }),
      });
      setGoal(result.goal);
      form.reset();
      setMessage({ kind: "success", text: "Contribuição registrada. O histórico foi atualizado sem alterar o snapshot do plano." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível registrar a contribuição." });
    } finally {
      setPending(false);
    }
  }

  if (!goal) {
    return <AppShell section="Metas"><p className="goal-empty" aria-live="polite">{message?.text ?? "Carregando plano…"}</p></AppShell>;
  }

  const progressStyle = { "--goal-progress": `${goal.progressPercentage}%` } as React.CSSProperties;
  return (
    <AppShell section="Metas · plano">
      <header className="goal-detail-heading">
        <div><p className="eyebrow">{goalTypeCopy[goal.goalType]}</p><h1>{goal.title}</h1><p>Plano calculado para {formatDate(goal.targetDate)}.</p><Link className="text-link" href={`/app/metas/${goal.id}/cenarios`}>Comparar cenários</Link></div>
        <div className="goal-primary-number"><span>Estimativa mensal</span><strong>{formatMoney(goal.projection.requiredMonthlyContribution)}</strong><small>aporte no {goal.contributionTiming === "END_OF_MONTH" ? "fim" : "início"} do mês</small></div>
      </header>

      <section className="goal-trajectory" aria-label={`Progresso de ${goal.progressPercentage}%`} style={progressStyle}>
        <div className="goal-trajectory__labels"><span>Hoje · {formatMoney(goal.currentBalance)}</span><span>Meta · {formatMoney(goal.projection.targetNominal)}</span></div>
        <div className="goal-trajectory__track"><span /></div>
        <p>Faltam {formatMoney(goal.remainingAmount)} com base no progresso manual registrado.</p>
      </section>

      <div className="goal-detail-grid">
        <section className="goal-plan">
          <p className="eyebrow">Premissas do snapshot</p>
          <dl className="goal-data-list">
            <div><dt>Valor informado</dt><dd>{formatMoney(goal.targetAmount)}</dd></div>
            <div><dt>Base do valor</dt><dd>{goal.targetValueBasis === "CURRENT_VALUE" ? "Valor de hoje" : "Nominal fixo"}</dd></div>
            <div><dt>Inflação anual</dt><dd>{goal.annualInflationRate}</dd></div>
            <div><dt>Retorno anual hipotético</dt><dd>{goal.annualReturnRate}</dd></div>
            <div><dt>Prazo calculado</dt><dd>{goal.projection.projectionMonths} meses</dd></div>
            <div><dt>Fórmula</dt><dd>{goal.projection.formulaVersion}</dd></div>
          </dl>
          <details className="goal-explanation">
            <summary>Como esta estimativa foi calculada</summary>
            <p>O servidor atualiza o valor-alvo quando a base é “valor de hoje”, converte as taxas anuais para períodos mensais e aplica uma série de aportes no momento escolhido. O resultado é arredondado para duas casas pelo método de meio par.</p>
            <p>Motor {goal.projection.engineVersion}. Custos e impostos só entram quando forem informados e modelados.</p>
          </details>
          <div className="goal-warnings" aria-label="Limitações da projeção">
            {goal.projection.warnings.map((warning) => <p key={warning}>{warningCopy[warning] ?? warning}</p>)}
          </div>
        </section>

        <aside className="goal-progress-panel">
          <p className="eyebrow">Acompanhamento manual</p>
          <form className="form-stack" onSubmit={contribute}>
            <label className="field"><span>Valor da contribuição</span><span className="money-input"><b>{goal.currentBalance.currency}</b><input inputMode="decimal" name="amount" pattern="[0-9]+([.,][0-9]{1,2})?" required /></span></label>
            <label className="field"><span>Data</span><input name="contributionDate" required type="date" /></label>
            <label className="field"><span>Observação opcional</span><input maxLength={240} name="note" /></label>
            {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}
            <button className="button button--primary" disabled={pending} type="submit">{pending ? "Registrando…" : "Registrar contribuição"}</button>
          </form>
          <div className="contribution-history">
            <h2>Histórico</h2>
            {goal.contributions.length === 0 && <p>Nenhuma contribuição registrada ainda.</p>}
            {goal.contributions.map((item) => <div key={item.id}><span>{item.contributionDate.split("-").reverse().join("/")}</span><strong>{formatMoney(item.amount)}</strong><small>{item.note ?? "Sem observação"}</small></div>)}
          </div>
        </aside>
      </div>
    </AppShell>
  );
}
