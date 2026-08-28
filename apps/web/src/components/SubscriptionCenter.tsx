"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch, idempotencyKey } from "@/lib/api/client";

type Plan = { code: string; displayName: string; entitlements: Record<string, string> };
type Overview = {
  subscription: null | { planCode: string; status: string; provider: string; version: number; updatedAt: string };
  entitlements: Record<string, string>;
  availablePlans: Plan[];
  mockCheckoutEnabled: boolean;
};
type Preferences = { essential: boolean; planningReminders: boolean; productUpdates: boolean; marketing: boolean };
type Features = { paymentsMock: boolean; notificationsMock: boolean; openFinance: boolean; loyalty: boolean;
  travel: boolean; realEstateFinancing: boolean; autoFinancing: boolean };
type Checkout = { id: string; planCode: string; provider: string; reference: string; status: string; createdAt: string };

const featureLabels: Array<[keyof Features, string]> = [
  ["paymentsMock", "Pagamentos simulados"], ["notificationsMock", "Notificações simuladas"],
  ["openFinance", "Open Finance"], ["loyalty", "Milhas e fidelidade"], ["travel", "Viagens"],
  ["realEstateFinancing", "Financiamento imobiliário"], ["autoFinancing", "Financiamento automotivo"],
];

export function SubscriptionCenter() {
  const router = useRouter();
  const [overview, setOverview] = useState<Overview>();
  const [preferences, setPreferences] = useState<Preferences>();
  const [features, setFeatures] = useState<Features>();
  const [checkout, setCheckout] = useState<Checkout>();
  const [message, setMessage] = useState<{ kind: "error" | "success" | "neutral"; text: string }>();

  useEffect(() => {
    Promise.all([
      apiFetch<Overview>("/subscriptions/current"),
      apiFetch<Preferences>("/notification-preferences"),
      apiFetch<Features>("/features"),
    ]).then(([subscription, notificationPreferences, flags]) => {
      setOverview(subscription); setPreferences(notificationPreferences); setFeatures(flags);
    }).catch((error) => {
      if (error instanceof ApiError && error.status === 401) router.push("/entrar");
      else setMessage({ kind: "error", text: "Não foi possível carregar as configurações." });
    });
  }, [router]);

  async function simulateCheckout(planCode: string) {
    try {
      const result = await apiFetch<Checkout>("/subscriptions/mock-checkouts", {
        method: "POST", headers: { "Idempotency-Key": idempotencyKey("mock-checkout") },
        body: JSON.stringify({ planCode }),
      });
      setCheckout(result);
      setMessage({ kind: "neutral", text: "Sessão simulada criada. Nenhuma cobrança foi realizada e o plano não foi ativado." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível simular." });
    }
  }

  async function savePreferences(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!preferences) return;
    try {
      const updated = await apiFetch<Preferences>("/notification-preferences", {
        method: "PUT", body: JSON.stringify({ planningReminders: preferences.planningReminders,
          productUpdates: preferences.productUpdates, marketing: preferences.marketing }),
      });
      setPreferences(updated);
      setMessage({ kind: "success", text: "Preferências salvas. Nenhuma mensagem real foi enviada." });
    } catch (error) {
      setMessage({ kind: "error", text: error instanceof ApiError ? error.message : "Não foi possível salvar." });
    }
  }

  return (
    <AppShell section="Plano · comunicações">
      <header className="app-heading subscription-heading">
        <p className="eyebrow">Acesso sob controle</p>
        <h1>Benefícios claros, cobrança ainda não.</h1>
        <p>Esta área exercita contratos e falhas sem movimentar dinheiro ou enviar mensagens.</p>
      </header>
      {message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}

      <section className="access-rail" aria-labelledby="current-access-title">
        <div className="access-rail__summary">
          <span className="hero__number">Estado canônico</span>
          <h2 id="current-access-title">{overview?.subscription?.planCode ?? "Nenhum plano ativo"}</h2>
          <p>{overview?.subscription ? `Estado ${overview.subscription.status.toLowerCase()} · versão ${overview.subscription.version}`
            : "Uma sessão simulada não altera o acesso. Somente um evento assinado pode fazê-lo."}</p>
        </div>
        <ol className="access-rail__steps" aria-label="Fluxo simulado da assinatura">
          <li data-state="ready"><span>01</span><strong>Catálogo interno</strong><small>benefícios definidos pelo produto</small></li>
          <li data-state={checkout ? "ready" : "waiting"}><span>02</span><strong>Sessão simulada</strong><small>sem preço ou cobrança</small></li>
          <li data-state={overview?.subscription ? "ready" : "waiting"}><span>03</span><strong>Evento verificado</strong><small>assinatura e repetição controladas</small></li>
        </ol>
      </section>

      <div className="subscription-layout">
        <section className="plan-catalog" aria-labelledby="plans-title">
          <p className="eyebrow">Catálogo deste ambiente</p>
          <h2 id="plans-title">Escolha uma simulação</h2>
          <p className="section-intro">Os planos e limites vêm do banco. Não há preço, provedor externo nem ativação automática.</p>
          <div className="plan-list">
            {overview?.availablePlans.map((plan) => <article key={plan.code}>
              <div><h3>{plan.displayName}</h3><small>{plan.code}</small></div>
              <ul aria-label={`Benefícios de ${plan.displayName}`}>
                {Object.entries(plan.entitlements).map(([key, value]) => <li key={key}>
                  <span>{entitlementLabel(key)}</span><strong>{value}</strong>
                </li>)}
              </ul>
              <button className="button button--quiet" disabled={!overview.mockCheckoutEnabled}
                onClick={() => simulateCheckout(plan.code)} type="button">Simular esta escolha</button>
            </article>)}
            {overview && overview.availablePlans.length === 0 && <p>Nenhum plano sintético foi configurado neste ambiente.</p>}
          </div>
        </section>

        <section className="communication-panel" aria-labelledby="preferences-title">
          <p className="eyebrow">Preferências</p>
          <h2 id="preferences-title">O que pode chegar até você</h2>
          <p className="section-intro">As escolhas são persistidas, mas nesta fase toda entrega termina no mock local.</p>
          {preferences && <form className="preference-form" onSubmit={savePreferences}>
            <Preference checked disabled label="Mensagens essenciais" detail="Segurança e mudanças críticas da conta." />
            <Preference checked={preferences.planningReminders} label="Lembretes de planejamento"
              detail="Revisões de meta e acompanhamento." onChange={(value) => setPreferences({ ...preferences, planningReminders: value })} />
            <Preference checked={preferences.productUpdates} label="Atualizações do produto"
              detail="Mudanças relevantes na experiência." onChange={(value) => setPreferences({ ...preferences, productUpdates: value })} />
            <Preference checked={preferences.marketing} label="Novidades e pesquisas"
              detail="Conteúdo opcional e convites de pesquisa." onChange={(value) => setPreferences({ ...preferences, marketing: value })} />
            <button className="button button--primary" type="submit">Salvar preferências</button>
          </form>}
        </section>
      </div>

      <section className="hub-boundary" aria-labelledby="hub-title">
        <div><p className="eyebrow">Fronteira operacional</p><h2 id="hub-title">Integrações não são promessas.</h2></div>
        <dl>{features && featureLabels.map(([key, label]) => <div key={key}>
          <dt>{label}</dt><dd>{features[key] ? "Mock habilitado" : "Desligado"}</dd>
        </div>)}</dl>
      </section>
    </AppShell>
  );
}

function Preference({ checked, disabled = false, label, detail, onChange }: { checked: boolean; disabled?: boolean;
  label: string; detail: string; onChange?: (value: boolean) => void }) {
  return <label className="preference-row">
    <input aria-label={label} checked={checked} disabled={disabled}
      onChange={(event) => onChange?.(event.target.checked)} type="checkbox" />
    <span><strong>{label}</strong><small>{detail}</small></span>
  </label>;
}

function entitlementLabel(key: string): string {
  return ({ GOAL_MANAGEMENT: "Gestão de metas", SCENARIO_LIMIT: "Cenários por meta",
    SHARED_PLANNING: "Planejamento compartilhado" } as Record<string, string>)[key] ?? key.replaceAll("_", " ").toLowerCase();
}
