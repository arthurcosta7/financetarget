import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default function CadastroPage() {
  return <AuthShell eyebrow="Começo" title="Seu plano começa com uma conta."
    intro="Poucos dados agora. Mais contexto apenas quando ele for necessário para o seu planejamento."
    aside={<p>Não pedimos CPF, conta bancária, endereço ou dados financeiros no cadastro.</p>}>
    <IdentityForm mode="register" />
  </AuthShell>;
}
