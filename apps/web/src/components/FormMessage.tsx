export function FormMessage({ kind, children }: { kind: "error" | "success" | "neutral"; children: React.ReactNode }) {
  return (
    <div className={`form-message form-message--${kind}`} role={kind === "error" ? "alert" : "status"}>
      {children}
    </div>
  );
}
