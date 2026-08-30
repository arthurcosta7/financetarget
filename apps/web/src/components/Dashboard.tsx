"use client";

import { ArrowUpRight, CalendarDays, Plus } from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { ApiError, apiFetch } from "@/lib/api/client";
import { formatDate, formatMoney, goalTypeCopy, type Goal } from "@/lib/goals";
import { recordBetaEvent, usePlanningSpaces } from "@/lib/spaces";

export function Dashboard() {
  const router = useRouter();
  const [goals, setGoals] = useState<Goal[]>();
  const [message, setMessage] = useState<string>();
  const { spaces, activeSpace, error: spacesError, selectSpace } = usePlanningSpaces();

  useEffect(() => {
    if (!activeSpace) return;
    apiFetch<Goal[]>(`/planning-spaces/${activeSpace.id}/goals`)
      .then((loaded) => { setGoals(loaded); void recordBetaEvent("DASHBOARD_VIEWED", "ACTIVATION"); })
      .catch((error) => {
        if (error instanceof ApiError && error.status === 401) router.push("/entrar");
        else if (error instanceof ApiError && error.status === 404) router.push("/app/onboarding");
        else setMessage(error instanceof Error ? error.message : "Não foi possível carregar a visão geral.");
      });
  }, [activeSpace, router]);

  const orderedGoals = goals ? [...goals].sort((a, b) => a.targetDate.localeCompare(b.targetDate)) : undefined;
  const contributionMaximum = Math.max(1, ...(goals ?? []).map((goal) => Number(goal.projection.requiredMonthlyContribution.amount)));

  return (
    <AppShell activeSpace={activeSpace} onSpaceChange={selectSpace} section="Visão geral" spaces={spaces}>
      <header className="dashboard-header">
        <div>
          <p className="eyebrow">Horizonte atual</p>
          <h1>Seu plano em movimento.</h1>
          <p>Compare o progresso, o esforço mensal e os próximos prazos sem perder de vista as premissas.</p>
        </div>
        <Link className="button button--primary dashboard-header__action" href="/app/metas/nova">
          <Plus aria-hidden="true" size={18} /> Nova meta
        </Link>
      </header>

      {(message || spacesError) && <p className="goal-empty">{message ?? spacesError?.message}</p>}
      {!goals && !message && <p className="goal-empty" aria-live="polite">Organizando o horizonte…</p>}

      {goals && goals.length > 0 && <>
        <section className="dashboard-overview" aria-label="Resumo das metas">
          <div className="dashboard-metrics">
            <div><span>Metas ativas</span><strong>{goals.length.toString().padStart(2, "0")}</strong></div>
            <div><span>Com aportes</span><strong>{goals.filter((goal) => goal.contributions.length > 0).length.toString().padStart(2, "0")}</strong></div>
            <p>Os valores abaixo são projeções calculadas no servidor e permanecem revisáveis em cada plano.</p>
          </div>

          <div className="dashboard-chart dashboard-chart--progress">
            <div className="dashboard-section-heading">
              <div><p className="eyebrow">Visão comparativa</p><h2>Progresso por meta</h2></div>
              <span>0—100%</span>
            </div>
            <div className="progress-chart" role="group" aria-label="Comparação do percentual concluído de cada meta">
              {goals.map((goal) => (
                <div className="progress-chart__row" key={goal.id}>
                  <span title={goal.title}>{goal.title}</span>
                  <div aria-hidden="true"><i style={{ "--chart-value": `${boundedPercentage(goal.progressPercentage)}%` } as React.CSSProperties} /></div>
                  <strong>{goal.progressPercentage}%</strong>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="dashboard-lower-grid">
          <div className="dashboard-chart dashboard-chart--effort">
            <div className="dashboard-section-heading">
              <div><p className="eyebrow">Capacidade mensal</p><h2>Esforço estimado</h2></div>
              <span>por mês</span>
            </div>
            <div className="effort-chart" role="group" aria-label="Comparação do aporte mensal estimado de cada meta">
              {goals.map((goal) => {
                const ratio = (Number(goal.projection.requiredMonthlyContribution.amount) / contributionMaximum) * 100;
                return <div className="effort-chart__item" key={goal.id}>
                  <div><span>{goal.title}</span><strong>{formatMoney(goal.projection.requiredMonthlyContribution)}</strong></div>
                  <div aria-hidden="true"><i style={{ "--chart-value": `${ratio}%` } as React.CSSProperties} /></div>
                </div>;
              })}
            </div>
            <p className="dashboard-chart__note">Escala relativa à maior estimativa exibida. Consulte cada plano para fórmula, taxas e limitações.</p>
          </div>

          <div className="dashboard-deadlines">
            <div className="dashboard-section-heading">
              <div><p className="eyebrow">Linha do tempo</p><h2>Próximos marcos</h2></div>
              <CalendarDays aria-hidden="true" size={20} strokeWidth={1.6} />
            </div>
            <ol>
              {orderedGoals?.map((goal, index) => <li key={goal.id}>
                <span>{String(index + 1).padStart(2, "0")}</span>
                <div><strong>{goal.title}</strong><small>{goalTypeCopy[goal.goalType]} · {formatDate(goal.targetDate)}</small></div>
                <Link aria-label={`Abrir plano de ${goal.title}`} href={`/app/metas/${goal.id}`}><ArrowUpRight aria-hidden="true" size={18} /></Link>
              </li>)}
            </ol>
          </div>
        </section>
      </>}

      {goals?.length === 0 && <section className="dashboard-empty">
        <span aria-hidden="true">01</span>
        <div><p className="eyebrow">Primeiro passo</p><h2>Transforme uma intenção em prazo.</h2><p>Crie uma meta para visualizar progresso, esforço mensal e cenários.</p></div>
        <Link className="button button--primary" href="/app/metas/nova">Criar primeira meta</Link>
      </section>}
    </AppShell>
  );
}

function boundedPercentage(value: string): number {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return Math.min(100, Math.max(0, parsed));
}
