import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default async function VerificarEmailPage({ searchParams }: { searchParams: Promise<{ token?: string }> }) {
  const { token } = await searchParams;
  return <AuthShell eyebrow="Confirmação" title="Confirme o caminho de volta."
    intro="A verificação protege sua conta e cria seu espaço pessoal de planejamento."
    aside={<p>Cada código funciona uma única vez e expira automaticamente.</p>}>
    <IdentityForm initialToken={token} mode="verify" />
  </AuthShell>;
}
