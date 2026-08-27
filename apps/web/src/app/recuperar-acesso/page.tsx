import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default function RecuperarAcessoPage() {
  return <AuthShell eyebrow="Recuperação" title="Encontre o caminho de volta."
    intro="Informe seu e-mail. A resposta será a mesma exista ou não uma conta vinculada."
    aside={<p>Esse cuidado evita que terceiros descubram quem usa o produto.</p>}>
    <IdentityForm mode="request-recovery" />
  </AuthShell>;
}
