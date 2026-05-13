const EVENT_LABELS = {
  RUECKGABE: "Rückgabe",
  EINLAGERUNG: "Einlagerung",
  PRUEFUNG: "Prüfung",
  TRANSPORT: "Transport",
  REPARATUR: "Reparatur",
  ENTSORGUNG: "Entsorgung",
  TEILDEMONTAGE: "Teildemontage",
};

const EVENT_COLORS = {
  RUECKGABE: "#2196f3",
  EINLAGERUNG: "#00796b",
  PRUEFUNG: "#5c6bc0",
  TRANSPORT: "#9c27b0",
  REPARATUR: "#e91e63",
  ENTSORGUNG: "#795548",
  TEILDEMONTAGE: "#455a64",
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
        </div>
      ))}
    </div>
  );
}
