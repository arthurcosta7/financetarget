import { AuthShell } from "@/components/AuthShell";
import { IdentityForm } from "@/components/IdentityForms";

export default function EntrarPage() {
  return <AuthShell eyebrow="Acesso" title="Retome de onde parou."
    intro="Entre para revisar suas premissas e seguir construindo o plano."
    aside={<p>Suas projeções descrevem possibilidades calculadas. Elas não são promessas nem recomendações de investimento.</p>}>
    <IdentityForm mode="login" />
  </AuthShell>;
}
