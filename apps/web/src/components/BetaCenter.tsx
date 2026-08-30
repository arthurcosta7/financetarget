"use client";

import { useEffect, useState } from "react";

import { AppShell } from "@/components/AppShell";
import { FormMessage } from "@/components/FormMessage";
import { ApiError, apiFetch } from "@/lib/api/client";

type BetaConfig = { enabled: boolean; feedbackEnabled: boolean; maximumSharedMembers: number };

export function BetaCenter() {
  const [config, setConfig] = useState<BetaConfig>();
  const [pending, setPending] = useState(false);
  const [message, setMessage] = useState<{ kind: "error" | "success"; text: string }>();

  useEffect(() => { apiFetch<BetaConfig>("/beta/config").then(setConfig).catch(() => setConfig({ enabled: false, feedbackEnabled: false, maximumSharedMembers: 2 })); }, []);

  async function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPending(true);
    const form = event.currentTarget;
    const data = new FormData(form);
    try {
      await apiFetch("/beta/feedback", { method: "POST", body: JSON.stringify({ category: data.get("category"), rating: Number(data.get("rating")), comment: data.get("comment") || undefined }) });
      form.reset();
      setMessage({ kind: "success", text: "Feedback registrado. Obrigado por ajudar a tornar o plano mais compreensível." });
    } catch (cause) {
      setMessage({ kind: "error", text: cause instanceof ApiError ? cause.message : "Não foi possível enviar o feedback." });
    } finally { setPending(false); }
  }

  return <AppShell section="Beta e feedback">
    <header className="app-heading beta-heading"><p className="eyebrow">Programa controlado</p><h1>Aprender sem ampliar o risco.</h1><p>O beta ainda depende de validações humanas. A configuração técnica não autoriza por si só o uso de dados reais.</p></header>
    <div className="beta-layout">
      <section className="beta-status"><p className="eyebrow">Estado atual</p><strong>{config?.enabled ? "Beta técnico habilitado" : "Entrada de participantes bloqueada"}</strong><p>{config?.enabled ? "Esta instância aceita feedback estruturado." : "Revisão jurídica, validação matemática e checklist operacional continuam pendentes."}</p>
        <ol><li><span>01</span> Projeções são possibilidades, não garantias.</li><li><span>02</span> Não inclua renda, valores de metas ou outros dados financeiros no feedback.</li><li><span>03</span> Incidentes de segurança interrompem o programa.</li></ol></section>
      <aside className="beta-feedback"><p className="eyebrow">Feedback estruturado</p>{config?.feedbackEnabled ? <form className="form-stack" onSubmit={submit}><label className="field"><span>O que você está avaliando?</span><select name="category" defaultValue="COMPREHENSION"><option value="COMPREHENSION">Compreensão das projeções</option><option value="USABILITY">Facilidade de uso</option><option value="TRUST">Confiança e transparência</option><option value="COLLABORATION">Planejamento compartilhado</option><option value="PROBLEM">Problema técnico</option></select></label><label className="field"><span>Avaliação de 1 a 5</span><select name="rating" defaultValue="4">{[1,2,3,4,5].map((value) => <option key={value} value={value}>{value}</option>)}</select></label><label className="field"><span>Comentário opcional</span><textarea maxLength={500} name="comment" rows={5} /><small>Não informe renda, patrimônio, valores, e-mail ou dados de terceiros.</small></label>{message && <FormMessage kind={message.kind}>{message.text}</FormMessage>}<button className="button button--primary" disabled={pending} type="submit">{pending ? "Enviando…" : "Enviar feedback"}</button></form> : <div className="beta-locked"><span aria-hidden="true">—</span><p>O formulário ficará disponível somente em uma instância explicitamente preparada para o beta.</p></div>}</aside>
    </div>
  </AppShell>;
}
