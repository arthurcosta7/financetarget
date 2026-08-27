const points = [
  { label: "Hoje", x: 2, y: 82 },
  { label: "Ritmo", x: 34, y: 64 },
  { label: "Margem", x: 66, y: 38 },
  { label: "Meta", x: 98, y: 10 },
] as const;

export function Trajectory() {
  return (
    <figure className="trajectory" aria-labelledby="trajectory-title">
      <figcaption id="trajectory-title" className="sr-only">
        Exemplo ilustrativo de uma trajetória entre o momento atual e uma meta
      </figcaption>
      <svg
        aria-hidden="true"
        className="trajectory__line"
        viewBox="0 0 100 90"
        preserveAspectRatio="none"
      >
        <path d="M2 82 C28 79 38 62 51 56 C70 47 79 22 98 10" />
        {points.map((point) => (
          <circle key={point.label} cx={point.x} cy={point.y} r="1.2" />
        ))}
      </svg>
      <div className="trajectory__labels" aria-hidden="true">
        {points.map((point) => (
          <span key={point.label}>{point.label}</span>
        ))}
      </div>
    </figure>
  );
}
