const EVENT_LABELS = {
  RUECKGABE: "Rückgabe",
  PRUEFUNG: "Prüfung",
  EINLAGERUNG: "Einlagerung",
  REPARATUR: "Reparatur",
  TEILDEMONTAGE: "Teildemontage",
  ENTSORGUNG: "Entsorgung",
};

const PRUEFERGEBNIS_LABELS = {
  GUT: "Guter Zustand",
  REPARATUR: "Reparatur nötig",
  TEILWEISE_BESCHAEDIGT: "Teilweise beschädigt",
  SCHLECHT: "Nicht reparierbar",
};

const EVENT_COLORS = {
  RUECKGABE: "#2196f3",
  PRUEFUNG: "#5c6bc0",
  EINLAGERUNG: "#00796b",
  REPARATUR: "#e91e63",
  TEILDEMONTAGE: "#455a64",
  ENTSORGUNG: "#795548",
};

export default function ScanHistory({ scans }) {
  if (!scans || scans.length === 0) {
    return <p style={{ color: "#999", fontSize: "0.85rem" }}>Noch keine Scan-Ereignisse.</p>;
  }

  return (
    <div style={{ position: "relative", paddingLeft: "16px" }}>
      <div style={{
        position: "absolute", left: "6px", top: 0, bottom: 0,
        width: "2px", background: "#e0e0e0"
      }} />
      {[...scans].reverse().map((s) => (
        <div key={s.id} style={{ marginBottom: "12px", position: "relative" }}>
          <div style={{
            position: "absolute", left: "-13px", top: "4px",
            width: "10px", height: "10px", borderRadius: "50%",
            background: EVENT_COLORS[s.event_typ] || "#999",
            border: "2px solid white",
            boxShadow: "0 0 0 1px #ccc",
          }} />
          <div style={{ fontSize: "0.78rem", color: "#999" }}>
            {new Date(s.zeitstempel).toLocaleString("de-AT")}
            {s.gescannt_von && ` · ${s.gescannt_von}`}
          </div>
          <div style={{ fontWeight: 600, fontSize: "0.88rem", color: EVENT_COLORS[s.event_typ] || "#333" }}>
            {EVENT_LABELS[s.event_typ] || s.event_typ}
          </div>
          {s.standort_name && (
            <div style={{ fontSize: "0.82rem", color: "#555" }}>{s.standort_name}</div>
          )}
          {s.pruefergebnis && (
            <div style={{ fontSize: "0.78rem", color: "#888" }}>
              Ergebnis: {PRUEFERGEBNIS_LABELS[s.pruefergebnis] || s.pruefergebnis}
            </div>
          )}
          {s.ersatzteile_vorhanden != null && s.event_typ === "REPARATUR" && (
            <div style={{ fontSize: "0.78rem", color: "#888" }}>
              Ersatzteile: {s.ersatzteile_vorhanden ? "vorhanden" : "nicht vorhanden"}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
