import Link from "next/link";

import { ApiStatus } from "@/components/ApiStatus";

export default function SystemStatusPage() {
  return (
    <main className="status-page" id="conteudo-principal">
      <div className="status-page__content">
        <p className="eyebrow">Diagnóstico local</p>
        <h1>Fundação técnica</h1>
        <p className="status-page__intro">
          Esta verificação confirma o primeiro caminho completo entre interface,
          API e banco. Ela não acessa dados pessoais nem executa cálculos financeiros.
        </p>
        <div className="status-panel">
          <ApiStatus />
        </div>
        <Link className="text-link" href="/">
          ← Voltar ao início
        </Link>
      </div>
    </main>
  );
}
