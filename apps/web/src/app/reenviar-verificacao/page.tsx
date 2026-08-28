import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default function ReenviarVerificacaoPage() {
  return <AuthShell eyebrow="Novo envio" title="Receba um novo link de verificação."
    intro="Informe o e-mail usado no cadastro. A resposta é a mesma exista ou não uma conta pendente."
    aside={<p>O link anterior será invalidado quando uma nova mensagem for emitida.</p>}>
    <IdentityForm mode="request-verification" />
  </AuthShell>;
}
